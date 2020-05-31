package pl.wieczorkep._switch.server.spotify_api.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Album {
    /**
     * The type of the album: one of "album" , "single" , or "compilation".
     */
    @SerializedName("album_type")
    private String albumType;
    /**
     * The artists of the album. Each artist object includes a link in href to more detailed information about the
     * artist.
     */
    private Artist[] artists;
    @SerializedName("available_markets")
    private String[] availableMarkets;
    @SerializedName("external_urls")
    private ExternalUrl externalUrls;
    /**
     * A link to the Web API endpoint providing full details of the album.
     */
    private String href;
    private String id;
    private Image[] images;
    private String name;
    /**
     * The date the album was first released, for example 1981. Depending on the precision, it might be shown as 1981-12
     * or 1981-12-15.
     */
    @SerializedName("release_date")
    private String releaseDate;
    /**
     * The precision with which release_date value is known: year, month, or day.
     */
    @SerializedName("release_date_precision")
    private String releaseDatePrecision;
    @SerializedName("total_tracks")
    private int totalTracks;
    private String uri;
}
