package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;

@RequiredArgsConstructor
public class SpigotServerCommandSender implements ICommandSender
{
    protected static final @NonNull ColorScheme EMPTY_COLOR_SCHEME = ColorScheme.builder().build();

    @Override
    public void sendMessage(final @NonNull Text message)
    {
        System.out.println(message.toPlainString());
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
