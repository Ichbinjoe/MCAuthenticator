package com.aaomidi.mcauthenticator.auth;

import com.aaomidi.mcauthenticator.MCAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticator;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 2/28/16
 *
 * @since 1.1
 */
public class RFC6238 implements Authenticator {

    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private static final transient String googleFormat = "https://www.google.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s";
     private final MCAuthenticator mcAuthenticator;

    public RFC6238(MCAuthenticator mcAuthenticator) {
        this.mcAuthenticator = mcAuthenticator;
    }

    @Override
    public boolean authenticate(String secret, String input) {
        Integer code;
        try {
            code = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        }

        return gAuth.authorize(secret, code);
    }

    @Override
    public String secret(String lastSecret, String input) {
        return lastSecret; //Should never not be our last secret
    }

    public String getQRUrl(String username, String secret) {
        if (secret == null)
            return null;
        return String.format(googleFormat, username, mcAuthenticator.getC().getServerIP(), secret);
    }

    public String createNewKey() {
        return gAuth.createCredentials().getKey();
    }
}
