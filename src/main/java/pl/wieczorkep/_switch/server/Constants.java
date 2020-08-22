package pl.wieczorkep._switch.server;

public class Constants {
    private Constants() {}

    // ==!== Config variables ==!==
    // --- Prefixes ---
    public static final String PREFIX_ACTION = "actions.";
    public static final String PREFIX_FILESYSTEM = "fs.";
    public static final String PREFIX_SPOTIFY = PREFIX_ACTION + "spotify.";

    // --- Filesystem ---
    public static final String CONFIG_DIR = PREFIX_FILESYSTEM + "config_dir";
    public static final String SONGS_DIR = PREFIX_FILESYSTEM + "songs_dir";
    public static final String CONFIG_FILE = PREFIX_FILESYSTEM + "config_file";

    // --- Actions ---
    public static final String ACTIONS_FILE = PREFIX_ACTION + "registry";
    public static final String ACTIONS_DIR = PREFIX_ACTION + "dir";

    public static final String ACTION_SPOTIFY_APPID = PREFIX_SPOTIFY + "app-id";
    public static final String ACTION_SPOTIFY_APPSECRET = PREFIX_SPOTIFY + "app-secret";
    public static final String ACTION_SPOTIFY_AUTHSCOPES = PREFIX_SPOTIFY + "auth-scope";
    public static final String ACTION_SPOTIFY_CLIENT_TOKEN_REFRESH = PREFIX_SPOTIFY + "client.refresh-token";
    public static final String ACTION_SPOTIFY_CLIENT_TOKEN = PREFIX_SPOTIFY + "client.token";
    public static final String ACTION_SPOTIFY_CLIENT_TOKEN_VALIDITY = PREFIX_SPOTIFY + "client.token.validity";
    public static final String ACTION_SPOTIFY_CLIENT_TOKEN_LAST_REFRESH = PREFIX_SPOTIFY + "client.token.last-refresh";
    public static final String ACTION_SPOTIFY_CLIENT_TMPCODE = PREFIX_SPOTIFY + "client.tmp-code";
    public static final String ACTION_SPOTIFY_CLIENT_DEFAULTDEVICE = PREFIX_SPOTIFY + "client.default-device";

}
