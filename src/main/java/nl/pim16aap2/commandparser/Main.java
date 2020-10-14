package nl.pim16aap2.commandparser;

import lombok.NonNull;
import nl.pim16aap2.commandparser.command.AddOwner;
import nl.pim16aap2.commandparser.commandline.argument.Argument;
import nl.pim16aap2.commandparser.commandline.argument.OptionalArgument;
import nl.pim16aap2.commandparser.commandline.argument.RequiredArgument;
import nl.pim16aap2.commandparser.commandline.command.Command;
import nl.pim16aap2.commandparser.commandline.manager.CommandManager;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;

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

        String[] a = {"bigdoors", "addowner", "myDoor", "-p=pim16aap2", "-a"};

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
                   .argument(
                       RequiredArgument.builder()
                                       .name("doorID")
                                       .summary("The name or UID of the door")
                                       .parser(Argument.ParsedArgument::new)
                                       .build())
                   .argument(OptionalArgument.builder()
                                             .name("a")
                                             .alias("admin")
                                             .summary("Make the user an admin for the given door. " +
                                                          "Only applies to players.")
                                             .parser(str -> new Argument.ParsedArgument(true))
                                             .flag(true)
                                             .defautValue(Boolean.FALSE)
                                             .build())
                   .argument(OptionalArgument.builder()
                                             .name("p")
                                             .summary("The name of the player to add as owner")
                                             .repeatable(true)
                                             .parser(Argument.ParsedArgument::new)
                                             .defautValue("")
                                             .build())
                   .argument(OptionalArgument.builder()
                                             .name("g")
                                             .summary("The name of the group to add as owner")
                                             .repeatable(true)
                                             .parser(Argument.ParsedArgument::new)
                                             .defautValue("")
                                             .build())
                   .commandExecutor(
                       commandResult ->
                       {
                           try
                           {
                               Argument.ParsedArgument<?> doorIdArg = commandResult.getParsedArgument("doorID");
                               String doorId = doorIdArg == null ? null : (String) doorIdArg.getValue();

                               Argument.ParsedArgument<?> playerArg = commandResult.getParsedArgument("p");
                               String player = playerArg == null ? null : (String) playerArg.getValue();

                               Argument.ParsedArgument<?> groupArg = commandResult.getParsedArgument("g");
                               String group = groupArg == null ? null : (String) groupArg.getValue();

                               Argument.ParsedArgument<?> adminArg = commandResult.getParsedArgument("a");
                               Boolean admin = adminArg == null ? Boolean.FALSE : (Boolean) adminArg.getValue();

                               new AddOwner(doorId,
                                            player,
                                            group,
                                            admin).runCommand();

                           }
                           catch (Throwable t)
                           {
                               t.printStackTrace();
                           }
                       }


//                           new AddOwner(commandResult.getParsedArgument("doorID").getValue(),
//                                        commandResult.getParsedArgument(""),
//                                        commandResult.getParsedArgument(""),
//                                        commandResult.getParsedArgument("")
//                           ).runCommand()
//                                        System.out.print("PARSED A COMMAND: " +
//                                                                          commandResult.getCommand().getName())

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
