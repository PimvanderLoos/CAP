package nl.pim16aap2.commandparser;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;
import nl.pim16aap2.commandparser.manager.CommandManager;

import java.util.List;

// TODO: Consider using ByteBuddy to generate the commands?
//       Might be completely overkill, though
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
            System.out.println("Failed to find argument: " + e.getNonExistingArgument());
            e.printStackTrace();
        }
        catch (MissingArgumentException e)
        {
            System.out.println("Failed to find value for argument: " + e.getMissingArgument());
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
                                 .getRepeating()
                                 .name("p")
                                 .summary("The name of the player to add as owner")
                                 .build())
                   .argument(Argument.StringArgument
                                 .getRepeating()
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
}
