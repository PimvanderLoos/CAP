package nl.pim16aap2.commandparser.command;


import lombok.NonNull;

import java.util.Collections;
import java.util.List;

public class AddOwner
{
    private final @NonNull String doorID;
    private final @NonNull List<String> players;
    private final @NonNull List<String> groups;
    private final boolean admin;

    public AddOwner(final @NonNull String doorID, final @NonNull List<String> players,
                    final @NonNull List<String> groups, final boolean admin)
    {
        this.doorID = doorID;
        this.players = players;
        this.groups = groups;
        this.admin = admin;
    }

    public AddOwner(final @NonNull String doorID, final @NonNull String player,
                    final @NonNull String group, final boolean admin)
    {
        this(doorID, Collections.singletonList(player), Collections.singletonList(group), admin);
    }

    public void runCommand()
    {
        System.out.println("\n\n======= AddOwner command! ======= ");
        System.out.println("DoorID: " + doorID);
        System.out.println("Players: " + listToString(players));
        System.out.println("Groups: " + listToString(groups));
        System.out.println("admin: " + admin);
    }

    private static String listToString(final @NonNull List<?> lst)
    {
        final StringBuilder sb = new StringBuilder();
        lst.forEach(val -> sb.append(val.toString()).append(", "));
        final String result = sb.toString();
        return result.substring(0, result.length() - 2);
    }
}


//@Command(name = "addowner", header = "Add another owner to a door.",
//    footer = "Please specify at least 1 player or group.", addMethodSubcommands = false)
//public class AddOwner implements Runnable
//{
//    @CommandLine.Parameters(index = "0", description = "The name or UID of the door.")
//    private String doorID;
//
//    @CommandLine.Option(names = {"-p", "--player"}, description = "The name or UUID of the player to add.")
//    private String[] player;
//
//    @CommandLine.Option(names = {"-g", "--group"}, description = "The name of the group to add.")
//    private String[] group;
//
//    @CommandLine.Option(names = {"-a", "--admin"},
//        description = "Make the user an admin for the given door. Only applies to players.")
//    private boolean admin = false;
//
//    @CommandLine.Spec
//    CommandLine.Model.CommandSpec spec;
//
//    @Override
//    public void run()
//    {
//        System.out.println("AddOwner running!");
//        StringBuilder sb = new StringBuilder();
//        sb.append("Adding players:");
//        for (String playerStr : player)
//            sb.append(" \"").append(playerStr).append("\"");
//        System.out.println(sb.toString());
//    }
//}
