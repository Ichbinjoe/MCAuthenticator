package com.aaomidi.mcauthenticator.auth;

import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 2/28/16
 *
 * @since 1.1
 */
public class Yubikey implements Authenticator {

    private final YubicoClient authClient;

    public Yubikey(Integer clientId, String secret) {
        this.authClient = YubicoClient.getClient(clientId, secret);
    }

    @Override
    public boolean authenticate(String secret, String input) throws Exception {
        try {
            VerificationResponse resp = authClient.verify(input);
            return resp.isOk() && (secret == null || resp.getPublicId().equals(secret));
        } catch (YubicoValidationFailure yubicoValidationFailure) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String secret(String lastSecret, String input) {
        if (lastSecret != null) return lastSecret;
        if (!YubicoClient.isValidOTPFormat(input)) return null;
        return YubicoClient.getPublicId(input);
    }
}
