package nl.pim16aap2.cap.util;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class LocalizationSpecification
{
    private static final @NonNull Locale DEFAULT_LOCALE = Locale.ENGLISH;

    @Getter
    private final @NonNull Locale defaultLocale;
    @Getter
    private final @NonNull String baseName;
    @Getter
    private final @NonNull Locale[] locales;

    public LocalizationSpecification(final @Nullable Locale defaultLocale, final @NonNull String baseName,
                                     final @NonNull Locale... locales)
    {
        this.defaultLocale = Util.valOrDefault(defaultLocale, locales.length > 0 ? locales[0] : DEFAULT_LOCALE);
        this.baseName = baseName;
        this.locales = locales;
    }

    public LocalizationSpecification(final @NonNull String baseName, final @NonNull Locale... locales)
    {
        this(null, baseName, locales);
    }
}
