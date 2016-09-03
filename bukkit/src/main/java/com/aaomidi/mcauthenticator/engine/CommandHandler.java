package com.aaomidi.mcauthenticator.engine;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.engine.commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by amir on 2016-01-11.
 */
public final class CommandHandler implements CommandExecutor {
    private final MCAuthenticator instance;
    private final Map<String, AuthCommand> commands = new TreeMap<>();

    public CommandHandler(MCAuthenticator instance) {
        this.instance = instance;
    }

    public void registerCommand(AuthCommand cmd) {
        commands.put(cmd.getName().toLowerCase(), cmd);
    }

    public void registerCommands() {
        registerCommand(new ReloadConfigCommand(instance));
        registerCommand(new ResetCommand(instance));
        registerCommand(new DisableCommand(instance));
        registerCommand(new EnableCommand(instance));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String commandLabel, String[] args) {

        if(!commandSender.hasPermission("mcauthenticator.use")) {
            instance.getC().sendDirect(commandSender, "&cYou do not have permission to use the authenticator!");
            return true;
        }

        if (args.length == 0) return printHelp(commandSender, commandLabel);

        AuthCommand c = commands.get(args[0].toLowerCase());
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        if (c == null) {
            instance.getC().sendDirect(commandSender, "&cUnknown subcommand!");
            return printHelp(commandSender, commandLabel);
        }

        if (c.getPermission() != null && !commandSender.hasPermission(c.getPermission()) && !(commandSender instanceof ConsoleCommandSender)) {
            instance.getC().sendDirect(commandSender, "&cYou do not have permission to perform this action!");
            return true;
        }

        try {
            c.execute(cmd, commandSender, newArgs);
        } catch (RuntimeException e) {
            instance.handleException(e);
        }

        return true;
    }

    private boolean printHelp(CommandSender commandSender, String label) {
        int i = 1;

        StringBuilder sb = new StringBuilder("&7Possible commands are: ");
        for (AuthCommand c : commands.values()) {
            if(commandSender.hasPermission(c.getPermission()) ||
                    (commandSender instanceof ConsoleCommandSender))
                sb.append(String.format("\n &8%d. &7/%s %s &8- &7%s", i++, label, c.getName(), c.getDesc()));
        }

        instance.getC().sendDirect(commandSender, sb.toString());
        return true;
    }
}
