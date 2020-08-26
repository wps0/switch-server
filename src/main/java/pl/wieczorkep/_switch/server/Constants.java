package pl.wieczorkep._switch.server;

public class Constants {
    private Constants() {}


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

    // --- Spotify integration ---
    public static final String CONST_SPOTIFY_AUTH_ENDPOINT = "https://accounts.spotify.com";
    public static final String CONST_SPOTIFY_API_ENDPOINT = "https://api.spotify.com/v1";
    public static final String CONST_SPOTIFY_AUTH_ENDPOINT_AUTHORIZE = CONST_SPOTIFY_AUTH_ENDPOINT + "/authorize";
    public static final String CONST_SPOTIFY_AUTH_ENDPOINT_REFRESH = CONST_SPOTIFY_AUTH_ENDPOINT + "/api/token";
    public static final String CONST_SPOTIFY_HTTPS_ALGORITHM_DEFAULT = "TLSv1.2";
    public static final String CONST_SPOTIFY_HTTPS_SECURE_RANDOM_ALGORITHM = "PKCS11";
    public static final String CONST_SPOTIFY_HTTPS_TRUST_MANAGER_ALGORITHM = "PKIX";
    public static final String CONST_SPOTIFY_HTTPS_KEY_MANAGER_ALGORITHM = "PKIX";
    public static final String CONST_SPOTIFY_HTTPS_ROOT_PATH = "/callback";
    /** Free port as of 24.08.2020 */
    public static final String SPOTIFY_HTTPS_PORT = PREFIX_SPOTIFY + "https-server.port";
    public static final String SPOTIFY_HTTPS_IP = PREFIX_SPOTIFY + "https-server.ip";
    public static final String SPOTIFY_HTTPS_HOSTNAME = PREFIX_SPOTIFY + "https-server.hostname";
    public static final String SPOTIFY_HTTPS_BACKLOG = PREFIX_SPOTIFY + "https-server.waiting-queue-size";
    public static final String SPOTIFY_HTTPS_ALGORITHM = PREFIX_SPOTIFY + "https-server.encryption-algorithm";
}
