package com.membertransfer.e2e.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClubsDocument {

    private String description;
    private List<ClubEntry> clubs = new ArrayList<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ClubEntry> getClubs() {
        return clubs;
    }

    public void setClubs(List<ClubEntry> clubs) {
        this.clubs = clubs != null ? clubs : new ArrayList<>();
    }
}
