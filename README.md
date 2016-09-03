# MCAuthenticator

MCAuthenticator is a Bukkit API based plugin designed to allow for 2 Factor 
Authentication (otherwise known as TOTP, or the Time-Based One-Time Password 
Algorithm ([RFC6238](https://tools.ietf.org/html/rfc6238))) for all players on a server to optionally use, with configurable 
enforcement for certain players through permission nodes. This is designed to
allow for more secure minecraft server environments, especially when account
cracking and hacking are so prevalent. This plugin reduces the risk of a 
compromised staff account greatly, making it much harder to compromise a 
staff account and compromise a server in general.

## Popular 2FA (TOTP) Apps by Mobile Device Operating System

### Windows Phone

+ [Authenticator](https://www.microsoft.com/store/apps/authenticator/9wzdncrfj3rj)

### Android

+ [Authy](https://play.google.com/store/apps/details?id=com.authy.authy&hl=en)
+ [Google Authenticator](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2&hl=en)

### iOS

+ [Authy](https://itunes.apple.com/us/app/authy/id494168017?mt=8)
+ [Google Authenticator](https://itunes.apple.com/us/app/google-authenticator/id388497605?mt=8)

## Installation

To install MCAuthenticator, you simply put the `MCAuthenticator.jar` within
the `plugins/` folder of your Bukkit/Spigot/BungeeCord server. Once you start the 
server, the plugin will generate a configuration as described below.

### Compatibility

[![Build Status](https://travis-ci.org/Ichbinjoe/MCAuthenticator.svg?branch=master)](https://travis-ci.org/Ichbinjoe/MCAuthenticator)

MCAuthenticator is tested with Spigot 1.8.8, on Java 8. However, MCAuthenticator
is compatible with Java 7, and there is no reason it shouldn't work with Bukkit
1.6.x or 1.7.x servers. We will officially support 1.6.x spigot and up.

If you find issues on any server versions 1.6.x and up, please report them in
issues.

## Commands

MCAuthenticator uses 1 root command, /auth (with aliases /2fa, 
/authenticator, and /mcauthenticator) with a set of sub commands:

+ /auth enable \[player\] - Enables 2FA on an account, or your own account if
 a player isn't specified.
+ /auth disable \[player\] - Disables 2FA on an account, or your own account 
if a player isn't specified.
+ /auth reset \[player\] - Keeps 2FA enabled, but resets the 2FA on your 
account or another player's if player is specified.
+ /auth reload - Reloads the configuration, then re-authenticates all players 
based on configured authentication rules.

## Permissions

MCAuthenticator uses the root permission `mcauthenticator`, with a set of sub
permissions to control the behaviour of various aspects of the plugin.

#### Explanation of defaults
Each permission has one of two defaults: op and console only. If a permission
is defaulting to console only, even if a player has op, they will not be able
to perform the command unless they __explicitly__ have the listed permission.

### Command controlling permissions
+ `mcauthenticator.use` (default: op) - Allows for the root use of the /auth
 command. In order to do anything with the /auth command, the player __must__
 have this permission.
+ `mcauthenticator.enable` (default: op) - Allows for the user to do /auth 
 enable on themselves. This does not control whether the player is allowed to 
 have 2FA enabled on their account; if console or a player with `mcauthenticator.enable.other` 
 enables 2FA on their account, the plugin will let them.
+ `mcauthenticator.disable` (default: op) - Allows for the user to do /auth 
 disable on themselves.
+ `mcauthenticator.enable.other` (default: console only) - Allows for the user 
 to perform /auth enable <player>, enabling authentication for another player.
 The other player does not need to have `mcauthenticator.enable` in order to 
 have 2FA enabled on their account.
+ `mcauthenticator.disable.other` (default: console only) - Allows for the user
 to perform /auth disable <player>, disabling another player's authentication.
+ `mcauthenticator.reset` (default: op) - Allows for a player to reset their
 own account's authentication.
+ `mcauthenticator.reset.other` (default: console only) - Allows for the user
 to reset other user's authentications.
+ `mcauthenticator.reload` (default: console only) - Allows for the user to
 reload the configuration
 
### Enforcement Permissions
+ `mcauthenticator.lock` (default: op) - Does one of 2 things:
  1. Forces the user to always 2FA enabled, not allowing them to disable or
    reset their own authentication
  2. Does not allow anyone but console to reset or disable their authentication.
    Even if the user has any of the `.other` permission nodes, the plugin will
    __not__ allow them to perform any changes to authentication. This should
    be given to any staff members, as it will prevent any other compromised
    account from then chainly compromising other staff (or important persons)
    accounts.

## Configuration

The configuration of the plugin can be default out of the box, but it is highly
suggested that at least some of the configuration defaults be customized to your
server.

All of the configuration is located in `plugins/MCAuthenticator/config.yml`. Each
of the configuration options listed below are all located within this yml.

### Available Authentication Mechanisms

Since `1.1.0`, MCAuthentication has supported multiple types of
authentication mechanisms:

+ 2fa
+ Yubikey

Each of these can be turned on and off within the config file under
`authenticators`. When setting up 2FA, the user may use any of the
configured authentication mechanisms. The plugin will auto-detect which
mechanism they use, and authenticate them off of only that method in the
future.

### Yubikey

In order to offer authentication through Yubikey, you must have a
`clientId` as well as a `clientSecret`. Both of these are offered at
https://upgrade.yubico.com/getapikey/. If you do not add these
api credentials, Yubikey authentication will not function correctly.
These should be entered under `yubikey` within the configuration.

### GAuth

The `serverIp` field specifies what will be displayed to the user within their
2FA app.

For example, with a username `Ichbinjoe` and `serverIp` set to `ibj.io`, a sample
GAuth entry will look like the following:

![Sample GAuth Entry](https://ibj.io/imgs/example_auth.png "Sample GAuth Entry")

### Forcing Same IP Authentication

By default, MCAuthenticator does not force a player to re-authenticate if they rejoin
from the last IP they were authenticated from. This can be disabled by setting
`forceSameIPAuthentication` to true. When set to true, the plugin force the user to 
re-authenticate through 2FA each and every time they log into the server.

### Message customization

Under the `messages` section of the configuration, MCAuthenticator allows you to 
customize all publicly displayed messages, in order to modify color schemes and 
the prefix to better fit your server's color scheme/language requirements. All
staff commands are hardcoded, simply because there is much less of a reason to
customize these messages.

Each entry supports `&<opcode>` based colorcodes, as well as the `\n` character
for splitting up messages onto multiple lines. Where contextually logical, a
`%player%` replacement has been added to customize each message based on occurrence.

### Data Source Configuration

MCAuthenticator supports 1 of 3 different data sources:

+ Single File - Stores all player data in one file
+ Directory - Stores each player in their own file within a directory
+ MySQL - Stores all player data within a table in a MySQL/MariaDB table.

#### Single File Data Source

To enable the single file data source, `dataBacking.type` should be set to `single`.
The file to which the single file data source saves to is configured by setting 
`dataBacking.file`. This file is then by default present in `plugins/MCAuthenticator`

#### Directory Data Source

To enable the directory data source, `dataBacking.type` should be set to `directory`.
The directory to which all of the player data is saved is configured by the
`dataBacking.directory` field. Within this directory, a file for each player,
structured `<uuid>.json` stores a single JSON object with the authentication
details.

#### MySQL Data Source

To enable the MySQL data source, `dataBacking.type` should be set to `mysql`.
The database connection details should then be set in `dataBacking.mysql`.
The default connection url is `jdbc:mysql://localhost:3000/db`, with the format
`jdbc:mysql://<ip>:<port>/<databaseName>`. `username` and `password` should also
be set to a username and password that is able to access the database.

## Building MCAuthenticator

To build MCAuthenticator, run `mvn package` in a command terminal, and the
result of the build will save the jar to `target/MCAuthenticator-<version>.jar`.

### BungeeCord Mode

BungeeCord support will only require one authentication per player join, not every
time the player switches servers, does not require same IP to be true, and will deny
BungeeCord commands until the player is authenticated. To enable BungeeCord support,
`enable` under the `bungee` should be set to `true`, and `channel` should be set to
the same channel as the BungeeCord plugin's `pluginChannel` is set to. BungeeCord
mode requires that the plugin also be installed on the BungeeCord instance(s).

## Contributing

We love contributions! Feel free to fork this project and  PR any changes/
enhancements! Of note, we use the MIT License, which is listed [here](https://github.com/Ichbinjoe/MCAuthenticator/blob/master/LICENSE.txt).

## Reporting Issues/Bugs or Suggesting Additions

We request that issues/ suggestions be submitted on the Github issue manager.
This allows us to be more vigilant of issues, and allow more flexibility in
managing the project.
