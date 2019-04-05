package dev.kensa;

public class KensaException extends RuntimeException {

    public KensaException(String message) {
        super(message);
    }

    public KensaException(String message, Throwable cause) {
        super(message, cause);
    }

    public KensaException(Throwable cause) {
        super(cause);
    }
}
