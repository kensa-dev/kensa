package dev.kensa;

public class KensaException extends RuntimeException {

    static final long serialVersionUID = -1;

    public KensaException(String message) {
        super(message);
    }

    public KensaException(String message, Throwable cause) {
        super(message, cause);
    }
}
