package com.michaelshan.mykampuslite.viewmodel;

/**
 * Merepresentasikan hasil operasi data (tambah, perbarui, hapus)
 * untuk dikonsumsi oleh lapisan UI.
 */
public class OperationResult {

    public enum Status {
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final String message;
    private final String entityName;
    private final String action;

    public OperationResult(Status status, String message, String entityName, String action) {
        this.status = status;
        this.message = message;
        this.entityName = entityName;
        this.action = action;
    }

    public static OperationResult success(String message, String entityName, String action) {
        return new OperationResult(Status.SUCCESS, message, entityName, action);
    }

    public static OperationResult error(String message, String entityName, String action) {
        return new OperationResult(Status.ERROR, message, entityName, action);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getAction() {
        return action;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
