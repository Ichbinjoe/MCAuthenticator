package io.ibj.mcauthenticator.model.datasource;

import io.ibj.mcauthenticator.MCAuthenticator;
import io.ibj.mcauthenticator.model.UserData;
import io.ibj.mcauthenticator.model.UserDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/30/16
 */

public final class MySQLUserDataSource implements UserDataSource {

    public MySQLUserDataSource(String connectionURL, String username, String password) throws SQLException {
        this.updateHook = new UpdateHook() {
            @Override
            public void update(UpdatableFlagData me) {
                toUpdate.add(me);
            }
        };
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("com.mysql.jdbc.Driver");
        cfg.setJdbcUrl(connectionURL);
        cfg.setUsername(username);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(2);

        pool = new HikariDataSource(cfg);

        try (Connection c = pool.getConnection()) {
            ResultSet resultSet = c.createStatement().executeQuery("SHOW TABLES;");
            boolean found = false;
            while (resultSet.next()) {
                if (resultSet.getString(1).equalsIgnoreCase("2fa")) {
                    found = true;
                    break;
                }
            }
            resultSet.close();

            if (found) {
                try (ResultSet rs = c.createStatement().executeQuery("SHOW COLUMNS FROM 2FA;")) {
                    // Determine secret field (1.0.2 and before) and add type row
                    boolean hasAuthType = false;
                    while (rs.next()) {
                        String field = rs.getString("Field");
                        if (!field.equalsIgnoreCase("secret")) {
                            if (field.equalsIgnoreCase("authtype"))
                                hasAuthType = true;
                            continue;
                        }

                        // Secret field
                        if (!rs.getString("Type").equalsIgnoreCase("tinytext")) {
                            c.createStatement().execute("alter table 2FA MODIFY secret TINYTEXT;");
                            break;
                        }
                    }
                    if (!hasAuthType) {
                        c.createStatement().execute("alter table 2FA add authtype int DEFAULT 0;");
                    }
                }
            } else {
                c.createStatement().execute("CREATE TABLE 2FA(" +
                        "uuid CHAR(32) PRIMARY KEY," +
                        "ip VARCHAR(255)," +
                        "secret TINYTEXT," +
                        "authtype INT DEFAULT 0," +
                        "locked BIT(1));");
                c.createStatement().execute("CREATE INDEX uuid_index ON 2FA (uuid);");
            }
        }
    }

    private final HikariDataSource pool;
    private final UpdateHook updateHook;

    private volatile Set<UpdatableFlagData> toUpdate = new HashSet<>();
    private volatile Set<UUID> toDelete = new HashSet<>();

    @Override
    public UserData getUser(UUID id) throws IOException, SQLException {
        if (Bukkit.isPrimaryThread() && !MCAuthenticator.isReload)
            throw new RuntimeException("Primary thread I/O");
        try (Connection c = pool.getConnection()) {
            PreparedStatement p = c.prepareStatement("SELECT authtype, ip, secret, locked FROM 2FA WHERE uuid = ?;");
            p.setString(1, id.toString().replaceAll("-", ""));
            p.setQueryTimeout(5);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return new UpdatableFlagData(updateHook, id,
                        InetAddress.getByName(rs.getString("ip")),
                        rs.getString("secret"),
                        rs.getInt("authtype"),
                        rs.getBoolean("locked"));
            } else {
                return null;
            }
        }
    }

    @Override
    public UserData createUser(UUID id) {
        UpdatableFlagData d = new UpdatableFlagData(updateHook, id, null, null,
                -1, false);
        toUpdate.add(d);
        return d;
    }

    @Override
    public void destroyUser(UUID id) {
        toDelete.add(id);
    }

    @Override
    public void save() throws IOException, SQLException {
        Set<UpdatableFlagData> update = toUpdate;
        toUpdate = new HashSet<>();
        Set<UUID> delete = toDelete;
        toDelete = new HashSet<>();
        try (Connection c = pool.getConnection()) {
            PreparedStatement updateStatement = c.prepareStatement("INSERT INTO 2FA (uuid, ip, secret, locked) VALUES (?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE ip = ?, secret = ?, locked = ?");
            for (UpdatableFlagData upd : update) {
                if (delete.contains(upd.getId())) continue;
                updateStatement.setString(1, upd.getId().toString().replaceAll("-", ""));
                InetAddress lastAddress = upd.getLastAddress();
                String lastHostAddress = null;
                if (lastAddress != null)
                    lastHostAddress = lastAddress.getHostAddress();
                updateStatement.setString(2, lastHostAddress);
                updateStatement.setString(3, upd.getSecret());
                updateStatement.setBoolean(4, upd.isLocked(null));
                updateStatement.setString(5, lastHostAddress);
                updateStatement.setString(6, upd.getSecret());
                updateStatement.setBoolean(7, upd.isLocked(null));
                updateStatement.execute();
            }
            PreparedStatement deleteStatement = c.prepareStatement("DELETE FROM 2FA WHERE uuid = ?;");
            for (UUID uuid : delete) {
                deleteStatement.setString(1, uuid.toString().replaceAll("-", ""));
                deleteStatement.execute();
            }
        }
    }

    @Override
    public String toString() {
        return "(MySQLDataSource: " + pool.getJdbcUrl() + ")";
    }

    @Override
    public void invalidateCache() throws IOException {
        //No cache!!!
    }
}
