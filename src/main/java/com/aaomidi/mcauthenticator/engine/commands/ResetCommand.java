package com.aaomidi.mcauthenticator.engine.commands;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.model.UserData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by amir on 2016-01-11.
 */
public class ResetCommand extends AuthCommand {
    public ResetCommand(MCAuthenticator instance) {
        super(instance, "reset", "mcauthenticator.reset");
    }

    @Override
    public boolean execute(final Command command, final CommandSender commandSender, String[] args) {
        if (args.length == 0) {
            getInstance().getC().sendDirect(commandSender, "&cIncorrect Usage: /auth reset <player>");
            return true;
        }
        final String playerName = args[0];
        getInstance().async(new Runnable() {
            @Override
            public void run() {
                //getOfflinePlayer can make a web query, and isn't safe to run in sync.
                final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                if (player == null) {
                    getInstance().getC().sendDirect(commandSender, "&cCould not look up player '" + playerName + "'.");
                    return;
                }

                final User onlineUser = getInstance().getCache().get(player.getUniqueId());
                if (onlineUser == null) {
                    //Player is offline
                    UserData data;
                    try {
                        data = getInstance().getDataSource().getUser(player.getUniqueId());
                    } catch (IOException | SQLException e) {
                        getInstance().getC().sendDirect(commandSender, "&cThere was a fatal error when looking up that player's data!\n" +
                                "&c Check the console for more information!");
                        e.printStackTrace();
                        return;
                    }
                    if (data == null) {
                        //Player is also not 2fa'd
                        getInstance().getC().sendDirect(commandSender, "&cThat player does not have 2FA set up.");
                    } else {
                        if (data.isLocked(null) && !(commandSender instanceof ConsoleCommandSender)) {
                            getInstance().getC().sendDirect(commandSender, "&cThat player is locked, and can only be reset from console.");
                            return;
                        }
                        if (data.getSecret() == null) {
                            getInstance().getC().sendDirect(commandSender, "&cThe player is already in reset mode: when they log in, they will recieve a new 2fa.");
                            return;
                        }
                        data.setSecret(null); //Resets, triggers force 2fa update on log in
                        getInstance().getC().sendDirect(commandSender, "&bThe player's 2FA will be reset when they next log in.");
                    }
                } else {
                    if (!onlineUser.is2fa()) {
                        getInstance().getC().sendDirect(commandSender, "&cThat player does not have 2FA set up.");
                        return;
                    }

                    if (onlineUser.authenticated()) {
                        getInstance().getC().sendDirect(commandSender, "&cThat player is already authenticated.");
                        return;
                    }

                    if (onlineUser.isLocked(((Player) player)) && !(commandSender instanceof ConsoleCommandSender)) {
                        getInstance().getC().sendDirect(commandSender, "&cThat player is locked, and can only be reset from console.");
                        return;
                    }

                    onlineUser.invalidateKey();
                    getInstance().getC().send(commandSender, getInstance().getC().message("resetPlayer").replace("%player%", commandSender.getName()));
                    getInstance().sync(new Runnable() {
                        @Override
                        public void run() {
                            onlineUser.init2fa(((Player) player));
                            getInstance().getC().sendDirect(commandSender, "&bThe player's 2FA is now reset. They will now set up a new one.");
                        }
                    });
                }
            }
        });
        return true;
    }
}
