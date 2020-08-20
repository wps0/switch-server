package pl.wieczorkep._switch.server.integration.spotify.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Item {
    private Album album;
    private Artist[] artists;
    @SerializedName("available_markets")
    private String[] availableMarkets;
    @SerializedName("disc_number")
    private int discNumber;
    @SerializedName("duration_ms")
    private long durationMs;
    private boolean explicit;
    @SerializedName("external_ids")
    private ExternalIds externalIds;
    @SerializedName("external_urls")
    private ExternalUrl externalUrls;
    private String id;
    @SerializedName("is_local")
    private boolean local;
    private String name;
    private int popularity;
    @SerializedName("preview_url")
    private String previewUrl;
    @SerializedName("track_number")
    private int trackNumber;
    private String type;
    private String uri;
}
