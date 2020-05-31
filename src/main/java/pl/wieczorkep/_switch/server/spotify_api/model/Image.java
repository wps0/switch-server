package pl.wieczorkep._switch.server.spotify_api.model;

import lombok.Data;

@Data
public class Image {
    /**
     * The image height in pixels. If unknown: null or not returned.
     */
    private int height;
    /**
     * The source URL of the image.
     */
    private String url;
    /**
     * The image width in pixels. If unknown: null or not returned.
     */
    private int width;
}
