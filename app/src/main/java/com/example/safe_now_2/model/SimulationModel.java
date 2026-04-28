package com.example.safe_now_2.model;

public class SimulationModel {

    public enum Scenario {
        FIRE,
        EARTHQUAKE
    }

    private Scenario selectedScenario;

    public void setSelectedScenario(Scenario scenario) {
        this.selectedScenario = scenario;
    }

    public Scenario getSelectedScenario() {
        return selectedScenario;
    }

    public boolean isScenarioSelected() {
        return selectedScenario != null;
    }
}