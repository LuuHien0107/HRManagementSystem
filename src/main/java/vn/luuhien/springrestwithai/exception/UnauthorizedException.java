package vn.luuhien.springrestwithai.exception;

import java.io.Serial;

public class UnauthorizedException extends AppException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(message);
    }
}
