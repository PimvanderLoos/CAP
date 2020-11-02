package nl.pim16aap2.cap;

import lombok.NonNull;
import lombok.SneakyThrows;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.DoubleArgument;
import nl.pim16aap2.cap.argument.specialized.IntegerArgument;
import nl.pim16aap2.cap.argument.specialized.StringArgument;
import nl.pim16aap2.cap.argument.validator.number.MaximumValidator;
import nl.pim16aap2.cap.argument.validator.number.MinimumValidator;
import nl.pim16aap2.cap.argument.validator.number.RangeValidator;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.util.GenericCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class CommandParserTest
{
    final DefaultCommandSender commandSender = new DefaultCommandSender();

    private static final List<String> playerNames = new ArrayList<>(
        Arrays.asList("pim16aap2", "pim16aap3", "mip16aap2", "pim"));

    private static final List<String> doorIDs = new ArrayList<>(Arrays.asList("myDoor", "42", "myPortcullis", "84"));

    /**
     * Populates a {@link CAP} with all the commands and arguments.
     *
     * @param cap The {@link CAP} object to populate.
     * @return The same {@link CAP} instance.
     */
    private static @NonNull CAP setUp(final @NonNull CAP cap)
    {
        final int subCommandCount = 20;
        final List<Command> subcommands = new ArrayList<>(subCommandCount);
        for (int idx = 0; idx < subCommandCount; ++idx)
        {
            final String command = "subcommand_" + idx;
            final Command generic = Command
                .commandBuilder().name(command)
                .cap(cap)
                .argument(new StringArgument().getOptional()
                                              .name("value").label("val").summary("random value").build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subcommands.add(generic);
        }

        final Command numerical = Command
            .commandBuilder().name("numerical")
            .cap(cap)
            .argument(new StringArgument().getOptional()
                                          .name("value").label("val").summary("random value").build())
            .argument(new IntegerArgument().getOptional()
                                           .name("min").label("min").summary("Must be more than 10!")
                                           .argumentValidator(MinimumValidator.integerMinimumValidator(10)).build())
            .argument(new IntegerArgument().getOptional()
                                           .name("unbound").label("unbound")
                                           .summary("This value is not bound by anything!")
                                           .build())
            .argument(new IntegerArgument().getOptional()
                                           .name("max").label("max").summary("Must be less than 10!")
                                           .argumentValidator(MaximumValidator.integerMaximumValidator(10)).build())
            .argument(new DoubleArgument().getOptional()
                                          .name("maxd").label("maxd").summary("Must be less than 10.0!")
                                          .argumentValidator(MaximumValidator.doubleMaximumValidator(10.0)).build())
            .argument(new DoubleArgument().getOptional()
                                          .name("range").label("range").summary("Must be between 10 and 20!")
                                          .argumentValidator(RangeValidator.doubleRangeValidator(10, 20)).build())
            .commandExecutor(commandResult ->
                                 new GenericCommand("numerical", commandResult.getParsedArgument("value")).runCommand())
            .build();

        final Command addOwner = Command
            .commandBuilder()
            .cap(cap)
            .name("addowner")
            .addDefaultHelpArgument(true)
            .description("Add 1 or more players or groups of players as owners of a door.")
            .summary("Add another owner to a door.")
            .argument(new StringArgument()
                          .getRequired()
                          .name("doorID")
                          .tabcompleteFunction((commandSender, command, argument) -> doorIDs)
                          .summary("The name or UID of the door")
                          .build())
            .argument(Argument.valuesLessBuilder()
                              .value(true)
                              .name("a")
                              .longName("admin")
                              .summary("Make the user an admin for the given door. Only applies to players.")
                              .build())
            .argument(new StringArgument()
                          .getRepeatable()
                          .name("p")
                          .longName("player")
                          .label("player")
                          .tabcompleteFunction((commandSender, command, argument) -> playerNames)
                          .summary("The name of the player to add as owner")
                          .build())
            .argument(new StringArgument()
                          .getRepeatable()
                          .name("g")
                          .longName("group")
                          .label("group")
                          .summary("The name of the group to add as owner")
                          .build())
            .commandExecutor(CommandResult::sendHelpMenu)
            .build();

        final Command bigdoors = Command
            .commandBuilder()
            .cap(cap)
            .addDefaultHelpSubCommand(true)
            .name("bigdoors")
            .subCommand(addOwner)
            .subCommand(numerical)
            .subCommands(subcommands)
            .commandExecutor(CommandResult::sendHelpMenu)
            .hidden(true)
            .build();

        subcommands.forEach(cap::addCommand);
        cap.addCommand(addOwner).addCommand(bigdoors).addCommand(numerical);

        return cap;
    }

    @Test
    void getCommandTabCompleteOptions()
    {
        final @NonNull CAP cap = setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build());

        Assertions.assertEquals(20, cap.getTabCompleteOptions(commandSender, "bigdoors sub").size());

        Assertions.assertEquals(23, cap.getTabCompleteOptions(commandSender, "bigdoors").size());

        Assertions.assertEquals("bigdoors", cap.getTabCompleteOptions(commandSender, "big").get(0));

        Assertions.assertEquals(0, cap.getTabCompleteOptions(commandSender, "sub").size());
    }

    @Test
    void getArgumentNameTabCompleteOptions()
    {
        final @NonNull CAP cap = setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build());

        final List<String> playerSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner -p");
        Assertions.assertEquals(2, playerSuggestions.size());
        Assertions.assertEquals("-p=", playerSuggestions.get(0));
        Assertions.assertEquals("--player=", playerSuggestions.get(1));

        final List<String> longPlayerSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner --pla");
        Assertions.assertEquals(1, longPlayerSuggestions.size());
        Assertions.assertEquals("--player=", longPlayerSuggestions.get(0));

        final List<String> adminSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner --a");
        Assertions.assertEquals(2, adminSuggestions.size());
        Assertions.assertEquals("-a", adminSuggestions.get(0));
        Assertions.assertEquals("--admin", adminSuggestions.get(1));

        final List<String> suggestionsForSubcommand =
            cap.getTabCompleteOptions(commandSender, "bigdoors subcommand_0");
        Assertions.assertEquals(1, suggestionsForSubcommand.size());
        Assertions.assertEquals("value", suggestionsForSubcommand.get(0));

        final List<String> suggestionsHelpCommand =
            cap.getTabCompleteOptions(commandSender, "bigdoors help subcommand_1");
        Assertions.assertEquals(11, suggestionsHelpCommand.size());
    }

    private void getFreeArgumentValueTabCompleteOptions(final @NonNull CAP cap)
    {
        List<String> playerNameSuggestions =
            cap.getTabCompleteOptions(commandSender, String.format("bigdoors addowner -p%c", cap.getSeparator()));
        Assertions.assertEquals(4, playerNameSuggestions.size());

        playerNameSuggestions = cap
            .getTabCompleteOptions(commandSender, String.format("bigdoors addowner -p%cpim", cap.getSeparator()));
        Assertions.assertEquals(3, playerNameSuggestions.size());

        playerNameSuggestions = cap
            .getTabCompleteOptions(commandSender, String.format("bigdoors addowner -p%cpim16", cap.getSeparator()));
        Assertions.assertEquals(2, playerNameSuggestions.size());
        Assertions.assertEquals("pim16aap2", playerNameSuggestions.get(0));
        Assertions.assertEquals("pim16aap3", playerNameSuggestions.get(1));
    }

    @Test
    void getFreeArgumentValueTabCompleteOptions()
    {
        getFreeArgumentValueTabCompleteOptions(
            setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build()));
    }

    @Test
    void getFreeArgumentValueTabCompleteOptionsSpaceSeparator()
    {
        getFreeArgumentValueTabCompleteOptions(
            setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').build()));
    }

    @Test
    void getPositionalValueTabCompleteOptions()
    {
        final @NonNull CAP cap = setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build());

        List<String> doorIDSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner 4");

        Assertions.assertEquals(1, doorIDSuggestions.size());
        Assertions.assertEquals("42", doorIDSuggestions.get(0));

        doorIDSuggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner my");
        Assertions.assertEquals(2, doorIDSuggestions.size());
        Assertions.assertEquals("myDoor", doorIDSuggestions.get(0));
        Assertions.assertEquals("myPortcullis", doorIDSuggestions.get(1));

        doorIDSuggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner");
        Assertions.assertEquals(4, doorIDSuggestions.size());
        Assertions.assertEquals("myDoor", doorIDSuggestions.get(0));
        Assertions.assertEquals("42", doorIDSuggestions.get(1));
    }

    @Test
    void testLstripArgumentPrefix()
    {
        Assertions.assertFalse(CommandParser.lstripArgumentPrefix("admin--").isPresent());
        Assertions.assertFalse(CommandParser.lstripArgumentPrefix("admin-").isPresent());
        Assertions.assertFalse(CommandParser.lstripArgumentPrefix("admin").isPresent());

        Assertions.assertTrue(CommandParser.lstripArgumentPrefix("-admin").isPresent());
        Assertions.assertTrue(CommandParser.lstripArgumentPrefix("--admin").isPresent());

        Assertions.assertEquals("-admin", CommandParser.lstripArgumentPrefix("---admin").get());
    }

    /**
     * Asserts that the runnable throws an exception of the specified type.
     * <p>
     * Contrary to the regular Assertions.assertThrows, this method will catch a {@link RuntimeException} and check its
     * cause.
     *
     * @param clazz      The type of the exception expected inside the {@link RuntimeException}.
     * @param executable The method to execute.
     */
    @SneakyThrows
    private static void assertWrappedThrows(final @NonNull Class<?> clazz, final @NonNull Executable executable)
    {
        final RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class, executable);
        Assertions.assertNotNull(runtimeException.getCause());

        if (!clazz.isInstance(runtimeException.getCause()))
            Assertions.fail("Exception " + runtimeException.getCause().getClass().getCanonicalName() +
                                " is not of expected type: " + clazz.getCanonicalName());
    }

    @SneakyThrows
    void testNumericalInput(final @NonNull CAP cap)
    {
        final char sep = cap.separator;
        // My name is not numerical.
        assertWrappedThrows(IllegalValueException.class,
                            () -> cap
                                .parseInput(commandSender, String.format("bigdoors numerical -max%cpim16aap2", sep)));
        // The max value is set to 10, so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -max%c10", sep)));
        // With a max value of 10, 9 is perfect!
        Assertions
            .assertDoesNotThrow(() -> cap.parseInput(commandSender, String.format("bigdoors numerical -max%c9", sep)));


        // The maxd value is set to 10.0, so 10.0 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -maxd%c10.0", sep)));
        // With a maxd value of 10.0, 9.9 is perfect!
        Assertions
            .assertDoesNotThrow(
                () -> cap.parseInput(commandSender, String.format("bigdoors numerical -maxd%c9.9", sep)));


        // The min value is set to 10, so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -min%c10", sep)));
        // With a min value of 10, 11 is perfect!
        Assertions
            .assertDoesNotThrow(() -> cap.parseInput(commandSender, String.format("bigdoors numerical -min%c11", sep)));

        // The range is set to [10, 20], so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -range%c10", sep)));
        // The range is set to [10, 20], so 20 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -range%c20", sep)));
        // With a range of [10, 20], 11 is perfect!
        Assertions.assertDoesNotThrow(
            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -range%c11", sep)));

        // Let's give negative values some love.
        Assertions.assertDoesNotThrow(
            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -unbound%c-9", sep)));
        final Optional<CommandResult> result = cap
            .parseInput(commandSender, String.format("bigdoors numerical -unbound%c-9", sep));
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(result.get().<Integer>getParsedArgument("unbound"), -9);
    }

    @Test
    void testNumericalInput()
    {
        testNumericalInput(setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build()));
    }

    @Test
    void testNumericalInputSpaceSeparator()
    {
        testNumericalInput(setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').build()));
    }

    @Test
    void testSpaceSeparator()
    {
        final @NonNull CAP cap = setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').build());
        Assertions.assertDoesNotThrow(() -> cap.parseInput(commandSender, "bigdoors numerical -max 9"));
        Assertions.assertDoesNotThrow(() -> cap
            .parseInput(commandSender, "bigdoors addowner mydoor --player pim16aap2 --group \"group 1\" --admin"));
        Assertions.assertDoesNotThrow(() -> cap
            .parseInput(commandSender, "bigdoors addowner mydoor --player pim16aap2 --group \"group 1\""));
        Assertions.assertDoesNotThrow(() -> cap
            .parseInput(commandSender, "bigdoors addowner mydoor --player pim16aap2"));
    }
}
