package nl.pim16aap2.commandparser.text;

import nl.pim16aap2.commandparser.command.Command;

public enum TextType
{
    /**
     * The name(s) of a command.
     */
    COMMAND,

    /**
     * The name of the value of an optional parameter. E.g. For [-p=player] this would be the '[]' part.
     */
    OPTIONAL_PARAMETER,

    /**
     * The name of the value of an optional parameter. E.g. For [-p=player] this would be the 'player' part.
     */
    OPTIONAL_PARAMETER_LABEL,

    /**
     * The name/flag of an optional parameter. E.g. For [-p=player] this would be the '-p' part.
     */
    OPTIONAL_PARAMETER_FLAG,

    /**
     * The separator of an optional parameter. E.g. For [-p=player] this would be the '=' part.
     */
    OPTIONAL_PARAMETER_SEPARATOR,

    /**
     * A required parameter.
     */
    REQUIRED_PARAMETER,

    /**
     * The name of the value of a required parameter. E.g. For <-p=player> this would be the 'player' part.
     */
    REQUIRED_PARAMETER_LABEL,

    /**
     * The name/flag of a required parameter. E.g. For <-p=player> this would be the '-p' part.
     */
    REQUIRED_PARAMETER_FLAG,

    /**
     * The separator of a required parameter. E.g. For <-p=player> this would be the '=' part.
     */
    REQUIRED_PARAMETER_SEPARATOR,

    /**
     * The {@link Command#getSummary()} of a {@link Command}.
     */
    SUMMARY,

    /**
     * The {@link Command#getDescription()} of a {@link Command}.
     */
    DESCRIPTION,

    /**
     * Regular text used for miscellaneous text.
     */
    REGULAR_TEXT,

    /**
     * The {@link Command#getHeader()} ()} of a {@link Command}.
     */
    HEADER,

    /**
     * UNUSED for now.
     */
    FOOTER,
    ;
}
