package com.manager.passwordmanager.exceptions;

public class DuplicateNoteException extends RuntimeException {

    public DuplicateNoteException(String message) {
        super(message);
    }

    public DuplicateNoteException(String message, Throwable cause) {
        super(message, cause);
    }

}
