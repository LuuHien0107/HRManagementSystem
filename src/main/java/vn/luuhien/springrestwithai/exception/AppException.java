package vn.luuhien.springrestwithai.exception;

import java.io.Serial;

public abstract class AppException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AppException(String message) {
        super(message);
    }
}
