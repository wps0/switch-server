package pl.wieczorkep._switch.server.spotify_api.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Album {
    @SerializedName("album_type")
    private String albumType;
    private ExternalUrl[] artists;
    @SerializedName("available_markets")
    private String[] availableMarkets;
    @SerializedName("external_urls")
    private ExternalUrl externalUrls;
    private String href;
    private String id;
    private Image[] images;
    private String name;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("release_date_precision")
    private String releaseDatePrecision;
    @SerializedName("total_tracks")
    private int totalTracks;
    private String uri;
}
