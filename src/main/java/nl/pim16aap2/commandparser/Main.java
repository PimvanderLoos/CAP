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

// TODO: Consider using ByteBuddy to generate the commands? Might be completely overkill, though
// TODO: Add some way to inject text into the Text system. For example, when adding a piece of text with
//       a command, store which command it is and if it is listed as a subcommand or a supercommand.
//       Allow registering event handlers or something when a new component is added.
//       This is useful for stuff like clickable text. In Minecraft, this would be the ability to click on a command
//       in a help menu to execute the help command for that command.
// TODO: Allow positional optional arguments. Perhaps Required and OptionalPositional can both extend a Positional interface?
//       Maybe also extend a FlaggedArgument interface for stuff that does require flags?
//       This can be useful for stuff like "/bigdoors help addowner".
// TODO: Allow using space as a separator of flag-value pairs.
// TODO: Make a class somewhere where you can register ColorScheme objects. This class can then be used for caching
//       stuff like finished Texts etc.
//       For this to work properly, there should be some mechanism to keep track of whether a command(system) was
//       changed since it was last cached. For example, if subcommands are added, the cache will have to be invalidated.
// TODO: For the HelpCommand, just make it a boolean for the command (default true) to always look for it in the format
//       "/whatever command help [arguments]". Provide a default HelpCommand thingy, but also make it a builder,
//       to fully customize how the help command works. For example, hidden commands should probably display their help
//       also when no arguments are provided at all? Also, make the default help command extend an IHelpCommand interface
//       so you can just provide your own custom one if the options aren't sufficient.
// TODO: Add permissions to commands (and arguments?). Probably a setter via an interface.
// TODO: Command/argument(name/value) completion.
// TODO: Subcommand sorting + sorting options (alphabetical? Length? Creation order?).
//       Currently, it's a List, but perhaps a LinkedHashMap would be better for this?
//       https://docs.oracle.com/javase/10/docs/api/java/util/LinkedHashMap.html
//       also, arguments should use this as well, though those should not be sortable; they should just be sorted based
//       on insertion order.
// TODO: Allow defining and supplying custom renderers.
// TODO: For the argument renderers, allow specifying long/short name requirements and stuff. In some situations,
//       having both might be nice, while in other, it's better to only have the short version. Right?
// TODO: Customizable bracket types for optional/required arguments.
// TODO: Make is possible to have certain commands be server-only or player-only. These commands shouldn't show up in
//       the help menus.
// TODO: Maybe flag parameters (e.g. --admin), aka valueless parameters should have their own class?
// TODO: Allow the use of empty lines. For Spigot (and probably other platforms?) '\n' isn't good enough.
//       Instead, Spigot needs a color code on an otherwise empty line to have empty lines.
//       Perhaps this can be done via the color scheme?
// TODO: Support ResourceBundle.
// TODO: For the long help, maybe fall back to the summary if no description is available?
// TODO: Do not use 'helpful' messages in exceptions, but just variables. Whomever catches the exception
//       Should be able to easily parse it themselves. If an exception requires additional text to explain it
//       then it's time to create a new type or at the very least a new constructor.
// TODO: Every command needs to be able to print its own help message. Maybe every command should have its own usage
//       renderer? The help renderer can just use that to render the complete help command.
// TODO: Make it possible to use functions to verify arguments. E.g. for IntArgument: Arg must be in [1, 10].
// TODO: Make a module specific for Spigot and/or Paper. This should contain all the stuff needed for thost platforms.
//       E.g. BukkitCommandSender, ChatColor, Player/World Arguments, etc.
//       For the BukkitChatColor, the builder can just accept ChatColors and take care of the default off value /
//       instantiating TextComponents.
// TODO: Should Optional arguments be wrapped inside Optional as well? Might be nice.
// TODO: Store the HelpArgument in a command separately, so that we can
//       actually figure out which argument is the help argument.
// TODO: Unit tests.
// TODO: Make sure that positional arguments fed in the wrong order gets handled gracefully
//       (there are probably going to be some casting issues).
// TODO: ChatColors for required labels/separators/etc, now that free required arguments are possible.
// TODO: Maybe store the arguments by their label inside the CommandResult? That would avoid confusion of name vs longName.
// TODO: Be more consistent in naming help menus. There should be a clear distinction between the command-specific long help
//       and the command's list of subcommands. Maybe help text and help menu?

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

    private static void tryArgs(final @NonNull CommandManager commandManager, final @NonNull String... args)
    {
        final String command = arrToString(args);
        System.out.println(command + ":\n");
        final DefaultCommandSender commandSender = new DefaultCommandSender();
//        commandSender.setColorScheme(Main.getColorScheme());

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

        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "-p=pim16aap2");
        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "-p=pim16aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "myD\\\"oor", "-p=pim16aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "\"myD\\\"oor\"", "-p=pim16aap2", "-a");
        tryArgs(commandManager, "bigdoors", "addowner", "\"myD\\\"", "oor\"", "-p=\"pim16\"aap2", "-a");
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
                          .label("player")
                          .summary("The name of the player to add as owner")
                          .build())
            .argument(Argument.StringArgument
                          .getRepeatable()
                          .name("g")
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
            .subCommand(addOwner)
            .subCommands(subcommands)
            .commandExecutor(CommandResult::sendHelpMenu)
            .hidden(true)
            .build();

        // TODO: Allow specifying the name instead of the subcommand instance?
        //       If the CommandManager becomes a builder, it can just retrieve the instances is the ctor.
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
            .optionalParameterStyle(new TextComponent(MinecraftStyle.BLUE.getStringValue()))
            .optionalParameterFlagStyle(new TextComponent(MinecraftStyle.LIGHT_PURPLE.getStringValue()))
            .optionalParameterSeparatorStyle(new TextComponent(MinecraftStyle.WHITE.getStringValue()))
            .optionalParameterLabelStyle(new TextComponent(MinecraftStyle.GRAY.getStringValue()))
            .requiredParameterStyle(new TextComponent(MinecraftStyle.RED.getStringValue()))
            .summaryStyle(new TextComponent(MinecraftStyle.AQUA.getStringValue()))
            .regularTextStyle(new TextComponent(MinecraftStyle.GOLD.getStringValue()))
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
