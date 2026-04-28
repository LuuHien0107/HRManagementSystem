package vn.luuhien.springrestwithai.exception;

import java.io.Serial;

public class InvalidRequestException extends AppException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String message) {
        super(message);
    }
}
