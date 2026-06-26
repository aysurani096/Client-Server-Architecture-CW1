package com.mlops.store;

import com.mlops.model.EvaluationMetric;
import com.mlops.model.MachineLearningModel;
import com.mlops.model.MLWorkspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// singleton class to act as our in-memory "database"
// all resource classes share the same instance so data persists between requests
public class DataStore {

    private static DataStore instance;

    private final Map<String, MLWorkspace> workspaces = new HashMap<>();
    private final Map<String, MachineLearningModel> models = new HashMap<>();
    // metrics are stored per model - key is modelId, value is the list of metrics for that model
    private final Map<String, List<EvaluationMetric>> metrics = new HashMap<>();

    private DataStore() {}

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public Map<String, MLWorkspace> getWorkspaces() {
        return workspaces;
    }

    public Map<String, MachineLearningModel> getModels() {
        return models;
    }

    public Map<String, List<EvaluationMetric>> getMetrics() {
        return metrics;
    }

    // helper to initialise the metrics list for a new model
    public void initMetricsForModel(String modelId) {
        metrics.put(modelId, new ArrayList<>());
    }
}
