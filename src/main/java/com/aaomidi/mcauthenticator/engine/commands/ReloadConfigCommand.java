package com.aaomidi.mcauthenticator.engine.commands;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.aaomidi.mcauthenticator.model.AuthCommand;
import com.aaomidi.mcauthenticator.util.StringManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by amir on 2016-01-11.
 */
public class ReloadConfigCommand extends AuthCommand {
    public ReloadConfigCommand(MCAuthenticator instance, String name, String permission) {
        super(instance, name, permission);
    }

    @Override
    public boolean execute(Command command, CommandSender commandSender, String[] args) {
        getInstance().reload();

        StringManager.sendMessage(commandSender, "&bSuccessfully reloaded configuration!");
        return true;
    }
}
