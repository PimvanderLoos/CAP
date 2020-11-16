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
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.MissingValueException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.localization.ArgumentNamingSpec;
import nl.pim16aap2.cap.localization.CommandNamingSpec;
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
                .commandBuilder()
                .nameSpec(UtilsForTesting.getBasicCommandName(command))
                .cap(cap)
                .argument(new StringArgument()
                              .getOptional()
                              .nameSpec(ArgumentNamingSpec.RawStrings.builder()
                                                                     .shortName("v").label("val")
                                                                     .summary("random value").build())
                              .identifier("value")
                              .build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subcommands.add(generic);
        }

        final Command subSubSubCommand = Command
            .commandBuilder()
            .nameSpec(UtilsForTesting.getBasicCommandName("subsubsubcommand"))
            .cap(cap).virtual(true)
            .build();
        final Command subSubCommand = Command
            .commandBuilder()
            .nameSpec(UtilsForTesting.getBasicCommandName("subsubcommand"))
            .cap(cap).virtual(true)
            .subCommand(subSubSubCommand).build();

        final Command numerical = Command
            .commandBuilder()
            .nameSpec(UtilsForTesting.getBasicCommandName("numerical"))
            .cap(cap)
            .argument(new StringArgument()
                          .getOptional()
                          .nameSpec(ArgumentNamingSpec
                                        .RawStrings
                                        .builder()
                                        .shortName("value").label("val")
                                        .summary("random value").build())
                          .identifier("value")
                          .build())
            .argument(new IntegerArgument()
                          .getOptional().identifier("min")
                          .nameSpec(ArgumentNamingSpec
                                        .RawStrings
                                        .builder()
                                        .shortName("min").label("min")
                                        .summary("Must be more than 10!")
                                        .build())
                          .argumentValidator(MinimumValidator.integerMinimumValidator(10)).build())
            .argument(new IntegerArgument()
                          .getOptional().identifier("unbound")
                          .nameSpec(ArgumentNamingSpec
                                        .RawStrings
                                        .builder()
                                        .shortName("unbound").label("unbound")
                                        .summary("This value is not bound by anything!")
                                        .build())
                          .build())
            .argument(new IntegerArgument()
                          .getOptional().identifier("max")
                          .nameSpec(ArgumentNamingSpec.RawStrings
                                        .builder()
                                        .shortName("max").label("max")
                                        .summary("Must be less than 10!")
                                        .build())
                          .argumentValidator(MaximumValidator.integerMaximumValidator(10)).build())
            .argument(new DoubleArgument()
                          .getOptional().identifier("maxd")
                          .nameSpec(ArgumentNamingSpec
                                        .RawStrings
                                        .builder()
                                        .shortName("maxd").label("maxd")
                                        .summary("Must be less than 10.0!")
                                        .build())
                          .argumentValidator(MaximumValidator.doubleMaximumValidator(10.0)).build())
            .argument(new IntegerArgument()
                          .getOptional().identifier("range")
                          .nameSpec(ArgumentNamingSpec
                                        .RawStrings
                                        .builder()
                                        .shortName("range").label("range")
                                        .summary("Must be between 10 and 20!")
                                        .build())
                          .argumentValidator(RangeValidator.integerRangeValidator(10, 20)).build())
            .commandExecutor(commandResult ->
                                 new GenericCommand("numerical", commandResult.getParsedArgument("value")).runCommand())
            .build();

        final Command addOwner = Command
            .commandBuilder()
            .cap(cap)
            .nameSpec(CommandNamingSpec.RawStrings
                          .builder()
                          .name("addowner")
                          .description("Add 1 or more players or groups of players as owners of a door.")
                          .summary("Add another owner to a door.")
                          .build())
            .addDefaultHelpArgument(true)
            .argument(new StringArgument()
                          .getRequired().identifier("doorID")
                          .tabCompleteFunction((request) -> doorIDs)
                          .nameSpec(ArgumentNamingSpec.RawStrings
                                        .builder()
                                        .shortName("doorID")
                                        .summary("The name or UID of the door")
                                        .build())
                          .build())
            .argument(Argument.valuesLessBuilder().identifier("admin")
                              .value(true)
                              .nameSpec(ArgumentNamingSpec.RawStrings
                                            .builder()
                                            .shortName("a")
                                            .longName("admin")
                                            .summary(
                                                "Make the user an admin for the given door. Only applies to players.")
                                            .build())
                              .build())
            .argument(new StringArgument()
                          .getRepeatable().identifier("player")
                          .tabCompleteFunction((request) -> playerNames)
                          .nameSpec(ArgumentNamingSpec.RawStrings
                                        .builder()
                                        .shortName("p")
                                        .longName("player")
                                        .label("player")
                                        .summary("The name of the player to add as owner")
                                        .build())
                          .build())
            .argument(new StringArgument()
                          .getRepeatable().identifier("group")
                          .nameSpec(ArgumentNamingSpec.RawStrings
                                        .builder()
                                        .shortName("g")
                                        .longName("group")
                                        .label("player")
                                        .summary("The name of the group to add as owner")
                                        .build())
                          .build())
            .subCommand(subSubCommand)
            .commandExecutor(CommandResult::sendHelpMenu)
            .build();

        final Command bigdoors = Command
            .commandBuilder()
            .cap(cap)
            .addDefaultHelpSubCommand(true)
            .nameSpec(UtilsForTesting.getBasicCommandName("bigdoors"))
            .subCommand(addOwner)
            .subCommand(numerical)
            .subCommands(subcommands)
            .virtual(true)
            .build();

        subcommands.forEach(cap::addCommand);
        return cap.addCommand(addOwner).addCommand(bigdoors).addCommand(numerical).addCommand(subSubCommand)
                  .addCommand(subSubSubCommand);
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
        // The max value is set to 10, so 11 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -max%c11", sep)));
        // With a max value of 10, 9 is perfect!
        assertParseResult(cap, String.format("bigdoors numerical -max%c9", sep), "max", 9);


        // The maxd value is set to 10.0, so 10.1 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -maxd%c10.1", sep)));
        // With a maxd value of 10.0, 9.9 is perfect!
        assertParseResult(cap, String.format("bigdoors numerical -maxd%c9.9", sep), "maxd", 9.9);


        // The min value is set to 10, so 9 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -min%c9", sep)));
        // With a min value of 10, 11 is perfect!
        assertParseResult(cap, String.format("bigdoors numerical -min%c11", sep), "min", 11);


        // The range is set to [10, 20], so 9 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -range%c9", sep)));
        // The range is set to [10, 20], so 21 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, String.format("bigdoors numerical -range%c21", sep)));
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

        final @NonNull Command addowner = cap.getCommand("addowner", null).orElseThrow(
            () -> new RuntimeException("Failed to find command \"addowner\"!!"));

        Assertions.assertTrue(CommandParser.isFreeArgumentName(commandSender, addowner, "--group"));
        Assertions.assertTrue(CommandParser.isFreeArgumentName(commandSender, addowner, "-g"));
        Assertions.assertFalse(CommandParser.isFreeArgumentName(commandSender, addowner, "aaaaa"));
        Assertions.assertFalse(CommandParser.isFreeArgumentName(commandSender, addowner, "--groups"));
        Assertions.assertFalse(CommandParser.isFreeArgumentName(commandSender, addowner, "-groups"));
        Assertions.assertFalse(CommandParser.isFreeArgumentName(commandSender, addowner, "-z"));
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
        assertLastArgument(cap, "bigdoors addowner subsubcommand subsubsubcommand aaaa ", "subsubsubcommand");
    }
}
