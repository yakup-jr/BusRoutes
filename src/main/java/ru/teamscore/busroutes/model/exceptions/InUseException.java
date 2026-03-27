package ru.teamscore.busroutes.model.exceptions;

public class InUseException extends RuntimeException {
    public InUseException(String message) {
        super(message);
    }
}
