package nl.pim16aap2.commandparser;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.command.DefaultHelpCommand;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.IllegalValueException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;
import nl.pim16aap2.commandparser.manager.CommandManager;
import nl.pim16aap2.commandparser.renderer.ColorScheme;
import nl.pim16aap2.commandparser.renderer.TextComponent;
import nl.pim16aap2.commandparser.renderer.TextStyle;
import nl.pim16aap2.commandparser.renderer.TextType;

import java.util.ArrayList;
import java.util.List;

// TODO: Consider using ByteBuddy to generate the commands? Might be completely overkill, though
// TODO: Add some way to inject text into the TextComponent system. For example, when adding a piece of text with
//       a command, store which command it is and if it is listed as a subcommand or a supercommand.
//       Allow registering event handlers or something when a new component is added.
//       This is useful for stuff like clickable text. In Minecraft, this would be the ability to click on a command
//       in a help menu to execute the help command for that command.
// TODO: Rename TextComponent class. The StyledSection class should have this name, while the TextComponent should be
//       something else; it's not a component after all, it's the whole thing.
// TODO: Allow positional optional arguments. Perhaps Required and OptionalPositional can both extend a Positional interface?
//       Maybe also extend a FlaggedArgument interface for stuff that does require flags?
// TODO: Allow using space as a separator of flag-value pairs.
// TODO: Make a class somewhere where you can register ColorScheme objects. This class can then be used for caching
//       stuff like finished TextComponents etc.
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

        System.out.println("=============\n");
    }

    public static void main(final String... args)
    {
        String executor = "mip";

        testTextComponents();

        CommandManager commandManager = initCommandManager();

        final ColorScheme colorScheme = getColorScheme();
        DefaultHelpCommand.builder().colorScheme(colorScheme).pageSize(16).firstPageSize(1).build();

        String[] a = {"bigdoors", "addowner", "addowner", "myDoor", "-p=pim16aap2", "-a"};
//        String[] b = {"addowner", "myDoor", "-p=pim16aap2", "-p=pim16aap3", "-p=pim16aap4", "-a"};
//        String[] c = {"bigdoors", "help", "addowner"};
//        String[] c = {"bigdoors", "help"};
//        String[] f = {"help"};

        tryArgs(commandManager, a);
//        tryArgs(commandManager, b);
//        tryArgs(commandManager, c);
//        tryArgs(commandManager, d);
//        tryArgs(commandManager, e);
//        tryArgs(commandManager, f);


//        String[] a = {"bigdoors", "addowner", "myDoor", "-p", "pim16aap2", "--admin"};
//        String[] b = {"bigdoors", "help", "addowner"};
//        String[] c = {"addowner", "myDoor", "-p ", "pim16aap2", "--admin"};
//        String[] d = {"addowner", "myDoor", "-p ", "pim16aap2", "-p ", "pim16aap3", "--admin"};
//        String[] e = {"help"};


//        String[] a = {"bigdoors", "addowner", "myDoor", "-p", "pim16aap2", "--admin"};
//        String[] b = {"bigdoors", "help", "addowner"};
//        String[] c = {"addowner", "myDoor", "-p ", "pim16aap2", "--admin"};
//        String[] d = {"addowner", "myDoor", "-p ", "pim16aap2", "-p ", "pim16aap3", "--admin"};
//        String[] e = {"help"};
    }

    private static CommandManager initCommandManager()
    {
        final CommandManager commandManager = new CommandManager();

        final int subsubCommandCount = 5;
        final List<Command> subsubcommands = new ArrayList<>(subsubCommandCount);
        for (int idx = 0; idx < subsubCommandCount; ++idx)
        {
            final String command = "subsubcommand_" + idx;
            final Command generic = Command
                .builder().name(command)
                .commandManager(commandManager)
                .summary("This is the summary for subsubcommand_" + idx)
                .argument(Argument.StringArgument.getRequired().name("value").summary("random value").build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subsubcommands.add(generic);
        }

        final Command addOwner = Command
            .builder()
            .commandManager(commandManager)
            .name("addowner")
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
                .builder().name(command)
                .commandManager(commandManager)
                .argument(Argument.StringArgument.getRequired().name("value").summary("random value").build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subcommands.add(generic);
        }

        final Command bigdoors = Command
            .builder()
            .commandManager(commandManager)
            .name("bigdoors")
            .subCommand(addOwner)
            .subCommands(subcommands)
            .commandExecutor(commandResult ->
                                 System.out.print("PARSED A COMMAND: " + commandResult.getCommand().getName()))
            .hidden(true)
            .build();


        // TODO: Allow specifying the name instead of the subcommand instance?
        //       If the CommandManager becomes a builder, it can just retrieve the instances is the ctor.
        subcommands.forEach(commandManager::addCommand);
        subsubcommands.forEach(commandManager::addCommand);
        commandManager.addCommand(addOwner).addCommand(bigdoors);


        {
            DefaultHelpCommand helpCommand = DefaultHelpCommand.builder().firstPageSize(2)
                                                               .pageSize(3)
                                                               .colorScheme(getColorScheme())
//                                                               .colorScheme(getClearColorScheme())
                                                               .build();
            System.out.print(helpCommand.renderLongCommand(addOwner));
            helpCommand.renderLongCommand(commandManager.getCommand("subsubcommand_2").get());

            for (int idx = 0; idx < 20; ++idx)
            {
                final int page = idx;
                tryHelpCommand(() -> helpCommand.render(bigdoors, page));
            }
        }
        System.exit(0);


        return commandManager;
    }

    @FunctionalInterface
    private interface CheckedSupplier<T, E extends Exception>
    {
        T get()
            throws E;
    }

    private static void tryHelpCommand(CheckedSupplier<TextComponent, IllegalValueException> sup)
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
            .commandStyle(new TextStyle(MinecraftStyle.GOLD.getStringValue(),
                                        MinecraftStyle.RESET.getStringValue()))
            .optionalParameterStyle(new TextStyle(MinecraftStyle.BLUE.getStringValue(),
                                                  MinecraftStyle.RESET.getStringValue()))
            .optionalParameterFlagStyle(new TextStyle(MinecraftStyle.LIGHT_PURPLE.getStringValue(),
                                                      MinecraftStyle.RESET.getStringValue()))
            .optionalParameterSeparatorStyle(new TextStyle(MinecraftStyle.WHITE.getStringValue(),
                                                           MinecraftStyle.RESET.getStringValue()))
            .optionalParameterLabelStyle(new TextStyle(MinecraftStyle.GRAY.getStringValue(),
                                                       MinecraftStyle.RESET.getStringValue()))
            .requiredParameterStyle(new TextStyle(MinecraftStyle.RED.getStringValue(),
                                                  MinecraftStyle.RESET.getStringValue()))
            .summaryStyle(new TextStyle(MinecraftStyle.AQUA.getStringValue(),
                                        MinecraftStyle.RESET.getStringValue()))
            .regularTextStyle(new TextStyle(MinecraftStyle.GOLD.getStringValue(),
                                            MinecraftStyle.RESET.getStringValue()))
            .headerStyle(new TextStyle(MinecraftStyle.GREEN.getStringValue(),
                                       MinecraftStyle.RESET.getStringValue()))
            .footerStyle(new TextStyle(MinecraftStyle.DARK_RED.getStringValue(),
                                       MinecraftStyle.RESET.getStringValue()))
            .build();
    }

    static void testTextComponents()
    {
        final ColorScheme colorScheme = getColorScheme();

        {
            final TextComponent textComponent = new TextComponent(colorScheme);
            textComponent.add("Unstyled text!");
            System.out.println(textComponent);
            System.out.println(textComponent.add(textComponent));
        }

        final TextComponent textComponent = new TextComponent(colorScheme);
        textComponent.add("This is a command", TextType.COMMAND).add("\n")
                     .add("This is an optional parameter", TextType.OPTIONAL_PARAMETER).add("\n")
                     .add("This is something else? I can't remember the types :(", TextType.HEADER).add("\n");


        final TextComponent textComponent2 = new TextComponent(colorScheme);
        textComponent2.add("This is the second TextComponent!", TextType.REQUIRED_PARAMETER).add("\n");
        textComponent2.add("This is some unstyled text!").add("\n");

        System.out.println(textComponent.add(textComponent2).toString());
    }
}
