package com.aaomidi.mcauthenticator.model;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by amir on 2016-01-11.
 */
public abstract class AuthCommand {
    @Getter
    private final MCAuthenticator instance;
    @Getter
    private final String name;
    @Getter
    private final String permission;

    public AuthCommand(MCAuthenticator instance, String name, String permission) {
        this.instance = instance;
        this.name = name;
        this.permission = permission;

        instance.getCommandHandler().registerCommand(this);
    }

    public abstract boolean execute(Command command, CommandSender commandSender, String[] args);

}
