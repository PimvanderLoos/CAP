package nl.pim16aap2.cap;


import lombok.NonNull;

public class GenericCommand
{
    private final @NonNull String value;
    private final @NonNull String commandName;

    public GenericCommand(final @NonNull String commandName, final @NonNull String value)
    {
        this.value = value;
        this.commandName = commandName;
    }

    public void runCommand()
    {
        System.out.println("\n\n======= " + commandName + " command! ======= ");
        System.out.println("value: " + value);
    }
}
