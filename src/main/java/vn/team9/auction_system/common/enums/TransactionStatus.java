package vn.team9.auction_system.common.enums;

public enum TransactionStatus {
    PENDING,   // chờ thanh toán
    PAID,      // đã thanh toán
    SHIPPED,   // người bán đã giao hàng
    DONE,      // hoàn tất giao dịch
    CANCELLED  // giao dịch bị hủy
}
