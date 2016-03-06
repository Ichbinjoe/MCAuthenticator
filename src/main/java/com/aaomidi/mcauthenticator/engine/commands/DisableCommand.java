package com.aaomidi.mcauthenticator.engine.commands;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.model.UserData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 2/1/16
 */
public final class DisableCommand extends AuthCommand {
    public DisableCommand(MCAuthenticator instance) {
        super(instance, "disable", "mcauthenticator.disable", "Disables 2FA on an account");
    }

    @Override
    public boolean execute(Command command, final CommandSender commandSender, String[] args) {
        if (args.length == 0) {
            //Self
            if (!(commandSender instanceof Player)) {
                getInstance().getC().sendDirect(commandSender, "&cYou must specify a player to enable 2FA on.\n" +
                        "&c    /auth disable <player>");
                return true;
            }

            Player sndr = ((Player) commandSender);
            User u = getInstance().getCache().get(sndr.getUniqueId());
            if (!(u.is2fa() && !u.mustSetUp2FA())) { //Fully set up, we can cancel 'init' staged 2FA
                getInstance().getC().send(commandSender, getInstance().getC().message("alreadyDisabled"));
                return true;
            }

            if(u.isLocked(sndr)) {
                getInstance().getC().send(commandSender, getInstance().getC().message("disableForced"));
                return true;
            }

            u.invalidateKey();
            u.reverseInventory(sndr);
            getInstance().save();

            getInstance().getC().send(commandSender, getInstance().getC().message("selfDisabled"));
        } else if (args.length == 1) {
            if (!commandSender.hasPermission("mcauthenticator.disable.other") && !isConsole(commandSender)) {
                getInstance().getC().sendDirect(commandSender, "&cYou are not permitted to disable other people's 2FA!");
                return true;
            }
            final String playerQuery = args[0];
            getInstance().async(new Runnable() {
                @Override
                public void run() {
                    final OfflinePlayer player = Bukkit.getOfflinePlayer(playerQuery);
                    if (player == null) {
                        getInstance().getC().sendDirect(commandSender, "&cThe player &4'" + playerQuery + "'&c does not exist!");
                        return;
                    }

                    if (player.isOnline()) {
                        final User u = getInstance().getCache().get(player.getUniqueId());
                        if (!(u.is2fa() && !u.mustSetUp2FA())) {
                            getInstance().getC().sendDirect(commandSender, "&cThe player &4'" + playerQuery + "'&c already has 2FA disabled.");
                            return;
                        }

                        if(u.isLocked(((Player) player))) {
                            getInstance().getC().sendDirect(commandSender, "&cYou cannot disable 2FA on the player's account since they have the locked permission.");
                            return;
                        }

                        getInstance().sync(new Runnable() {
                            @Override
                            public void run() {
                                getInstance().getC().send(((Player) player), getInstance().getC().message("otherDisable").replaceAll("%player%", commandSender.getName()));
                                getInstance().getC().sendDirect(commandSender, "&7You have disabled 2FA on "+player.getName()+"'s account.");
                                u.invalidateKey();
                                u.reverseInventory((Player) player);
                                getInstance().save();
                            }
                        });
                    } else {
                        //Player isn't online, so lets just kinda 'help them along'
                        //Lets first check if they already have a record.

                        UserData d;

                        try {
                            d = getInstance().getDataSource().getUser(player.getUniqueId());
                        } catch (IOException | SQLException e) {
                            commandSender.sendMessage(ChatColor.RED + "There was an issue retrieving the userdata. Check console.");
                            getInstance().handleException(e);
                            return;
                        }

                        if (d != null) {
                            if(d.isLocked(null) && !(commandSender instanceof ConsoleCommandSender)) {
                                getInstance().getC().sendDirect(commandSender,"&c This user has the locked permission! You cannot disable 2FA for this person unless you are in console!");
                                return;
                            }
                            //Take out the entry
                            getInstance().getDataSource().destroyUser(player.getUniqueId());
                            getInstance().getC().sendDirect(commandSender, "&7Disabled 2FA for " + player.getName() + ".");
                            getInstance().save();
                        } else {
                            getInstance().getC().sendDirect(commandSender, "&4'" + player.getName() + "'&c already has 2FA disabled!");
                        }
                    }
                }
            });
        } else {
            getInstance().getC().sendDirect(commandSender, "&c Invalid usage: /auth disable" + (commandSender.hasPermission("mcauthenticator.disable.other") ? " [player]" : ""));
        }
        return true;
    }
}
