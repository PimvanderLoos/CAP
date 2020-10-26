package nl.pim16aap2.cap.exception;

public abstract class CommandParserException extends Exception
{
    protected final boolean stacktraceEnabled;

    protected CommandParserException(final boolean stacktraceEnabled)
    {
        this.stacktraceEnabled = stacktraceEnabled;
        fillInOptionalStackTrace();
    }

    public CommandParserException(String message, final boolean stacktraceEnabled)
    {
        super(message);
        this.stacktraceEnabled = stacktraceEnabled;
        fillInOptionalStackTrace();
    }

    public CommandParserException(String message, Throwable cause, final boolean stacktraceEnabled)
    {
        super(message, cause);
        this.stacktraceEnabled = stacktraceEnabled;
        fillInOptionalStackTrace();
    }

    public CommandParserException(Throwable cause, final boolean stacktraceEnabled)
    {
        super(cause);
        this.stacktraceEnabled = stacktraceEnabled;
        fillInOptionalStackTrace();
    }

    protected CommandParserException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
        stacktraceEnabled = writableStackTrace;
        fillInOptionalStackTrace();
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
