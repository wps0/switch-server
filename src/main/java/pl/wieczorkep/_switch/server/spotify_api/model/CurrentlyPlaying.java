package pl.wieczorkep._switch.server.spotify_api.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CurrentlyPlaying {
    private Device device;
    @SerializedName("repeat_state")
    private String repeatState;
    @SerializedName("shuffle_state")
    private boolean isShuffled;
    private Context context;
    private long timestamp;
    @SerializedName("progress_ms")
    private long progressMs;
    @SerializedName("is_playing")
    private boolean isPlaying;
    private Item item;
    @SerializedName("currently_playing_type")
    private String currentlyPlayingType;
    private Actions actions;
}
