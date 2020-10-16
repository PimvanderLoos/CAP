package nl.pim16aap2.commandparser;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum MinecraftStyle
{
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    OBFUSCATED('k'),
    BOLD('l'),
    STRIKETHROUGH('m'),
    UNDERLINE('n'),
    ITALIC('o'),
    RESET('r'),
    ;

    public static final char COLOR_CHAR = '\u00A7';

    @Getter
    private final char code;

    @Getter
    private final @NotNull String stringValue;

    MinecraftStyle(final char code)
    {
        this.code = code;
        stringValue = new String(new char[]{COLOR_CHAR, code});
    }
}
