package com.aaomidi.mcauthenticator.engine.commands;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.AuthCommand;
import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.util.StringManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by amir on 2016-01-11.
 */
public class ResetCommand extends AuthCommand {
    public ResetCommand(MCAuthenticator instance, String name, String permission) {
        super(instance, name, permission);
    }

    @Override
    public boolean execute(Command command, CommandSender commandSender, String[] args) {
        if (args.length == 0) {
            StringManager.sendMessage(commandSender, "&cPlease specify a player.");
            return true;
        }
        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            StringManager.sendMessage(commandSender, "&cThat player was not recognized.");
            return true;
        }
        User user = getInstance().getDataManager().getDataFile().getUser(player.getUniqueId());

        if (user == null) {
            StringManager.sendMessage(commandSender, "&cThat player is not 2FAed.");
            return true;
        }

        if (user.isAuthenticated()) {
            StringManager.sendMessage(commandSender, "&cThat player is already authenticated.");
            return true;
        }

        if (user.isLocked()) {
            StringManager.sendMessage(commandSender, "&cThat player is locked. You must delete that player's entry in the data.json file!");
            return true;
        }

        user.setSecret(null);
        user.protectPlayer(player);
        StringManager.sendMessage(commandSender, "&bSuccessfully reset that player's datafile!");
        return true;
    }
}
