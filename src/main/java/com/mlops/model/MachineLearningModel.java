package com.mlops.model;

public class MachineLearningModel {

    // possible states a model can be in
    public enum ModelStatus {
        TRAINING,
        DEPLOYED,
        DEPRECATED
    }

    private String id;
    private String framework;
    private ModelStatus status;
    private double latestAccuracy;
    // which workspace this model lives in
    private String workspaceId;

    // Jackson needs this
    public MachineLearningModel() {}

    public MachineLearningModel(String id, String framework, ModelStatus status,
                                 double latestAccuracy, String workspaceId) {
        this.id = id;
        this.framework = framework;
        this.status = status;
        this.latestAccuracy = latestAccuracy;
        this.workspaceId = workspaceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public ModelStatus getStatus() {
        return status;
    }

    public void setStatus(ModelStatus status) {
        this.status = status;
    }

    public double getLatestAccuracy() {
        return latestAccuracy;
    }

    public void setLatestAccuracy(double latestAccuracy) {
        this.latestAccuracy = latestAccuracy;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
}
