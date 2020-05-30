package pl.wieczorkep._switch.server.spotify_api.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Artist {
    @SerializedName("external_urls")
    private ExternalUrl externalUrls;
    private String uri;
    private String href;
    private String type;
    private String id;
    private String name;
}
