package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class UnmatchedQuoteException extends CAPException
{
    private final String rawInput;

    public UnmatchedQuoteException(final @NonNull String rawInput, final boolean stacktrace)
    {
        super("Found unmatched quotation marks in input: '" + rawInput + "'", stacktrace);
        this.rawInput = rawInput;
    }
}

