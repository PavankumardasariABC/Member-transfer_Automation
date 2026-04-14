package com.membertransfer.e2e.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One club row from {@code e2e/clubs.json}. Optional fields are ignored at runtime if absent.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClubEntry {

    private String clubNumber;
    private String organizationName;
    private String organizationId;
    private String locationName;
    private String locationId;
    private List<String> tags = new ArrayList<>();
    private Map<String, String> links = new LinkedHashMap<>();

    public String getClubNumber() {
        return clubNumber;
    }

    public void setClubNumber(String clubNumber) {
        this.clubNumber = clubNumber;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links != null ? links : new LinkedHashMap<>();
    }
}
