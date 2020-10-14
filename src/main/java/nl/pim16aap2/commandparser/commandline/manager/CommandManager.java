package nl.pim16aap2.commandparser.commandline.manager;

import lombok.NonNull;
import nl.pim16aap2.commandparser.commandline.command.Command;
import nl.pim16aap2.commandparser.commandline.command.CommandResult;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;

public class CommandManager
{
    private final CommandTree commandTree = new CommandTree();

    public CommandManager addCommand(final @NonNull Command command)
    {
        commandTree.addCommand(command);
        return this;
    }

    public @NonNull CommandResult parseCommand(String... cmd)
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException
    {
        return new CommandParser(commandTree, cmd).parse();
    }
}
