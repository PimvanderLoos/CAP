/*
 *  MIT License
 *
 * Copyright (c) 2020 Pim van der Loos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.pim16aap2.spigotexample;

import nl.pim16aap2.cap.SpigotCAP;
import nl.pim16aap2.cap.argument.specialized.StringArgument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.localization.ArgumentNamingSpec;
import nl.pim16aap2.cap.localization.CommandNamingSpec;
import nl.pim16aap2.cap.localization.Localizer;
import nl.pim16aap2.cap.text.TextType;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Locale;

/**
 * Simple plugin for Spigot showing how to use CAP to create a command system.
 *
 * @author Pim
 */
public class ExamplePlugin extends JavaPlugin
{
    private static final Locale LOCALE_DUTCH = new Locale("nl", "NL");
    private static final Locale LOCALE_ENGLISH = Locale.US;

    private SpigotCAP cap;

    @Override
    public void onEnable()
    {
        // Step 1: Create a new CAP instance.
        cap = SpigotCAP.spigotCAPBuilder()
                       .plugin(this)
                       // Optional: Sets the separator for free arguments. When set to ' ' those will be of the format
                       // "--player pim16aap2". When set to '=' as another example, that would be "--player=pim16aap2".
                       // This defaults to ' '.
                       .separator(' ')
                       // Optional: Make some changes to the default color scheme.
                       .colorScheme(SpigotCAP.getDefaultColorScheme()
                                             .setStyle(TextType.SUMMARY, ChatColor.AQUA)
                                             .setStyle(TextType.ERROR, ChatColor.DARK_RED, ChatColor.BOLD))
                       // Optional: Provide a Localizer for managing translations.
                       // The first locale you provide will be the default one.
                       // Note that in the current example, there isn't actually an en_US translation file, so
                       // all requests will fall through to the base translation file.
                       .localizer(new Localizer("Translations", LOCALE_ENGLISH, LOCALE_DUTCH))
                       .build();

        // Step 2: Let's add some commands, shall we?
        // We build this in reverse order. So if we want to build a command structure "/baseCommand config set <val>"
        // we define the "set" subcommand first, followed by the "config" command and then we do the "baseCommand" last.
        final Command setCommand = Command
            .commandBuilder()
            // Required: Provide the command with the CAP instance we're using.
            .cap(cap)
            // Required: Provide the name specification for this command.
            // For localized commands, we only need to provide the base of the keys.
            // In this case, we specify "myPlugin.command.set". CAP will then use
            // "myplugin.command.set.name" from the .properties file in the resources directory for the name.
            // The summary will be "myplugin.command.set.summary" and the same goes for the other names as well.
            // Note that of those, only the name is required.
            .nameSpec(new CommandNamingSpec.Localized("myPlugin.command.set"))
            // Optional: Now let's add an argument!
            // We'll add an argument to change the language using "-l <language>" or "--language <language>".
            // An argument of the shape "--argumentName value" is a free and optional argument, as it doesn't
            // require to be in a specific order or position.
            // Because we'll want a string as input, we'll use a StringArgument, but there are other as well
            // (and you can easily make your own one!).
            .argument(new StringArgument()
                          .getOptional()
                          // Required: The name of the argument.
                          // This works the same as it did for giving the command a name.
                          .nameSpec(new ArgumentNamingSpec.Localized("myPlugin.command.set.argument.language"))
                          // Required: The identifier to give the argument. This is how we'll retrieve it later on.
                          .identifier("locale")
                          // Optional: Provide a function to get a list of tab-completion suggestions.
                          .tabCompleteFunction(request -> Arrays.asList("nl", "en"))
                          // Optional: Provide a default value.
                          .defaultValue("en")
                          .build())
            // Required: (Unless this command is virtual, more about that later).
            // Define what happens when this command is used.
            .commandExecutor(this::updateLocale)
            .build();

        // Now let's work on the next command. In this case, "config".
        final Command configCommand = Command
            .commandBuilder()
            .cap(cap)
            // Add the command we just created as sub command.
            .subCommand(setCommand)
            // This is an alternative way to give a command its name. This way the names/etc are all completely static
            // No localization will be used for this command, so no matter the language of the CommandSender,
            // they all see the same stuff.
            // The ArgumentNamingSpec (for Arguments) also has a similar function).
            .nameSpec(CommandNamingSpec.RawStrings
                          .builder()
                          .name("config")
                          .summary("Configures all kinds of things!")
                          .sectionTitle("Config")
                          .build())
            // We don't want to let this command execute anything on it's own; it's only a super command for
            // the subcommands we defined before.
            .virtual(true)
            .build();

        // And lastly, there's the base command.
        final Command baseCommand = Command
            .commandBuilder()
            .cap(cap)
            // Optional: Tell CAP to add the default help subcommand ("default.helpCommand").
            // When its name is "help" (which it is by default), this means we can now use
            // "/basecommand help [subcommand]".
            .addDefaultHelpSubCommand(true)
            .subCommand(configCommand)
            .nameSpec(new CommandNamingSpec.Localized("myPlugin.command.baseCommand"))
            .build();

        // Finally, once all commands have been created, we tell CAP to register all top-level commands.
        // This makes sure that Spigot is aware that these commands exist. If it doesn't know about our commands'
        // existence, it won't be able to provide any tab-completion support for them.
        cap.registerTopLevelCommands();
    }

    private void updateLocale(final CommandResult commandResult)
    {
        // Let's first get the input provided by the user. Remember that we set the
        // identifier for the language argument to "locale"? That's how we can get it now.
        final String input = commandResult.getParsedArgument("locale");
        final Locale locale;
        if (input.equals("nl"))
            locale = LOCALE_DUTCH;
        else if (input.equals("en"))
            locale = LOCALE_ENGLISH;
        else
        {
            // Well, we couldn't find a valid language code, so let's send the user the
            // help menu for this command again. Maybe that'll help them figure out how it works.
            commandResult.sendCommandHelp();
            return;
        }

        // Now that that's all out of the way, let's update the command sender's locale.
        cap.updateLocale(commandResult.getCommandSender(), locale);

        // Quick note on updating locales: This only works for as long as the CommandSender is available.
        // So for players, their locale resets when they log out and for the server it will reset on restart.
        // If you want persistent locales, you'll have to figure out some way of storing them yourself.
        // You can specify and ILocaleProvider in the CAP setup, which will request a CommandSender's locale
        // when it isn't set yet (subsequent lookups are cached).
    }
}
