package ru.teamscore.busroutes.model.exceptions;

import ru.teamscore.busroutes.model.enums.ItemType;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String item, ItemType type) {
        super(String.format("%s %s not found", type.getName(), item));
    }
}
