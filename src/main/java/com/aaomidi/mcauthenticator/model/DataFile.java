package com.aaomidi.mcauthenticator.model;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by amir on 2016-01-11.
 */
@RequiredArgsConstructor
public class DataFile {
    private final List<User> users;
    private transient HashMap<UUID, User> userMap;

    private void setupUserMap() {
        if (userMap != null && !userMap.isEmpty()) {
            return;
        }
        userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getUuid(), user);
        }
    }

    public User getUser(UUID uuid) {
        this.setupUserMap();
        return userMap.get(uuid);
    }

    public void addUser(User user) {
        this.users.add(user);
        this.userMap.put(user.getUuid(), user);
    }
}
