package pl.wieczorkep._switch.server.core.spotify_api.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Device {
    private String id;
    @SerializedName("is_active")
    private boolean active;
    @SerializedName("is_private_session")
    private boolean privateSession;
    @SerializedName("is_restricted")
    private boolean restricted;
    private String name;
    private String type;
    @SerializedName("volume_percent")
    private int volumePercent;
}
