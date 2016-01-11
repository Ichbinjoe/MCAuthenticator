package com.aaomidi.mcauthenticator.engine.commands;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.config.ConfigReader;
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
public class ReloadDataCommand extends AuthCommand {
    public ReloadDataCommand(MCAuthenticator instance, String name, String permission) {
        super(instance, name, permission);
    }

    @Override
    public boolean execute(Command command, CommandSender commandSender, String[] args) {
        getInstance().getDataManager().reload();
        StringManager.sendMessage(commandSender, "&bSuccessfully reloaded data file!");
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = getInstance().getDataManager().getDataFile().getUser(player.getUniqueId());
            if (user == null && !player.hasPermission(ConfigReader.getStaffPermission())) {
                continue;
            }
            getInstance().handlePlayer(player);

            user = getInstance().getDataManager().getDataFile().getUser(player.getUniqueId());
            if (user == null) {
                continue;
            }
            if (user.getInetAddress() != null && user.getInetAddress().equals(player.getAddress().getAddress())) {
                user.setAuthenticated(true);
                continue;
            }

            StringManager.sendMessage(player, "&cDue to a datareset issued by &a%s&c, you must reauthenticate.", commandSender.getName());
            if (!user.isProtected()) {
                if (!user.protectPlayer(player)) {
                    StringManager.sendMessage(player, "&cSevere error occurred when protecting you!");
                }
            }
            StringManager.sendMessage(player, "&dPlease enter your authentication code using Google Authenticator: ");
            getInstance().getDataManager().saveFile();
        }
        return true;
    }
}
