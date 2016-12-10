package io.ibj.mcauthenticator.model;

import io.ibj.mcauthenticator.MCAuthenticator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
public final class UserCache {

    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();
    private final MCAuthenticator authenticator;

    public UserCache(MCAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public User get(UUID id) {
        return userMap.get(id);
    }

    public User join(UUID id, UserData data) throws IOException, SQLException {
        User user = new User(id, data, authenticator);
        userMap.put(id, user);
        return user;
    }

    public User leave(UUID id) {
        return userMap.remove(id);
    }

    public void invalidate() {
        userMap.clear();
    }

}
