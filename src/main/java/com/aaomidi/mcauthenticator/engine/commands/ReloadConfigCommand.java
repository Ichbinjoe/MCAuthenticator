package com.aaomidi.mcauthenticator.engine.commands;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by amir on 2016-01-11.
 */
public class ReloadConfigCommand extends AuthCommand {
    public ReloadConfigCommand(MCAuthenticator instance) {
        super(instance, "reload", "mcauthenticator.reload.config");
    }

    @Override
    public boolean execute(Command command, CommandSender commandSender, String[] args) {
        MCAuthenticator i = getInstance();
        i.reload();

        i.getC().sendDirect(commandSender, "&bSuccessfully reloaded configuration and datasources!\n" +
                "&bAll authenticated users will now be forced to reauthenticate.");
        return true;
    }
}
