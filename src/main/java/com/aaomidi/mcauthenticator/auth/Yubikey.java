package com.aaomidi.mcauthenticator.auth;

import com.aaomidi.mcauthenticator.model.User;
import com.aaomidi.mcauthenticator.model.UserData;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import com.yubico.client.v2.exceptions.YubicoVerificationException;
import org.bukkit.entity.Player;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 2/28/16
 * @since 1.1
 */
public class Yubikey implements Authenticator {

    private final YubicoClient authClient;

    public Yubikey(Integer clientId, String secret) {
        this.authClient = YubicoClient.getClient(clientId, secret);
    }

    @Override
    public boolean authenticate(User user, String input) throws Exception {
        UserData userData = user.getUserData();
        if (userData != null && userData.getAuthType() != 1) return false;
        String publicId = userData != null ? userData.getSecret() : null;
        try {
            VerificationResponse resp = authClient.verify(input);
            if (resp.isOk() && (publicId == null ||
                    resp.getPublicId().equals(publicId))) {
                if (publicId == null) {
                    user.setUserInfo(resp.getPublicId(), 1);
                }
                return true;
            } else {
                return false;
            }
        } catch (YubicoValidationFailure yvf) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (YubicoVerificationException e) {
            return false;
        }
    }

    @Override
    public boolean isFormat(String s) {
        return YubicoClient.isValidOTPFormat(s);
    }

    @Override
    public void initUser(User u, Player p) {
        //Don't care
    }

    @Override
    public void quitUser(User u, Player p) {
        //Don't care
    }
}
