package com.aaomidi.mcauthenticator.model.datasource;

import com.aaomidi.mcauthenticator.MCAuthenticator;

import java.net.InetAddress;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/30/16
 */
public class UpdatableFlagData extends BasicUserData {

    public UpdatableFlagData(MCAuthenticator authenticator, UpdateHook hook, UUID id, InetAddress inetAddress, String secret, boolean locked) {
        super(authenticator, id, inetAddress, secret, locked);
        this.hook = hook;
    }

    private final UpdateHook hook;

    @Override
    public void setLastAddress(InetAddress inetAddress) {
        super.setLastAddress(inetAddress);
        hook.update(this);
    }

    @Override
    public void setSecret(String secret) {
        super.setSecret(secret);
        hook.update(this);
    }

    @Override
    public void setLocked(boolean lock) {
        super.setLocked(lock);
        hook.update(this);
    }

}
