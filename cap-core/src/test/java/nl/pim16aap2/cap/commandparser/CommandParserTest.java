package nl.pim16aap2.cap.commandparser;

import lombok.NonNull;
import lombok.SneakyThrows;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.DoubleArgument;
import nl.pim16aap2.cap.argument.specialized.IntegerArgument;
import nl.pim16aap2.cap.argument.specialized.StringArgument;
import nl.pim16aap2.cap.argument.validator.number.MaximumValidator;
import nl.pim16aap2.cap.argument.validator.number.MinimumValidator;
import nl.pim16aap2.cap.argument.validator.number.RangeValidator;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.command.DefaultHelpCommand;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.MissingValueException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.util.GenericCommand;
import nl.pim16aap2.cap.util.UtilsForTesting;
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

    private static final List<String> doorIDs = new ArrayList<>(Arrays.asList("myDoor", "42", "my Portcullis", "84"));

    /**
     * Populates a {@link CAP} with all the commands and arguments.
     *
     * @param cap The {@link CAP} object to populate.
     * @return The same {@link CAP} instance.
     */
    public static @NonNull CAP setUp(final @NonNull CAP cap)
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
                                              .shortName("v").identifier("value").label("val").summary("random value")
                                              .build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subcommands.add(generic);
        }

        final Command numerical = Command
            .commandBuilder().name("numerical")
            .cap(cap)
            .argument(new StringArgument().getOptional()
                                          .shortName("value").label("val").identifier("value").summary("random value")
                                          .build())
            .argument(new IntegerArgument().getOptional()
                                           .shortName("min").label("min").identifier("min")
                                           .summary("Must be more than 10!")
                                           .argumentValidator(MinimumValidator.integerMinimumValidator(10)).build())
            .argument(new IntegerArgument().getOptional()
                                           .shortName("unbound").label("unbound").identifier("unbound")
                                           .summary("This value is not bound by anything!")
                                           .build())
            .argument(new IntegerArgument().getOptional()
                                           .shortName("max").label("max").identifier("max")
                                           .summary("Must be less than 10!")
                                           .argumentValidator(MaximumValidator.integerMaximumValidator(10)).build())
            .argument(new DoubleArgument().getOptional()
                                          .shortName("maxd").label("maxd").identifier("maxd")
                                          .summary("Must be less than 10.0!")
                                          .argumentValidator(MaximumValidator.doubleMaximumValidator(10.0)).build())
            .argument(new IntegerArgument().getOptional()
                                           .shortName("range").label("range").identifier("range")
                                           .summary("Must be between 10 and 20!")
                                           .argumentValidator(RangeValidator.integerRangeValidator(10, 20)).build())
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
                          .shortName("doorID")
                          .identifier("doorID")
                          .tabCompleteFunction((request) -> doorIDs)
                          .summary("The name or UID of the door")
                          .build())
            .argument(Argument.valuesLessBuilder()
                              .value(true)
                              .shortName("a")
                              .longName("admin")
                              .identifier("admin")
                              .summary("Make the user an admin for the given door. Only applies to players.")
                              .build())
            .argument(new StringArgument()
                          .getRepeatable()
                          .shortName("p")
                          .longName("player")
                          .identifier("player")
                          .label("player")
                          .tabCompleteFunction((request) -> playerNames)
                          .summary("The name of the player to add as owner")
                          .build())
            .argument(new StringArgument()
                          .getRepeatable()
                          .shortName("g")
                          .longName("group")
                          .identifier("group")
                          .label("player")
                          .summary("The name of the group to add as owner")
                          .build())
            .commandExecutor(CommandResult::sendHelpMenu)
            .build();

        final Command bigdoors = Command
            .commandBuilder()
            .cap(cap)
            .helpCommand(DefaultHelpCommand.getDefault(cap).toBuilder().name("help").build())
            .name("bigdoors")
            .subCommand(addOwner)
            .subCommand(numerical)
            .subCommands(subcommands)
            .virtual(true)
            .build();

        subcommands.forEach(cap::addCommand);
        cap.addCommand(addOwner).addCommand(bigdoors).addCommand(numerical);

        return cap;
    }

    @Test
    void testLstripArgumentPrefix()
    {
        Assertions.assertFalse(CommandParser.lStripArgumentPrefix("admin--").isPresent());
        Assertions.assertFalse(CommandParser.lStripArgumentPrefix("admin-").isPresent());
        Assertions.assertFalse(CommandParser.lStripArgumentPrefix("admin").isPresent());

        Assertions.assertTrue(CommandParser.lStripArgumentPrefix("-admin").isPresent());
        Assertions.assertTrue(CommandParser.lStripArgumentPrefix("--admin").isPresent());

        UtilsForTesting.optionalEquals(CommandParser.lStripArgumentPrefix("---admin"), "-admin");
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

    private <T> void assertParseResult(final @NonNull CAP cap, final @NonNull String command,
                                       final @NonNull String identifier, final @NonNull T expected)
    {
        final @NonNull Optional<CommandResult> result = Assertions
            .assertDoesNotThrow(() -> cap.parseInput(commandSender, command));

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expected, result.get().getParsedArgument(identifier));
    }

    private void testInvalidInput(final @NonNull CAP cap, final char sep)
    {
        assertWrappedThrows(MissingValueException.class, () -> cap
            .parseInput(commandSender,
                        String.format("bigdoors addowner mydoor --admin --group --player%cpim16aap2", sep)));

        assertWrappedThrows(MissingValueException.class, () -> cap
            .parseInput(commandSender, String.format("bigdoors addowner mydoor --admin --group%c --player%cpim16aap2",
                                                     sep, sep)));

        assertWrappedThrows(MissingValueException.class, () -> cap
            .parseInput(commandSender, String.format("bigdoors addowner mydoor --admin --player%c ", sep)));

        assertWrappedThrows(MissingValueException.class, () -> cap
            .parseInput(commandSender, String.format("bigdoors addowner mydoor --admin --player%c", sep)));

        assertWrappedThrows(MissingValueException.class, () -> cap
            .parseInput(commandSender, "bigdoors addowner mydoor --admin --player"));
    }

    @Test
    void testInvalidInput()
    {
        testInvalidInput(setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build()), '=');
    }

    @Test
    void testInvalidInputSpaceSeparator()
    {
        testInvalidInput(setUp(CAP.getDefault().toBuilder().exceptionHandler(null).build()), ' ');
    }

    @SneakyThrows
    void testNumericalInput(final @NonNull CAP cap)
    {
        final char sep = cap.getSeparator();
        // My name is not numerical.
        assertWrappedThrows(IllegalValueException.class,
                            () -> cap
                                .parseInput(commandSender, String.format("bigdoors numerical -max%cpim16aap2", sep)));
        // The max value is set to 10, so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -max%c10", sep)));
        // With a max value of 10, 9 is perfect!
        assertParseResult(cap, String.format("bigdoors numerical -max%c9", sep), "max", 9);


        // The maxd value is set to 10.0, so 10.0 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -maxd%c10.0", sep)));
        // With a maxd value of 10.0, 9.9 is perfect!
        assertParseResult(cap, String.format("bigdoors numerical -maxd%c9.9", sep), "maxd", 9.9);


        // The min value is set to 10, so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -min%c10", sep)));
        // With a min value of 10, 11 is perfect!
        assertParseResult(cap, String.format("bigdoors numerical -min%c11", sep), "min", 11);


        // The range is set to [10, 20], so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -range%c10", sep)));
        // The range is set to [10, 20], so 20 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -range%c20", sep)));
        // With a range of [10, 20], 11 is perfect!
        assertParseResult(cap, String.format("bigdoors numerical -range%c11", sep), "range", 11);


        assertParseResult(cap, String.format("bigdoors numerical -unbound%c-9", sep), "unbound", -9);
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
        assertParseResult(cap, "bigdoors numerical -max 9", "max", 9);
        assertParseResult(cap, "bigdoors addowner mydoor --player pim16aap2 --group \"group 1\" --admin", "admin",
                          true);
        assertParseResult(cap, "bigdoors addowner mydoor --player pim16aap2 --group \"group 1\"", "doorID", "mydoor");

        Assertions.assertDoesNotThrow(() -> cap
            .parseInput(commandSender, "bigdoors addowner mydoor --player pim16aap2 --group \"group 1\" --admin"));
        Assertions.assertDoesNotThrow(() -> cap
            .parseInput(commandSender, "bigdoors addowner mydoor --player pim16aap2 --group \"group 1\""));
        Assertions.assertDoesNotThrow(() -> cap
            .parseInput(commandSender, "bigdoors addowner mydoor --player pim16aap2"));
        Assertions.assertDoesNotThrow(() -> cap
            .parseInput(commandSender, "bigdoors addowner mydoor --player    pim16aap2"));
    }

    @SneakyThrows
    private void assertLastArgument(final @NonNull CAP cap, final @NonNull String input,
                                    final @NonNull String commandName)
    {
        final @NonNull CommandParser commandParser = new CommandParser(cap, commandSender, input, cap.getSeparator());
        Assertions.assertEquals(commandName, commandParser.getLastCommand().getCommand().getName(null));
    }

    @Test
    void isFreeArgumentName()
    {
        final @NonNull CAP cap = setUp(CAP.getDefault().toBuilder().exceptionHandler(null).build());

        final @NonNull Command addowner = cap.getCommand("addowner").orElseThrow(
            () -> new RuntimeException("Failed to find command \"addowner\"!!"));

        // TODO: Maybe check the number of argument prefixes? "-".
        Assertions.assertTrue(CommandParser.isFreeArgumentName(addowner, "--group"));
        Assertions.assertTrue(CommandParser.isFreeArgumentName(addowner, "-g"));
        Assertions.assertFalse(CommandParser.isFreeArgumentName(addowner, "aaaaa"));
        Assertions.assertFalse(CommandParser.isFreeArgumentName(addowner, "--groups"));
        Assertions.assertFalse(CommandParser.isFreeArgumentName(addowner, "-groups"));
        Assertions.assertFalse(CommandParser.isFreeArgumentName(addowner, "-z"));
    }

    @Test
    void testGetLastCommand()
    {
        final @NonNull CAP cap = setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build());
        assertLastArgument(cap, "bigdoors addowner test", "addowner");
        assertLastArgument(cap, "bigdoors addowner ", "addowner");
        assertLastArgument(cap, "bigdoors addowner=", "bigdoors");
        assertLastArgument(cap, "bigdoors addowner help", "addowner");
        assertLastArgument(cap, "bigdoors addowner help bigdoors", "addowner");
        assertLastArgument(cap, "bigdoors help addowner ", "help");
    }

    @Test
    void testGetLastCommandSpaceSeparator()
    {
        final @NonNull CAP cap = setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').build());
        assertLastArgument(cap, "bigdoors addowner ", "addowner");
    }
}
