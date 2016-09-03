package com.aaomidi.mcauthenticator.engine.commands;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by amir on 2016-01-11.
 */
public final class ReloadConfigCommand extends AuthCommand {
    public ReloadConfigCommand(MCAuthenticator instance) {
        super(instance, "reload", "mcauthenticator.reload", "Reloads the configuration and data, and reauthenticates everyone if needed.");
    }

    @Override
    public boolean execute(Command command, CommandSender commandSender, String[] args) {
        MCAuthenticator i = getInstance();
        i.reload();

        i.getC().sendDirect(commandSender, "&7Successfully reloaded configuration and datasources!\n" +
                "&7All authenticated users will now be forced to reauthenticate.");
        return true;
    }
}
