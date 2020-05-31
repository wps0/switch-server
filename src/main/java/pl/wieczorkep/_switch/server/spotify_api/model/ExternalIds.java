package pl.wieczorkep._switch.server.spotify_api.model;

import lombok.Data;

@Data
public class ExternalIds {
    private String isrc;
    private String ean;
    private String upc;
}
