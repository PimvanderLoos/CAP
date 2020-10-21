package nl.pim16aap2.commandparser;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.command.Command;
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
// TODO: Right now the DefaultCommandHandler stores a ColorScheme which is then always used. It would probably be nice
//       to be able to override this. Alternatively, don't store the enable/disable string of the styles in the
//       TextComponents, but just the TextStyle. Then get the strings in the toString method. This would allow
//       changing the scheme at any time.
// TODO: Support ResourceBundle.
// TODO: For the long help, maybe fall back to the summary if no description is available?
// TODO: Should no-arg return help?
// TODO: Do not use 'helpful' messages in exceptions, but just variables. Whomever catches the exception
//       Should be able to easily parse it themselves. If an exception requires additional text to explain it
//       then it's time to create a new type or at the very least a new constructor.
// TODO: For the ColorScheme, make the disableAll or whatever it's called optional. Just accept a nullable String
//       to disable every style in its constructor. When not specified, the text should always terminate with the
//       specific style's off (not with any disableAll), and when it is specified, it should just use that one variable.
//       Furthermore, styles should have a second constructor that uses a default 'off' value that's just an empty
//       String for cases like this.

public class Main
{
    private static @NonNull String arrToString(final @NonNull String... args)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("=============\n").append("Arguments:");
        for (String arg : args)
            sb.append(" \"").append(arg).append("\"");
        return sb.toString();
    }

    private static void tryArgs(final @NonNull CommandManager commandManager, final @NonNull String... args)
    {
        final String command = arrToString(args);
        System.out.println(command + ":\n");

        try
        {
            commandManager.parseCommand(args).run();
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
            System.out.println("Failed to find value for argument: \"" + e.getMissingArgument() + "\" for command: \"" +
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
        String executor = "mip";

//        testTextComponents();
        CommandManager commandManager = initCommandManager();
//        testHelpRenderer(commandManager);

        final ColorScheme colorScheme = getColorScheme();
        DefaultHelpCommandRenderer.builder().colorScheme(colorScheme).pageSize(16).firstPageSize(1).build();

//        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "-p=pim16aap2", "-a");
//        tryArgs(commandManager, "bigdoors", "addowner", "\"myD\\\"oor\"", "-p=pim16aap2", "-a");
//        tryArgs(commandManager, "bigdoors", "addowner", "\"myD\\\"", "oor\"", "-p=\"pim16\"aap2", "-a");
//        tryArgs(commandManager, "bigdoors", "addowner", "\'myDoor\'", "-p=pim16aap2", "-a");
//        tryArgs(commandManager, "bigdoors", "addowner", "-h");
//        tryArgs(commandManager, "bigdoors", "addowner", "myDoor", "-p=\"pim16", "\"aap2", "-a");
//        tryArgs(commandManager, "addowner", "myDoor", "-p=pim16aap2", "-p=pim16aap3", "-p=pim16aap4", "-a");
//
//        tryArgs(commandManager, "bigdoors", "help", "addowner");
        tryArgs(commandManager, "bigdoors", "help", "-h=addowner");
        tryArgs(commandManager, "bigdoors", "help", "-h=1");
//        tryArgs(commandManager, "bigdoors", "help");
//        tryArgs(commandManager, "addowner", "myDoor", "-p=pim16aap2", "-p=pim16aap3", "-p=pim16aap4", "-a");
    }

    private static CommandManager initCommandManager()
    {
        final ColorScheme colorScheme = getColorScheme();

        final CommandManager commandManager = new CommandManager(System.out::println, () -> colorScheme);

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
            .argument(Argument.FlagArgument
                          .getOptional(true)
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
                                 commandResult.getParsedArgument("a")).runCommand()
            )
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
            .addDefaultHelpArgument(true)
            .addDefaultHelpSubCommand(true)
            .name("bigdoors")
            .subCommand(addOwner)
            .subCommands(subcommands)
            .commandExecutor(commandResult ->
                                 System.out.print("PARSED A COMMAND: " + commandResult.getCommand().getName()))
//            .hidden(true)
            .build();


        // TODO: Allow specifying the name instead of the subcommand instance?
        //       If the CommandManager becomes a builder, it can just retrieve the instances is the ctor.
        subcommands.forEach(commandManager::addCommand);
        subsubcommands.forEach(commandManager::addCommand);
        commandManager.addCommand(addOwner).addCommand(bigdoors);

        return commandManager;
    }

    private static void testHelpRenderer(final @NonNull CommandManager commandManager)
    {
        final @NonNull Command addOwner = commandManager.getCommand("addowner").orElseThrow(
            () -> new NullPointerException("Could not find command addowner"));
        final @NonNull Command bigdoors = commandManager.getCommand("bigdoors").orElseThrow(
            () -> new NullPointerException("Could not find command bigdoors"));

        {
            DefaultHelpCommandRenderer helpCommand = DefaultHelpCommandRenderer.builder().firstPageSize(2)
                                                                               .pageSize(3)
//                                                               .colorScheme(getColorScheme())
                                                                               .colorScheme(getClearColorScheme())
                                                                               .build();
            System.out.println("==============================");
            System.out.println("==============================");
            System.out.print(helpCommand.renderLongCommand(addOwner));
            System.out.println("==============================");
            System.out.print(helpCommand.renderLongCommand(commandManager.getCommand("subsubcommand_2").get()));
            System.out.println("==============================");
            System.out.println("==============================");

            for (int idx = 0; idx < 12; ++idx)
            {
                final int page = idx;
                tryHelpCommand(() -> helpCommand.render(bigdoors, page));
            }
        }
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
            .commandStyle(new TextComponent(MinecraftStyle.GOLD.getStringValue(),
                                            MinecraftStyle.RESET.getStringValue()))
            .optionalParameterStyle(new TextComponent(MinecraftStyle.BLUE.getStringValue(),
                                                      MinecraftStyle.RESET.getStringValue()))
            .optionalParameterFlagStyle(new TextComponent(MinecraftStyle.LIGHT_PURPLE.getStringValue(),
                                                          MinecraftStyle.RESET.getStringValue()))
            .optionalParameterSeparatorStyle(new TextComponent(MinecraftStyle.WHITE.getStringValue(),
                                                               MinecraftStyle.RESET.getStringValue()))
            .optionalParameterLabelStyle(new TextComponent(MinecraftStyle.GRAY.getStringValue(),
                                                           MinecraftStyle.RESET.getStringValue()))
            .requiredParameterStyle(new TextComponent(MinecraftStyle.RED.getStringValue(),
                                                      MinecraftStyle.RESET.getStringValue()))
            .summaryStyle(new TextComponent(MinecraftStyle.AQUA.getStringValue(),
                                            MinecraftStyle.RESET.getStringValue()))
            .regularTextStyle(new TextComponent(MinecraftStyle.GOLD.getStringValue(),
                                                MinecraftStyle.RESET.getStringValue()))
            .headerStyle(new TextComponent(MinecraftStyle.GREEN.getStringValue(),
                                           MinecraftStyle.RESET.getStringValue()))
            .footerStyle(new TextComponent(MinecraftStyle.DARK_RED.getStringValue(),
                                           MinecraftStyle.RESET.getStringValue()))
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
