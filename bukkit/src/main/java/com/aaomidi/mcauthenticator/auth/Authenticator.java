package com.aaomidi.mcauthenticator.auth;

import com.aaomidi.mcauthenticator.model.User;
import org.bukkit.entity.Player;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 2/28/16
 *
 * Represents anything that authenticates
 *
 * @since 1.1
 */
public interface Authenticator {

    boolean authenticate(User user, Player p, String input) throws Exception;

    boolean isFormat(String s);

    void initUser(User u, Player p);

    void quitUser(User u, Player p);

}
