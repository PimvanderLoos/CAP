package nl.pim16aap2.commandparser;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;
import nl.pim16aap2.commandparser.manager.CommandManager;
import nl.pim16aap2.commandparser.renderer.ColorScheme;
import nl.pim16aap2.commandparser.renderer.TextComponent;
import nl.pim16aap2.commandparser.renderer.TextStyle;
import nl.pim16aap2.commandparser.renderer.TextType;

import java.util.List;

// TODO: Consider using ByteBuddy to generate the commands? Might be completely overkill, though
// TODO: Add some way to inject text into the TextComponent system. For example, when adding a piece of text with
//       a command, store which command it is and if it is listed as a subcommand or a supercommand.
//       Allow registering event handlers or something when a new component is added.
//       This is useful for stuff like clickable text. In Minecraft, this would be the ability to click on a command
//       in a help menu to execute the help command for that command.
// TODO: Rename TextComponent class. The StyledSection class should have this name, while the TextComponent should be
//       something else; it's not a component after all, it's the whole thing.
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

    private static void tryArgs(final @NonNull String... args)
    {
        final String command = arrToString(args);
        System.out.println(command + ":\n");


        System.out.println("=============\n");
    }

    public static void main(final String... args)
    {
        String executor = "mip";

        testTextComponents();

        CommandManager commandManager = initCommandManager();

//        String[] a = {"bigdoors", "addowner", "myDoor", "-p=pim16aap2", "-a"};
        String[] a = {"addowner", "myDoor", "-p=pim16aap2", "-p=pim16aap3", "-p=pim16aap4", "-a"};

        try
        {
            commandManager.parseCommand(a).run();
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
//
//        tryArgs(a);
//        tryArgs(b);
//        tryArgs(c);
//        tryArgs(d);
//        tryArgs(e);
    }

    private static CommandManager initCommandManager()
    {
        final CommandManager commandManager = new CommandManager();

        Command addOwner =
            Command.builder()
                   .name("addowner")
                   .description("Add 1 or more players or groups of players as owners of a door.")
                   .summary("Add another owner to a door.")
                   .argument(Argument.StringArgument
                                 .getRequired()
                                 .name("doorID")
                                 .summary("The name or UID of the door")
                                 .build())
                   .argument(Argument.FlagArgument
                                 .getOptional(true)
                                 .name("a")
                                 .longName("admin")
                                 .summary("Make the user an admin for the given door. " +
                                              "Only applies to players.")
                                 .build())
                   .argument(Argument.StringArgument
                                 .getRepeatable()
                                 .name("p")
                                 .summary("The name of the player to add as owner")
                                 .build())
                   .argument(Argument.StringArgument
                                 .getRepeatable()
                                 .name("g")
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

        Command bigdoors =
            Command.builder()
                   .name("bigdoors")
                   .subCommand(addOwner)
                   .commandExecutor(commandResult -> System.out.print("PARSED A COMMAND: " +
                                                                          commandResult.getCommand().getName()))
                   .build();

        // TODO: Allow specifying the name instead of the subcommand instance?
        //       If the CommandManager becomes a builder, it can just retrieve the instances is the ctor.

        commandManager.addCommand(addOwner);
        commandManager.addCommand(bigdoors);
        return commandManager;
    }

    static void testTextComponents()
    {
        final ColorScheme colorScheme =
            ColorScheme.builder()
                       .commandStyle(new TextStyle(MinecraftStyle.BLACK.getStringValue(),
                                                   MinecraftStyle.RESET.getStringValue()))
                       .optionalParameterStyle(new TextStyle(MinecraftStyle.WHITE.getStringValue(),
                                                             MinecraftStyle.RESET.getStringValue()))
                       .requiredParameterStyle(new TextStyle(MinecraftStyle.GREEN.getStringValue(),
                                                             MinecraftStyle.RESET.getStringValue()))
                       .summaryStyle(new TextStyle(MinecraftStyle.BLUE.getStringValue(),
                                                   MinecraftStyle.RESET.getStringValue()))
                       .regularTextStyle(new TextStyle(MinecraftStyle.GOLD.getStringValue(),
                                                       MinecraftStyle.RESET.getStringValue()))
                       .headerStyle(new TextStyle(MinecraftStyle.AQUA.getStringValue(),
                                                  MinecraftStyle.RESET.getStringValue()))
                       .footerStyle(new TextStyle(MinecraftStyle.DARK_RED.getStringValue(),
                                                  MinecraftStyle.RESET.getStringValue()))
                       .build();

        final TextComponent textComponent = new TextComponent(colorScheme);
        textComponent.add("This is a command", TextType.COMMAND).add("\n")
                     .add("This is an optional parameter", TextType.OPTIONAL_PARAMETER).add("\n")
                     .add("This is something else? I can't remember the types :(", TextType.HEADER).add("\n");


        final TextComponent textComponent2 = new TextComponent(colorScheme);
        textComponent2.add("This is the second TextComponent!", TextType.REQUIRED_PARAMETER).add("\n");

        System.out.println(textComponent.add(textComponent2).toString());
    }
}
