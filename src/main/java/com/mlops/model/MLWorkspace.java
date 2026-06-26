package com.mlops.model;

import java.util.ArrayList;
import java.util.List;

public class MLWorkspace {

    private String id;
    private String teamName;
    private int storageQuotaGb;
    // list of model IDs that belong to this workspace
    private List<String> modelIds;

    // no-arg constructor is needed by Jackson for deserialisation
    public MLWorkspace() {
        this.modelIds = new ArrayList<>();
    }

    public MLWorkspace(String id, String teamName, int storageQuotaGb) {
        this.id = id;
        this.teamName = teamName;
        this.storageQuotaGb = storageQuotaGb;
        this.modelIds = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getStorageQuotaGb() {
        return storageQuotaGb;
    }

    public void setStorageQuotaGb(int storageQuotaGb) {
        this.storageQuotaGb = storageQuotaGb;
    }

    public List<String> getModelIds() {
        return modelIds;
    }

    public void setModelIds(List<String> modelIds) {
        this.modelIds = modelIds;
    }
}
