package com.aaomidi.mcauthenticator.auth;

/**
 * @author Joseph Hirschfeld <joe@ibj.io>
 * @date 2/28/16
 *
 * Represents anything that authenticates
 *
 * @since 1.1
 */
public interface Authenticator {

    boolean authenticate(String secret, String input) throws Exception;

    //Caters to yubikey auto-public key detection
    String secret(String lastSecret, String input);

}
