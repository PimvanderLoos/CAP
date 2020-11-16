package nl.pim16aap2.cap.renderer;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.DefaultHelpCommand;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.localization.Localizer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.SpigotTextUtility;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the default implementation of {@link IHelpCommandRenderer}.
 *
 * @author Pim
 */
@SuperBuilder(toBuilder = true)
public class SpigotHelpCommandRenderer extends DefaultHelpCommandRenderer
{
    protected static final @NonNull String DEFAULT_NEXT_PAGE_NAME = "Click me for more information!";
    protected static final @NonNull String DEFAULT_NEXT_PAGE_NAME_LOCALIZED = "command.help.clickable.nextPageHover";

    protected static final @NonNull String DEFAULT_PREVIOUS_PAGE_NAME = "Click me for more information!";
    protected static final @NonNull String DEFAULT_PREVIOUS_PAGE_NAME_LOCALIZED = "command.help.clickable.previousPageHover";

    protected static final @NonNull String DEFAULT_SUB_HOVER_NAME = "Click me for more information!";
    protected static final @NonNull String DEFAULT_SUB_HOVER_LOCALIZED = "command.help.clickable.subCommandHover";

    /**
     * The name of the next-page hover message.
     * <p>
     * When null, this will be either {@link #DEFAULT_NEXT_PAGE_NAME} or {@link #DEFAULT_NEXT_PAGE_NAME_LOCALIZED}
     * depending on {@link CAP#localizationEnabled()}.
     */
    @Builder.Default
    private final @Nullable String nextPageHoverName = null;

    /**
     * The name of the previous-page hover message.
     * <p>
     * When null, this will be either {@link #DEFAULT_PREVIOUS_PAGE_NAME} or {@link
     * #DEFAULT_PREVIOUS_PAGE_NAME_LOCALIZED} depending on {@link CAP#localizationEnabled()}.
     */
    @Builder.Default
    private final @Nullable String previousPageHoverName = null;

    /**
     * The name of the message to display when hovering over the help message for a sub-command (that takes you to the
     * help menu for that command when clicked).
     * <p>
     * When null, this will be either {@link #DEFAULT_SUB_HOVER_NAME} or {@link #DEFAULT_SUB_HOVER_LOCALIZED} depending
     * on {@link CAP#localizationEnabled()}.
     */
    @Builder.Default
    private final @Nullable String subCommandHoverName = null;

    /**
     * Gets a new instance of this {@link SpigotHelpCommandRenderer} using the default values.
     * <p>
     * Use {@link SpigotHelpCommandRenderer#toBuilder()} if you wish to customize it.
     *
     * @return A new instance of this {@link SpigotHelpCommandRenderer}.
     */
    public static @NonNull SpigotHelpCommandRenderer getDefault()
    {
        return SpigotHelpCommandRenderer.builder().build();
    }

    @Override
    protected void renderHelpHeader(final @NonNull ICommandSender commandSender, final @NonNull Command command,
                                    final @NonNull Text text)
    {
        text.add(" ", TextType.REGULAR_TEXT)
            .add("\n--- " + command.getSectionTitle(commandSender) + " ---\n", TextType.SECTION);
    }

    @Override
    protected void renderPageCountHeader(final @NonNull ICommandSender commandSender, final @NonNull Text text,
                                         final int page, final int pageCount, @NonNull Command command)
    {
        final @NonNull Localizer localizer = command.getCap().getLocalizer();
        @Nullable Command helpCommand = command.getHelpCommand();
        if (helpCommand == null)
        {
            // If there is no help command in sight, we cannot provide a link to the help command for this command.
            // When that's the case, just let the default renderer take care of the header.
            if (!(command instanceof DefaultHelpCommand))
            {
                super.renderPageCountHeader(commandSender, text, page, pageCount, command);
                return;
            }

            // If the command is a help command, it has to have a supercommand (otherwise, whose help command is it?).
            // So, we'll have a command/helpcommand after all.
            helpCommand = command;
            command = command.getSuperCommand().orElseThrow(
                () -> new RuntimeException("Failed to find supercommand of help command!"));
        }

        if (page == 1)
            text.add("---", TextType.REGULAR_TEXT);
        else
        {
            final @NonNull String localizedPreviousPage = getLocalizedMessage(command.getCap(), commandSender,
                                                                              previousPageHoverName,
                                                                              DEFAULT_PREVIOUS_PAGE_NAME,
                                                                              DEFAULT_PREVIOUS_PAGE_NAME_LOCALIZED);
            SpigotTextUtility.addClickableCommandText(text, "<<<", TextType.COMMAND, String
                                                          .format("/%s %s %d", command.getName(commandSender.getLocale()),
                                                                  helpCommand.getName(commandSender.getLocale()), page - 1),
                                                      "§c" + localizedPreviousPage);
        }

        final @NonNull String localizedPageName = getLocalizedMessage(command.getCap(), commandSender, pageName,
                                                                      DEFAULT_PAGE_NAME, DEFAULT_PAGE_NAME_LOCALIZED);
        text.add(String.format("----- %s (%2d / %2d) -----",
                               localizedPageName, page, pageCount), TextType.REGULAR_TEXT);

        if (page == pageCount)
            text.add("---", TextType.REGULAR_TEXT);
        else
        {
            final @NonNull String localizedNextPage = getLocalizedMessage(command.getCap(), commandSender,
                                                                          nextPageHoverName,
                                                                          DEFAULT_NEXT_PAGE_NAME,
                                                                          DEFAULT_NEXT_PAGE_NAME_LOCALIZED);
            SpigotTextUtility.addClickableCommandText(text, ">>>", TextType.COMMAND, String
                .format("/%s %s %d", command.getName(commandSender.getLocale()),
                        helpCommand.getName(commandSender.getLocale()), page + 1), "§c" + localizedNextPage);
        }

        text.add("\n");
    }

    @Override
    protected void renderCommand(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                 final @NonNull Text text, final @NonNull Command command,
                                 final @NonNull String superCommands)
    {
        final @NonNull Localizer localizer = command.getCap().getLocalizer();
        final @NonNull Command topCommand = command.getTopLevelCommand();
        final @Nullable Command helpCommand = topCommand.getHelpCommand();

        final @NonNull Text commandText = new Text(text.getColorScheme());

        commandText.add(superCommands + command.getName(commandSender.getLocale()), TextType.COMMAND);
        renderArgumentsShort(commandSender.getLocale(), colorScheme, commandText, command);

        if (helpCommand != null)
        {
            final String commandName =
                command.getName(commandSender.getLocale()).equals(helpCommand.getName(commandSender.getLocale())) ? "" :
                " " + command.getName(commandSender.getLocale());

            final String clickableCommand =
                "/" + topCommand.getName(commandSender.getLocale()) + " " +
                    helpCommand.getName(commandSender.getLocale()) + commandName;

            final @NonNull String hoverMessage = getLocalizedMessage(command.getCap(), commandSender,
                                                                     subCommandHoverName, DEFAULT_SUB_HOVER_NAME,
                                                                     DEFAULT_SUB_HOVER_LOCALIZED);

            SpigotTextUtility.makeClickableCommand(commandText, clickableCommand, "§c" + // TODO: Use ColorScheme!
                localizer.getMessage(hoverMessage, commandSender.getLocale()));
        }
        text.add(commandText);

        if (!command.getSummary(commandSender).equals(""))
            text.add("\n").add(descriptionIndent).add(command.getSummary(commandSender), TextType.SUMMARY);
    }
}
