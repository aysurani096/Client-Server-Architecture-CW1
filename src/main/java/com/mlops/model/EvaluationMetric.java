package com.mlops.model;

public class EvaluationMetric {

    private String id;
    // storing as String (ISO-8601) - simpler than dealing with long epoch in JSON
    private String timestamp;
    private double accuracyScore;

    // Jackson needs this
    public EvaluationMetric() {}

    public EvaluationMetric(String id, String timestamp, double accuracyScore) {
        this.id = id;
        this.timestamp = timestamp;
        this.accuracyScore = accuracyScore;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getAccuracyScore() {
        return accuracyScore;
    }

    public void setAccuracyScore(double accuracyScore) {
        this.accuracyScore = accuracyScore;
    }
}
