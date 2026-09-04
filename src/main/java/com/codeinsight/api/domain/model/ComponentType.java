package com.codeinsight.api.domain.model;

public enum ComponentType {
    CONTROLLER("Controllers / Endpoints"),
    SERVICE("Services / Use Cases"),
    REPOSITORY("Repositories / Persistence"),
    ENTITY("Entities / Domain Models"),
    CONFIGURATION("Configurations / Infrastructure Beans"),
    COMPONENT("Generic Components"),
    FRONTEND_COMPONENT("Frontend Components"),
    FRONTEND_SERVICE("Frontend Services / Injectables");

    private final String description;

    ComponentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
