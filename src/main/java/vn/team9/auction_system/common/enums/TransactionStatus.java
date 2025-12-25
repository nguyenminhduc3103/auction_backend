package vn.team9.auction_system.common.enums;

public enum TransactionStatus {
    PENDING,   // Waiting for payment
    PAID,      // Payment completed
    SHIPPED,   // Seller shipped the item
    DONE,      // Transaction completed
    CANCELLED  // Transaction cancelled
}