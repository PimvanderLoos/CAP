package nl.pim16aap2.commandparser.exception;

public abstract class CommandParserException extends Exception
{
    protected final boolean stacktraceEnabled;

    protected CommandParserException(final boolean stacktraceEnabled)
    {
        this.stacktraceEnabled = stacktraceEnabled;
        this.fillInOptionalStackTrace();
    }

    public CommandParserException(String message, final boolean stacktraceEnabled)
    {
        super(message);
        this.stacktraceEnabled = stacktraceEnabled;
        this.fillInOptionalStackTrace();
    }

    public CommandParserException(String var1, Throwable var2, final boolean stacktraceEnabled)
    {
        super(var1, var2);
        this.stacktraceEnabled = stacktraceEnabled;
        this.fillInOptionalStackTrace();
    }

    public CommandParserException(Throwable var1, final boolean stacktraceEnabled)
    {
        super(var1);
        this.stacktraceEnabled = stacktraceEnabled;
        this.fillInOptionalStackTrace();
    }

    protected CommandParserException(String var1, Throwable var2, boolean var3, boolean var4)
    {
        super(var1, var2, var3, var4);
        stacktraceEnabled = var4;
        this.fillInOptionalStackTrace();
    }

    private void fillInOptionalStackTrace()
    {
        if (!stacktraceEnabled)
            return;
        super.fillInStackTrace();
    }

    @Override
    public Throwable fillInStackTrace()
    {
        return null;
    }
}
