package nl.pim16aap2.cap;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.IntegerArgument;
import nl.pim16aap2.cap.argument.specialized.StringArgument;
import nl.pim16aap2.cap.argument.validator.number.RangeValidator;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.exception.ExceptionHandler;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextComponent;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.util.LocalizationSpecification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main
{
    private static final @NonNull Locale LOCALE_DUTCH = new Locale("nl", "NL");
    private static final @NonNull Locale LOCALE_ENGLISH = Locale.US;

    private static void tabComplete(final @NonNull CAP cap, final @NonNull String command)
    {
        System.out.println(command + ":\n");
        final DefaultCommandSender commandSender = new DefaultCommandSender();

        final @NonNull List<String> tabOptions = cap.getTabCompleteOptions(commandSender, command);
        final StringBuilder sb = new StringBuilder();
        sb.append("Tab complete options:\n");
        tabOptions.forEach(opt -> sb.append(opt).append("\n"));
        System.out.println(sb.toString());
        System.out.println("=============\n");
    }

    private static void tryArgs(final @NonNull CAP cap, final @NonNull String command)
    {
        System.out.println(command + ":\n");
        final DefaultCommandSender commandSender = new DefaultCommandSender();
//        commandSender.setColorScheme(Main.getColorScheme());

        cap.parseInput(commandSender, command).ifPresent(CommandResult::run);
        System.out.println("=============\n");
    }

    private static void testSubStrings()
    {
        ColorScheme colorScheme = ColorScheme.builder().build();
        Text text = new Text(colorScheme);
        text.add("123456789");
        System.out.println(text.subsection(0, 3));
        System.out.println(text.subsection(3, 6));
        System.out.println(text.subsection(6, 9));

    }

    public static void main(final String... args)
    {
//        testTextComponents();
        final @NonNull CAP cap = initCommandManager();
//        testHelpRenderer(commandManager);
//        testSubStrings();

        Text textA = new Text(getColorScheme()).add("D E F", TextType.COMMAND).add(" ");
        Text textB = new Text(getColorScheme()).add("A B C", TextType.REGULAR_TEXT).add(" ");

        Text textC = new Text(getColorScheme()).add("D E F", TextType.COMMAND).add(" ");
        Text textD = new Text(getColorScheme()).add("A B C", TextType.REGULAR_TEXT).add(" ");

        Text textE = new Text(getColorScheme()).add("A B C", TextType.REGULAR_TEXT).add(" ");

        System.out.println(textA.prepend(textB));
        System.out.println(textD.add(textC));
        System.out.println(textE.add(textE));

        tabComplete(cap, "big");
        tabComplete(cap, "add");
        tabComplete(cap, "bigdoors a");
        tabComplete(cap, "bigdoors \"a");
        tabComplete(cap, "bigdoors h");
        tabComplete(cap, "bigdoors subcomma");
        tabComplete(cap, "bigdoors addowner myDoor -p=pim16aap2 -");
        tabComplete(cap, "bigdoors addowner myDoor -p=pim16aap2 ");
        tabComplete(cap, "bigdoors addowner myDoor --play");
        tabComplete(cap, "bigdoors addowner mydoor --admin ");
        tabComplete(cap, "bigdoors addowner \"tes");

        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2");
        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2 -p=pim16aap3 -p=pim16aap4");
        tryArgs(cap, "bigdoors addowner myDoor --player=pim16aap2");
        tryArgs(cap, "bigdoors addowner myDoor --player=pim16aap2 --admin");
        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner myD\\\"oor -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner \"myD\\\"oor\" -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner \"myD\\\" oor\" -p=\"pim16\"aap2 -a");
        tryArgs(cap, "bigdoors addowner 'myDoor' -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner 'myDoor' -p=pim16aap2 -a");
        tryArgs(cap, "bigdoors addowner -h");
        tryArgs(cap, "bigdoors addowner myDoor -p=\"pim16 \"aap2 -a");

        tryArgs(cap, "bigdoors help addowner");
        tryArgs(cap, "bigdoors help");
        tryArgs(cap, "bigdoors help 1");
        tryArgs(cap, "bigdoors help 2");
        tryArgs(cap, "bigdoors help 3");
        tryArgs(cap, "bigdoors help 4");
        tryArgs(cap, "bigdoors help 5");
        tryArgs(cap, "bigdoors help 6");
        tryArgs(cap, "bigdoors help");
        tryArgs(cap, "bigdoors addowner");
        tryArgs(cap, "bigdoors");
        tryArgs(cap, "bigdoors 1");
        tryArgs(cap, "bigdoors 2");

        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2 -p=pim16aap3 -p=pim16aap4 --admin -i=100");
        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2 -p=pim16aap3 -p=pim16aap4 --admin -i=5");
        tryArgs(cap, "bigdoors addowner myDoor -p");

        tryArgs(cap, "bigdoors required my_door 12");
//        tryArgs(cap, "bigdoors required 12 my_door"); // Invalid

        cap.setDefaultLocale(LOCALE_DUTCH);
        tryArgs(cap, "grotedeuren help");
        tryArgs(cap, "grotedeuren help 1");
        tryArgs(cap, "grotedeuren help 2");
        tryArgs(cap, "grotedeuren help eigenaartoevoegen");
        tryArgs(cap, "grotedeuren help 3");
        tryArgs(cap, "grotedeuren help 4");
        tryArgs(cap, "grotedeuren help 5");
        tryArgs(cap, "grotedeuren help 6");


        cap.setDefaultLocale(LOCALE_ENGLISH);
        tryArgs(cap, "bigdoors addowner myDoor -p=pim16aap2 -p=pim16aap3 -p=pim16aap4 --admin");
        cap.setDefaultLocale(LOCALE_DUTCH);
        tryArgs(cap, "grotedeuren eigenaartoevoegen myDoor --speler=pim16aap2 --speler=pim16aap3 -s=pim16aap4 --admin");
    }

    private static CAP initCommandManager()
    {
        final CAP cap = CAP
            .builder()
            .localizationSpecification(new LocalizationSpecification("CAPExample", LOCALE_ENGLISH, LOCALE_DUTCH))
            .separator('=')
            .debug(true)
            .exceptionHandler(ExceptionHandler.getDefault().toBuilder()
                                              .handler(nl.pim16aap2.cap.exception.NonExistingArgumentException.class,
                                                       (sender, ex) -> ex.printStackTrace())
                                              .handler(nl.pim16aap2.cap.exception.IllegalValueException.class,
                                                       (sender, ex) -> ex.printStackTrace())
//                                              .handler(nl.pim16aap2.cap.exception.CommandNotFoundException.class,
//                                                       (sender, ex) -> ex.printStackTrace())
//                                              .handler(nl.pim16aap2.cap.exception.MissingArgumentException.class,
//                                                       (sender, ex) -> ex.printStackTrace())
//                                              .handler(nl.pim16aap2.cap.exception.NoPermissionException.class,
//                                                       (sender, ex) -> ex.printStackTrace())
//                                              .handler(nl.pim16aap2.cap.exception.ValidationFailureException.class,
//                                                       (sender, ex) -> ex.printStackTrace())
//                                              .handler(nl.pim16aap2.cap.exception.UnmatchedQuoteException.class,
//                                                       (sender, ex) -> ex.printStackTrace())
//                                              .handler(nl.pim16aap2.cap.exception.MissingValueException.class,
//                                                       (sender, ex) -> ex.printStackTrace())
                                              .build())
            .helpCommandRenderer(DefaultHelpCommandRenderer
                                     .builder()
                                     .firstPageSize(1)
                                     .build())
            .build();

        final int subSubCommandCount = 5;
        final List<Command> subsubcommands = new ArrayList<>(subSubCommandCount);
        for (int idx = 0; idx < subSubCommandCount; ++idx)
        {
            final String base = "example.sub.sub.command." + idx;
            final String commandName = base + ".name";

            final Command generic = Command
                .commandBuilder().name(commandName)
                .addDefaultHelpArgument(true)
                .cap(cap)
                .summary(base + ".summary")
                .argument(
                    new StringArgument().getRequired().shortName("value").identifier("value").summary("random value")
                                        .build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(commandName, commandResult.getParsedArgument("value"))
                                         .runCommand())
                .build();
            subsubcommands.add(generic);
        }

        final Command addOwner = Command
            .commandBuilder()
            .cap(cap)
            .name("example.command.addowner.name")
            .addDefaultHelpArgument(true)
            .description("example.command.addowner.description")
            .summary("example.command.addowner.summary")
            .subCommands(subsubcommands)
            .permission(((commandSender, command) -> true))
            .argument(new StringArgument()
                          .getRequired()
                          .shortName("example.command.addowner.argument.doorid.shortname")
                          .summary("example.command.addowner.argument.doorid.summary")
                          .identifier("doorID")
                          .tabCompleteFunction(request -> Arrays.asList("test a", "test_b"))
                          .build())
            .argument(Argument.valuesLessBuilder()
                              .value(true)
                              .shortName("example.command.addowner.argument.admin.shortname")
                              .longName("example.command.addowner.argument.admin.longname")
                              .summary("example.command.addowner.argument.admin.summary")
                              .identifier("admin")
                              .build())
            .argument(new StringArgument()
                          .getRepeatable()
                          .shortName("example.command.addowner.argument.player.shortname")
                          .longName("example.command.addowner.argument.player.longname")
                          .label("example.command.addowner.argument.player.label")
                          .summary("example.command.addowner.argument.player.summary")
                          .identifier("players")
                          .tabCompleteFunction(request -> Arrays.asList("pim", "pim16aap2", "mip"))
                          .build())
            .argument(new StringArgument()
                          .getRepeatable()
                          .shortName("example.command.addowner.argument.group.shortname")
                          .longName("example.command.addowner.argument.group.longname")
                          .label("example.command.addowner.argument.group.label")
                          .summary("example.command.addowner.argument.group.summary")
                          .identifier("groups")
                          .build())
            .argument(new IntegerArgument()
                          .getOptional()
                          .shortName("example.command.addowner.argument.range.shortname")
                          .longName("example.command.addowner.argument.range.longname")
                          .label("example.command.addowner.argument.range.label")
                          .summary("example.command.addowner.argument.range.summary")
                          .identifier("range")
                          .argumentValidator(RangeValidator.integerRangeValidator(10, 20))
                          .build())
            .commandExecutor(
                commandResult ->
                    new AddOwner(commandResult.getParsedArgument("doorID"),
                                 commandResult.getParsedArgument("players"),
                                 commandResult.<List<String>>getParsedArgument("groups"),
                                 commandResult.getParsedArgument("admin")).runCommand())
            .build();

        final Command required = Command
            .commandBuilder()
            .cap(cap)
            .name("required")
            .addDefaultHelpArgument(true)
            .argument(new StringArgument()
                          .getRequired()
                          .shortName("doorID")
                          .identifier("ID")
                          .summary("The name or UID of the door")
                          .tabCompleteFunction(request -> Arrays.asList("test a", "test_b"))
                          .build())
            .argument(new IntegerArgument()
                          .getRequired()
                          .shortName("doorUID")
                          .identifier("UID")
                          .summary("The UID of the door")
                          .tabCompleteFunction(request -> Arrays.asList("0", "1", "1", "2", "3", "5", "8", "13", "21"))
                          .build())
            .commandExecutor(
                commandResult -> new GenericCommand("required", String
                    .format("ID: \"%s\", UID: %d", commandResult.getParsedArgument("ID"),
                            commandResult.<Integer>getParsedArgument("UID"))).runCommand())
            .build();

        final int subCommandCount = 20;
        final List<Command> subcommands = new ArrayList<>(subCommandCount);
        for (int idx = 0; idx < subCommandCount; ++idx)
        {
            final String command = "subcommand_" + idx;
            final Command generic = Command
                .commandBuilder().name(command)
                .addDefaultHelpArgument(true)
                .cap(cap)
                .argument(
                    new StringArgument().getRequired().shortName("value").identifier("value").summary("random value")
                                        .build())
                .commandExecutor(commandResult ->
                                     new GenericCommand(command, commandResult.getParsedArgument("value")).runCommand())
                .build();
            subcommands.add(generic);
        }

        final Command bigdoors = Command
            .commandBuilder()
            .cap(cap)
            .addDefaultHelpSubCommand(true)
            .name("example.command.bigdoors")
            .headerSupplier(commandSender -> new Text(commandSender.getColorScheme())
                .add("Parameters in angled brackets are required: ", TextType.HEADER)
                .add("<", TextType.REQUIRED_PARAMETER)
                .add("parameter", TextType.REQUIRED_PARAMETER_LABEL)
                .add(">", TextType.REQUIRED_PARAMETER).add("\n")

                .add("Parameters in square brackets are optional: ", TextType.HEADER)
                .add("[", TextType.OPTIONAL_PARAMETER)
                .add("parameter", TextType.OPTIONAL_PARAMETER_LABEL)
                .add("]", TextType.OPTIONAL_PARAMETER).add("\n")

                .add("If an argument is followed by a \"+\" symbol, it can be\n" +
                         "repeated as many times as you want. For example, for a\n" +
                         "hypothetical command \"", TextType.HEADER)
                .add("/command ", TextType.COMMAND)
                .add("[", TextType.OPTIONAL_PARAMETER)
                .add("p", TextType.OPTIONAL_PARAMETER_FLAG)
                .add("=", TextType.OPTIONAL_PARAMETER_SEPARATOR)
                .add("player", TextType.OPTIONAL_PARAMETER_LABEL)
                .add("]+", TextType.OPTIONAL_PARAMETER)

                .add("\", you can do: \n\"", TextType.HEADER)
                .add("/command -p=playerA -p=playerB", TextType.REGULAR_TEXT)
                .add("\".\n", TextType.HEADER)
                .add(" ", TextType.REGULAR_TEXT)
                .toString())

            .subCommand(addOwner)
            .subCommand(required)
            .subCommands(subcommands)
            .virtual(true)
            .build();

        subcommands.forEach(cap::addCommand);
        subsubcommands.forEach(cap::addCommand);
        cap.addCommand(addOwner).addCommand(bigdoors).addCommand(required);

        return cap;
    }

    static ColorScheme getClearColorScheme()
    {
        return ColorScheme.builder().build();
    }

    static ColorScheme getColorScheme()
    {
        return ColorScheme
            .builder()
            .setDefaultDisable(MinecraftStyle.RESET.getStringValue())
            .commandStyle(new TextComponent(MinecraftStyle.GOLD.getStringValue()))
            .optionalParameterStyle(new TextComponent(MinecraftStyle.BLUE.getStringValue() +
                                                          MinecraftStyle.BOLD.getStringValue()))
            .optionalParameterFlagStyle(new TextComponent(MinecraftStyle.LIGHT_PURPLE.getStringValue()))
            .optionalParameterSeparatorStyle(new TextComponent(MinecraftStyle.DARK_RED.getStringValue()))
            .optionalParameterLabelStyle(new TextComponent(MinecraftStyle.DARK_AQUA.getStringValue()))
            .requiredParameterStyle(new TextComponent(MinecraftStyle.RED.getStringValue() +
                                                          MinecraftStyle.BOLD.getStringValue()))
            .requiredParameterFlagStyle(new TextComponent(MinecraftStyle.DARK_BLUE.getStringValue()))
            .requiredParameterSeparatorStyle(new TextComponent(MinecraftStyle.BLACK.getStringValue()))
            .requiredParameterLabelStyle(new TextComponent(MinecraftStyle.WHITE.getStringValue()))
            .summaryStyle(new TextComponent(MinecraftStyle.AQUA.getStringValue()))
            .regularTextStyle(new TextComponent(MinecraftStyle.DARK_PURPLE.getStringValue()))
            .headerStyle(new TextComponent(MinecraftStyle.GREEN.getStringValue()))
            .sectionStyle(new TextComponent(MinecraftStyle.AQUA.getStringValue()))
            .footerStyle(new TextComponent(MinecraftStyle.DARK_RED.getStringValue()))
            .build();
    }

    static void testTextComponents()
    {
        final ColorScheme colorScheme = getColorScheme();

        {
            final Text text = new Text(colorScheme);
            text.add("Unstyled text!");
            System.out.println(text);
            System.out.println(text.add(text));
        }

        final Text text = new Text(colorScheme);
        text.add("This is a command", TextType.COMMAND).add("\n")
            .add("This is an optional parameter", TextType.OPTIONAL_PARAMETER).add("\n")
            .add("This is something else? I can't remember the types :(", TextType.HEADER).add("\n");


        final Text text2 = new Text(colorScheme);
        text2.add("This is the second Text!", TextType.REQUIRED_PARAMETER).add("\n");
        text2.add("This is some unstyled text!").add("\n");

        System.out.println(text.add(text2).toString());
    }
}
