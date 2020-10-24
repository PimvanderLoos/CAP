package nl.pim16aap2.commandparser;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.command.CommandResult;
import nl.pim16aap2.commandparser.commandsender.DefaultCommandSender;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.CommandParserException;
import nl.pim16aap2.commandparser.exception.IllegalValueException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NoPermissionException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;
import nl.pim16aap2.commandparser.manager.CommandManager;
import nl.pim16aap2.commandparser.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.commandparser.text.ColorScheme;
import nl.pim16aap2.commandparser.text.Text;
import nl.pim16aap2.commandparser.text.TextComponent;
import nl.pim16aap2.commandparser.text.TextType;
import nl.pim16aap2.commandparser.util.CheckedSupplier;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

// TODO: Add some way to inject text into the Text system. For example, when adding a piece of text with
//       a command, store which command it is and if it is listed as a subcommand or a supercommand.
//       Allow registering event handlers or something when a new component is added.
//       This is useful for stuff like clickable text. In Minecraft, this would be the ability to click on a command
//       in a help menu to execute the help command for that command.
// TODO: Allow using space as a separator of flag-value pairs.
// TODO: Make a class somewhere where you can register ColorScheme objects. This class can then be used for caching
//       stuff like finished Texts etc.
//       For this to work properly, there should be some mechanism to keep track of whether a command(system) was
//       changed since it was last cached. For example, if subcommands are added, the cache will have to be invalidated.
// TODO: Hidden commands should have a default executor that just displays the help menu.
// TODO: Rename hidden commands to virtual commands as that more accurately describes what they are.
// TODO: Add permissions to commands (and arguments?). Probably a setter via an interface.
// TODO: Command/argument completion.
// TODO: Argument value suggestions (e.g. (online/nearby) player names, door names).
// TODO: Allow defining and supplying custom renderers.
// TODO: For the argument renderers, allow specifying long/short name requirements and stuff. In some situations,
//       having both might be nice, while in other, it's better to only have the short version. Right?
// TODO: Customizable bracket types for optional/required arguments.
// TODO: Make is possible to have certain commands be server-only or player-only. These commands shouldn't show up in
//       the help menus.
// TODO: Allow the use of empty lines. For Spigot (and probably other platforms?) '\n' isn't good enough.
//       Instead, Spigot needs a color code on an otherwise empty line to have empty lines.
//       Perhaps this can be done via the color scheme?
// TODO: Support ResourceBundle.
// TODO: Currently, the commands are kinda stored in a tree shape (1 super, n subs). Perhaps store it in an actual tree?
// TODO: For the long help, maybe fall back to the summary if no description is available?
// TODO: Do not use 'helpful' messages in exceptions, but just variables. Whomever catches the exception
//       Should be able to easily parse it themselves. If an exception requires additional text to explain it
//       then it's time to create a new type or at the very least a new constructor.
// TODO: Make it possible to use functions to verify arguments. E.g. for IntArgument: Arg must be in [1, 10].
// TODO: Make a module specific for Spigot and/or Paper. This should contain all the stuff needed for thost platforms.
//       E.g. BukkitCommandSender, ChatColor, Player/World Arguments, etc.
//       For the BukkitChatColor, the builder can just accept ChatColors and take care of the default off value /
//       instantiating TextComponents.
// TODO: Should Optional arguments be wrapped inside Optional as well? Might be nice.
// TODO: Unit tests.
// TODO: Make sure that positional arguments fed in the wrong order gets handled gracefully
//       (there are probably going to be some casting issues).
// TODO: Maybe store the arguments by their label inside the CommandResult? That would avoid confusion of name vs longName.
// TODO: Be more consistent in naming help menus. There should be a clear distinction between the command-specific long help
//       and the command's list of subcommands. Maybe help text and help menu?
// TODO: The Supplier<List<String>> in the Arguments should really be a function and it should have access to some basic
//       stuff, like ICommandSender and Command.

public class Main
{
    private static @NonNull String arrToString(final @NonNull String... args)
    {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        sb.append("=============\n").append("Arguments:");
        for (String arg : args)
        {
            sb.append(" \"").append(arg).append("\"");
            sb2.append(" ").append(arg);
        }
        return sb.append(", total: \"/").toString() + sb2.append("\"").substring(1);
    }

    private static void tabComplete(final @NonNull CommandManager commandManager, final @NonNull String... args)
    {
        final String command = arrToString(args);
        System.out.println(command + ":\n");
        final DefaultCommandSender commandSender = new DefaultCommandSender();

        final @NonNull List<String> tabOptions = commandManager.getTabCompleteOptions(commandSender, args);
        final StringBuilder sb = new StringBuilder();
        sb.append("Tab complete options:\n");
        tabOptions.forEach(opt -> sb.append(opt).append("\n"));
        System.out.println(sb.toString());
        System.out.println("=============\n");
    }

    private static void tryArgs(final @NonNull CommandManager commandManager, final @NonNull String... args)
    {
        final String command = arrToString(args);
        System.out.println(command + ":\n");
        final DefaultCommandSender commandSender = new DefaultCommandSender();
        commandSender.setColorScheme(Main.getColorScheme());

        try
        {
            commandManager.parseInput(commandSender, args).run();
        }
        catch (CommandNotFoundException e)
        {
            System.out.println("Failed to find command: " + e.getMissingCommand());
            e.printStackTrace();
        }
        catch (NonExistingArgumentException e)
        {
            System.out.println("Failed to find argument: \"" + e.getNonExistingArgument() + "\" for command: \"" +
                                   e.getCommand().getName() + "\"");
            e.printStackTrace();
        }
        catch (MissingArgumentException e)
        {
            System.out.println(
                "Failed to find value for argument: \"" + e.getMissingArgument().getName() + "\" for command: \"" +
                    e.getCommand().getName() + "\"");
            e.printStackTrace();
        }
        catch (IllegalValueException e)
        {
            System.out.println(
                "Illegal argument \"" + e.getIllegalValue() + "\" for command: \"" + e.getCommand().getName() + "\"");
            e.printStackTrace();
        }
        catch (EOFException e)
        {
            System.out.println("EOFException!");
            e.printStackTrace();
        }
        catch (NoPermissionException e)
        {
            System.out.println("No permission!");
            e.printStackTrace();
        }
        catch (CommandParserException e)
        {
            System.out.println("General CommandParserException!");
            e.printStackTrace();
        }

        System.out.println("=============\n");
    }

    public static void main(final String... args)
    {
//        testTextComponents();
        final CommandManager commandManager = initCommandManager();
//        testHelpRenderer(commandManager);

        tabComplete(commandManager, "big");
        tabComplete(commandManager, "add");
        tabComplete(commandManager, "bigdoors", "a");
        tabComplete(commandManager, "bigdoors", "\"a");
        tabComplete(commandManager, "bigdoors", "h");
        tabComplete(commandManager, "bigdoors", "subcomma");
        tabComplete(commandManager, "bigdoors", "addowner", "myDoor", "-p=pim16aap2");
        tabComplete(commandManager, "bigdoors", "addowner", "myDoor", "--play");

        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "-p=pim16aap2");
        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "--player=pim16aap2");
        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "--player=pim16aap2", "--admin");
        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "-p=pim16aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "myD\\\"oor", "-p=pim16aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "\"myD\\\"oor\"", "-p=pim16aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "\"myD\\\"", "oor\"", "-p=\"pim16\"aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "\'myDoor\'", "-p=pim16aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "\'myDoor\'", "-p=pim16aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "-h");
        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "-p=\"pim16", "\"aap2", "-a");

        tryArgs(commandManager, "bigdoors", "help", "addowner");
        tryArgs(commandManager, "bigdoors");
        tryArgs(commandManager, "bigdoors", "help", "1");
        tryArgs(commandManager, "bigdoors", "help");
        tryArgs(commandManager, "bigdoors", "addowner");
    }

    private static CommandManager initCommandManager()
    {
        final CommandManager commandManager = CommandManager
            .builder()
            .debug(true)
            .helpCommandRenderer(DefaultHelpCommandRenderer
                                     .builder()
                                     .firstPageSize(1)
                                     .build())
            .build();

        final int subsubCommandCount = 5;
        final List<Command> subsubcommands = new ArrayList<>(subsubCommandCount);
        for (int idx = 0; idx < subsubCommandCount; ++idx)
        {
            final String command = "subsubcommand_" + idx;
            final Command generic = Command
                .commandBuilder().name(command)
                .addDefaultHelpArgument(true)
                .commandManager(commandManager)
                .summary("This is the summary for subsubcommand_" + idx)
                .argument(Argument.StringArgument.getRequired().name("value").summary("random value").build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subsubcommands.add(generic);
        }

        final Command addOwner = Command
            .commandBuilder()
            .commandManager(commandManager)
            .name("addowner")
            .addDefaultHelpArgument(true)
            .description("Add 1 or more players or groups of players as owners of a door.")
            .summary("Add another owner to a door.")
            .subCommands(subsubcommands)
            .permission("bigdoors.user.addowner")
            .argument(Argument.StringArgument
                          .getRequired()
                          .name("doorID")
                          .summary("The name or UID of the door")
                          .build())
            .argument(Argument.valuesLessBuilder()
                              .value(true)
                              .name("a")
                              .longName("admin")
                              .summary("Make the user an admin for the given door. Only applies to players.")
                              .build())
            .argument(Argument.StringArgument
                          .getRepeatable()
                          .name("p")
                          .longName("player")
                          .label("player")
                          .summary("The name of the player to add as owner")
                          .build())
            .argument(Argument.StringArgument
                          .getRepeatable()
                          .name("g")
                          .longName("group")
                          .label("group")
                          .summary("The name of the group to add as owner")
                          .build())
            .commandExecutor(
                commandResult ->
                    new AddOwner(commandResult.getParsedArgument("doorID"),
                                 commandResult.getParsedArgument("p"),
                                 commandResult.<List<String>>getParsedArgument("g"),
                                 commandResult.getParsedArgument("a")).runCommand())
            .build();

        final int subCommandCount = 20;
        final List<Command> subcommands = new ArrayList<>(subCommandCount);
        for (int idx = 0; idx < subCommandCount; ++idx)
        {
            final String command = "subcommand_" + idx;
            final Command generic = Command
                .commandBuilder().name(command)
                .addDefaultHelpArgument(true)
                .commandManager(commandManager)
                .argument(Argument.StringArgument.getRequired().name("value").summary("random value").build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subcommands.add(generic);
        }

        final Command bigdoors = Command
            .commandBuilder()
            .commandManager(commandManager)
            .addDefaultHelpSubCommand(true)
            .name("bigdoors")
            .headerSupplier(colorScheme -> new Text(colorScheme)
                .add("Parameters in angled brackets are required: ", TextType.HEADER)
                .add("<", TextType.REQUIRED_PARAMETER)
                .add("parameter", TextType.REQUIRED_PARAMETER_LABEL)
                .add(">", TextType.REQUIRED_PARAMETER).add("\n")

                .add("Parameters in square brackets are optional: ", TextType.HEADER)
                .add("[", TextType.OPTIONAL_PARAMETER)
                .add("parameter", TextType.OPTIONAL_PARAMETER_LABEL)
                .add("]", TextType.OPTIONAL_PARAMETER).add("\n")

                .add("If an argument is followed by a \"+\" symbol, it can be\n" +
                         "repeated as many times as you want. For example, for a\n" +
                         "hypothetical command \"", TextType.HEADER)
                .add("/command ", TextType.COMMAND)
                .add("[", TextType.OPTIONAL_PARAMETER)
                .add("p", TextType.OPTIONAL_PARAMETER_FLAG)
                .add("=", TextType.OPTIONAL_PARAMETER_SEPARATOR)
                .add("player", TextType.OPTIONAL_PARAMETER_LABEL)
                .add("]+", TextType.OPTIONAL_PARAMETER)

                .add("\", you can do: \n\"", TextType.HEADER)
                .add("/command -p=playerA -p=playerB", TextType.REGULAR_TEXT)
                .add("\".\n", TextType.HEADER)
                .add(" ", TextType.REGULAR_TEXT)
                .toString())

            .subCommand(addOwner)
            .subCommands(subcommands)
            .commandExecutor(CommandResult::sendHelpMenu)
            .hidden(true)
            .build();

        subcommands.forEach(commandManager::addCommand);
        subsubcommands.forEach(commandManager::addCommand);
        commandManager.addCommand(addOwner).addCommand(bigdoors);

        return commandManager;
    }

    private static void tryHelpCommand(CheckedSupplier<Text, IllegalValueException> sup)
    {
        System.out.println("==============================");
        try
        {
            System.out.print(sup.get());
        }
        catch (IllegalValueException e)
        {
            System.out.println(
                "Illegal argument \"" + e.getIllegalValue() + "\" for command: \"" + e.getCommand().getName() + "\"");
        }
        System.out.println("==============================\n");
    }

    static ColorScheme getClearColorScheme()
    {
        return ColorScheme.builder().build();
    }

    static ColorScheme getColorScheme()
    {
        return ColorScheme
            .builder()
            .setDisableAll(MinecraftStyle.RESET.getStringValue())
            .commandStyle(new TextComponent(MinecraftStyle.GOLD.getStringValue()))
            .optionalParameterStyle(new TextComponent(MinecraftStyle.BLUE.getStringValue() +
                                                          MinecraftStyle.BOLD.getStringValue()))
            .optionalParameterFlagStyle(new TextComponent(MinecraftStyle.LIGHT_PURPLE.getStringValue()))
            .optionalParameterSeparatorStyle(new TextComponent(MinecraftStyle.DARK_RED.getStringValue()))
            .optionalParameterLabelStyle(new TextComponent(MinecraftStyle.DARK_AQUA.getStringValue()))
            .requiredParameterStyle(new TextComponent(MinecraftStyle.RED.getStringValue() +
                                                          MinecraftStyle.BOLD.getStringValue()))
            .requiredParameterFlagStyle(new TextComponent(MinecraftStyle.DARK_BLUE.getStringValue()))
            .requiredParameterSeparatorStyle(new TextComponent(MinecraftStyle.BLACK.getStringValue()))
            .requiredParameterLabelStyle(new TextComponent(MinecraftStyle.WHITE.getStringValue()))
            .summaryStyle(new TextComponent(MinecraftStyle.AQUA.getStringValue()))
            .regularTextStyle(new TextComponent(MinecraftStyle.DARK_PURPLE.getStringValue()))
            .headerStyle(new TextComponent(MinecraftStyle.GREEN.getStringValue()))
            .footerStyle(new TextComponent(MinecraftStyle.DARK_RED.getStringValue()))
            .build();
    }

    static void testTextComponents()
    {
        final ColorScheme colorScheme = getColorScheme();

        {
            final Text text = new Text(colorScheme);
            text.add("Unstyled text!");
            System.out.println(text);
            System.out.println(text.add(text));
        }

        final Text text = new Text(colorScheme);
        text.add("This is a command", TextType.COMMAND).add("\n")
            .add("This is an optional parameter", TextType.OPTIONAL_PARAMETER).add("\n")
            .add("This is something else? I can't remember the types :(", TextType.HEADER).add("\n");


        final Text text2 = new Text(colorScheme);
        text2.add("This is the second Text!", TextType.REQUIRED_PARAMETER).add("\n");
        text2.add("This is some unstyled text!").add("\n");

        System.out.println(text.add(text2).toString());
    }
}
