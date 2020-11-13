package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class UnmatchedQuoteException extends CAPException
{
    private final @NonNull String rawInput;

    public UnmatchedQuoteException(final @NonNull String rawInput, final @NonNull String localizedMessage,
                                   final boolean stacktrace)
    {
        super(localizedMessage, "Found unmatched quotation marks in input: '" + rawInput + "'", stacktrace);
        this.rawInput = rawInput;
    }
}

