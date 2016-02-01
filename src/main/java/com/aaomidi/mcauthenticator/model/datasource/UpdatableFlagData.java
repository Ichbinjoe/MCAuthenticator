package com.aaomidi.mcauthenticator.model.datasource;

import java.net.InetAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 1/30/16
 */
public class UpdatableFlagData extends BasicUserData {

    public UpdatableFlagData(UpdateHook hook, UUID id, InetAddress inetAddress, String secret, boolean locked) {
        super(id, inetAddress, secret, locked);
        this.hook = hook;
    }

    private final UpdateHook hook;

    @Override
    public void setLastAddress(InetAddress inetAddress) {
        InetAddress a = getLastAddress();
        super.setLastAddress(inetAddress);
        if(a != inetAddress) hook.update(this);
    }

    @Override
    public void setSecret(String secret) {
        String a = getSecret();
        super.setSecret(secret);
        if(!Objects.equals(a, secret)) hook.update(this);
    }

    @Override
    public void setLocked(boolean lock) {
        boolean a = isLocked(null);
        super.setLocked(lock);
        if (a != lock) hook.update(this);
    }

}
