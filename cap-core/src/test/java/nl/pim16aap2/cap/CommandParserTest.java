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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CommandParserTest
{
    final DefaultCommandSender commandSender = new DefaultCommandSender();
    private @NonNull nl.pim16aap2.cap.CAP cap;

    private static final List<String> playerNames = new ArrayList<>(
        Arrays.asList("pim16aap2", "pim16aap3", "mip16aap2", "pim"));

    private static final List<String> doorIDs = new ArrayList<>(Arrays.asList("myDoor", "42", "myPortcullis", "84"));

    @BeforeEach
    void setUp()
    {
        cap = CAP.getDefault().toBuilder().exceptionHandler(null).build();
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
                          .tabcompleteFunction((commandSender, argument) -> doorIDs)
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
                          .tabcompleteFunction((commandSender, argument) -> playerNames)
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
    }

    @Test
    void getCommandTabCompleteOptions()
    {
        Assertions.assertEquals(20, cap.getTabCompleteOptions(commandSender, "bigdoors sub").size());

        Assertions.assertEquals(23, cap.getTabCompleteOptions(commandSender, "bigdoors").size());

        Assertions.assertEquals("bigdoors", cap.getTabCompleteOptions(commandSender, "big").get(0));

        Assertions.assertEquals(0, cap.getTabCompleteOptions(commandSender, "sub").size());
    }

    @Test
    void getArgumentNameTabCompleteOptions()
    {
        final List<String> playerSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner -p");
        Assertions.assertEquals(2, playerSuggestions.size());
        Assertions.assertEquals("p", playerSuggestions.get(0));
        Assertions.assertEquals("player", playerSuggestions.get(1));

        final List<String> longPlayerSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner --pla");
        Assertions.assertEquals(1, longPlayerSuggestions.size());
        Assertions.assertEquals("player", longPlayerSuggestions.get(0));

        final List<String> adminSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner --a");
        Assertions.assertEquals(2, adminSuggestions.size());
        Assertions.assertEquals("a", adminSuggestions.get(0));
        Assertions.assertEquals("admin", adminSuggestions.get(1));

        final List<String> suggestionsFromEmpty =
            cap.getTabCompleteOptions(commandSender, "bigdoors subcommand_0");
        Assertions.assertEquals(1, suggestionsFromEmpty.size());
        Assertions.assertEquals("value", suggestionsFromEmpty.get(0));
    }

    @Test
    void getFreeArgumentValueTabCompleteOptions()
    {
        List<String> playerNameSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner -p=");

        Assertions.assertEquals(4, playerNameSuggestions.size());

        playerNameSuggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner -p=pim");
        Assertions.assertEquals(3, playerNameSuggestions.size());

        playerNameSuggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner -p=pim16");
        Assertions.assertEquals(2, playerNameSuggestions.size());
        Assertions.assertEquals("pim16aap2", playerNameSuggestions.get(0));
        Assertions.assertEquals("pim16aap3", playerNameSuggestions.get(1));
    }

    @Test
    void getPositionalValueTabCompleteOptions()
    {
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
    @Test
    void testNumericalInput()
    {
        // My name is not numerical.
        assertWrappedThrows(IllegalValueException.class,
                            () -> cap.parseInput(commandSender, "bigdoors numerical -max=pim16aap2"));
        // The max value is set to 10, so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, "bigdoors numerical -max=10"));
        // With a max value of 10, 9 is perfect!
        Assertions.assertDoesNotThrow(() -> cap.parseInput(commandSender, "bigdoors numerical -max=9"));


        // The maxd value is set to 10.0, so 10.0 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, "bigdoors numerical -maxd=10.0"));
        // With a maxd value of 10.0, 9.9 is perfect!
        Assertions.assertDoesNotThrow(() -> cap.parseInput(commandSender, "bigdoors numerical -maxd=9.9"));


        // The min value is set to 10, so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, "bigdoors numerical -min=10"));
        // With a min value of 10, 11 is perfect!
        Assertions.assertDoesNotThrow(() -> cap.parseInput(commandSender, "bigdoors numerical -min=11"));

        // The range is set to [10, 20], so 10 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, "bigdoors numerical -range=10"));
        // The range is set to [10, 20], so 20 will be illegal.
        assertWrappedThrows(ValidationFailureException.class,
                            () -> cap.parseInput(commandSender, "bigdoors numerical -range=20"));
        // With a range of [10, 20], 11 is perfect!
        Assertions.assertDoesNotThrow(() -> cap.parseInput(commandSender, "bigdoors numerical -range=11"));
    }
}
