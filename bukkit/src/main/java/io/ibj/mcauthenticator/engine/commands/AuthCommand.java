package io.ibj.mcauthenticator.engine.commands;

import io.ibj.mcauthenticator.MCAuthenticator;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

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
    @Getter
    private final String desc;

    public AuthCommand(MCAuthenticator instance, String name, String permission, String description) {
        this.instance = instance;
        this.name = name;
        this.permission = permission;
        this.desc = description;
    }

    public abstract boolean execute(Command command, CommandSender commandSender, String[] args);

    public boolean isConsole(CommandSender s) {
        return s instanceof ConsoleCommandSender;
    }
}
