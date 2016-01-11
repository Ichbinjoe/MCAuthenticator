package com.aaomidi.mcauthenticator.engine;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.engine.commands.ReloadConfigCommand;
import com.aaomidi.mcauthenticator.engine.commands.ReloadDataCommand;
import com.aaomidi.mcauthenticator.engine.commands.ResetCommand;
import com.aaomidi.mcauthenticator.model.AuthCommand;
import com.aaomidi.mcauthenticator.util.StringManager;
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
        instance.getCommand("auth").setExecutor(this);
    }

    public void registerCommand(AuthCommand cmd) {
        //instance.getCommand(cmd.getName().toLowerCase()).setExecutor(this);
        commands.put(cmd.getName().toLowerCase(), cmd);
    }

    public void registerCommands() {
        new ReloadConfigCommand(instance, "reloadconfig", "auth.reloadconfig");
        new ReloadDataCommand(instance, "reloaddata", "auth.reloaddata");
        new ResetCommand(instance, "reset", "auth.reset");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String commandLabel, String[] args) {
        if (!cmd.getName().equals("auth")) return false;

        if (args.length == 0) {
            int i = 1;
            StringBuilder sb = new StringBuilder("&bPossible commands are: ");
            sb.append(String.format("\n &d%d. &b/auth reloadconfig", i++));
            sb.append(String.format("\n &d%d. &b/auth reloaddata", i++));
            sb.append(String.format("\n &d%d. &b/auth reset [player]", i++));

            StringManager.sendMessage(commandSender, sb.toString());
            return true;
        }

        AuthCommand c = commands.get(args[0].toLowerCase());
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

        if (c == null) {
            StringManager.sendMessage(commandSender, "Unrecognized command");
            return true;
        }

        c.execute(cmd, commandSender, newArgs);
        return true;
    }
}
