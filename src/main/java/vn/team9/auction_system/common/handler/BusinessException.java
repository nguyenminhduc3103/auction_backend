package vn.team9.auction_system.common.handler;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}