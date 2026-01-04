package ru.teamscore.busroutes.model.enums;

import lombok.Getter;

@Getter
public enum ItemType {

    STOP("Stop"),
    ROUTE("Route");

    private final String name;

    ItemType(String name) {
        this.name = name;
    }
}
