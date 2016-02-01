package com.aaomidi.mcauthenticator.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/29/16
 */
public interface UserDataSource {

    UserData getUser(UUID id) throws IOException, SQLException;
    UserData createUser(UUID id);
    void destroyUser(UUID id);

    void save() throws IOException, SQLException;
    void invalidateCache() throws IOException;
}
