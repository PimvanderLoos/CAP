package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.text.ColorScheme;

public class SpigotServerCommandSender implements ICommandSender
{
    protected static final @NonNull ColorScheme EMPTY_COLOR_SCHEME = ColorScheme.builder().build();

    @Override
    public void sendMessage(final @NonNull String message)
    {
        System.out.println(message);
    }

    @Override
    public boolean hasPermission(final @NonNull Command command)
    {
        return true;
    }

    @Override
    public @NonNull ColorScheme getColorScheme()
    {
        return EMPTY_COLOR_SCHEME;
    }
}