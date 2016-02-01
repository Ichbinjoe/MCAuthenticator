package com.aaomidi.mcauthenticator.engine;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.engine.commands.ReloadConfigCommand;
import com.aaomidi.mcauthenticator.engine.commands.ResetCommand;
import com.aaomidi.mcauthenticator.engine.commands.AuthCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by amir on 2016-01-11.
 */
public class CommandHandler implements CommandExecutor {
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
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equals("auth")) return false;

        if (args.length == 0) {
            return printHelp(commandSender);
        }

        AuthCommand c = commands.get(args[0].toLowerCase());
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        if (c == null) {
            instance.getC().sendDirect(commandSender, "&cUnknown subcommand!");
            return true;
        }

        if (c.getPermission() != null && !commandSender.hasPermission(c.getPermission())) {
            instance.getC().sendDirect(commandSender, "&cYou do not have permission to perform this action!");
            return true;
        }

        c.execute(cmd, commandSender, newArgs);
        return true;
    }

    private boolean printHelp(CommandSender commandSender) {
        int i = 1;
        StringBuilder sb = new StringBuilder("&bPossible commands are: ");
        for (AuthCommand c : commands.values()) {
            sb.append(String.format("\n &d%d. &b/auth %s", i++, c.getName()));
        }

        instance.getC().sendDirect(commandSender, sb.toString());
        return true;
    }
}
