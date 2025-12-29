package vn.team9.auction_system.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.common.dto.notification.NotificationRequest;
import vn.team9.auction_system.common.service.INotificationService;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;
import vn.team9.auction_system.user.model.User;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TransactionNotificationService {

    private final INotificationService notificationService;

    /**
     * Notify Buyer payment success (Money moved to escrow)
     */
    public void notifyBuyerPaymentSuccess(TransactionAfterAuction txn) {
        try {
            User buyer = txn.getBuyer();
            String productName = txn.getAuction().getProduct().getName();
            String message = String.format(
                    "✅ Thanh toán thành công cho sản phẩm '%s'. Số tiền ₫%s đã được chuyển vào tài khoản đảm bảo.",
                    productName,
                    txn.getAmount().toPlainString());

            NotificationRequest request = NotificationRequest.builder()
                    .userId(buyer.getUserId())
                    .title("✅ Thanh toán thành công")
                    .message(message)
                    .type("PAYMENT")
                    .category("PAYMENT_SUCCESS")
                    .priority("HIGH")
                    .actionUrl("/user/bid/won-products/order/" + txn.getTransactionId())
                    .actionLabel("Xem giao dịch")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent payment success notification to buyer: {}", buyer.getUserId());
        } catch (Exception e) {
            log.error("Failed to send payment success notification", e);
        }
    }

    /**
     * Notify Seller that Buyer has paid
     */
    public void notifySellerPaymentReceived(TransactionAfterAuction txn) {
        try {
            User seller = txn.getSeller();
            String buyerName = txn.getBuyer().getFullName();
            String productName = txn.getAuction().getProduct().getName();

            String message = String.format(
                    "💰 Người mua %s đã thanh toán ₫%s cho sản phẩm '%s'. Vui lòng chuẩn bị giao hàng.",
                    buyerName,
                    txn.getAmount().toPlainString(),
                    productName);

            NotificationRequest request = NotificationRequest.builder()
                    .userId(seller.getUserId())
                    .title("💰 Đã nhận thanh toán")
                    .message(message)
                    .type("PAYMENT")
                    .category("PAYMENT_RECEIVED")
                    .priority("HIGH")
                    .actionUrl("/seller/orders")
                    .actionLabel("Xử lý đơn hàng")
                    .build();

            notificationService.sendNotification(request);
            log.info("Sent payment received notification to seller: {}", seller.getUserId());
        } catch (Exception e) {
            log.error("Failed to send payment received notification", e);
        }
    }

    /**
     * Notify Transaction Completed (Money released)
     */
    public void notifyTransactionCompleted(TransactionAfterAuction txn) {
        try {
            String productName = txn.getAuction().getProduct().getName();

            // Notify Buyer
            NotificationRequest buyerReq = NotificationRequest.builder()
                    .userId(txn.getBuyer().getUserId())
                    .title("✅ Giao dịch hoàn tất")
                    .message("Giao dịch cho sản phẩm '" + productName + "' đã hoàn tất. Cảm ơn bạn đã mua hàng!")
                    .type("SYSTEM")
                    .category("TRANSACTION_COMPLETED")
                    .priority("MEDIUM")
                    .actionUrl("/user/bid/won-products/order/" + txn.getTransactionId())
                    .actionLabel("Xem chi tiết")
                    .build();
            notificationService.sendNotification(buyerReq);

            // Notify Seller
            NotificationRequest sellerReq = NotificationRequest.builder()
                    .userId(txn.getSeller().getUserId())
                    .title("✅ Giao dịch hoàn tất")
                    .message("Giao dịch cho sản phẩm '" + productName
                            + "' đã hoàn tất. Tiền đã được chuyển vào số dư của bạn.")
                    .type("SYSTEM")
                    .category("TRANSACTION_COMPLETED")
                    .priority("MEDIUM")
                    .actionUrl("/seller/orders")
                    .actionLabel("Xem chi tiết")
                    .build();
            notificationService.sendNotification(sellerReq);

            log.info("Sent transaction completed notifications for txn: {}", txn.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to send transaction completed notifications", e);
        }
    }

    /**
     * Notify Transaction Cancelled
     */
    public void notifyTransactionCancelled(TransactionAfterAuction txn) {
        try {
            String productName = txn.getAuction().getProduct().getName();
            String message = String.format(
                    "❌ Giao dịch cho sản phẩm '%s' đã bị hủy. Tiền đã được hoàn lại (nếu có).",
                    productName);

            // Notify Buyer
            NotificationRequest buyerReq = NotificationRequest.builder()
                    .userId(txn.getBuyer().getUserId())
                    .title("❌ Giao dịch bị hủy")
                    .message(message)
                    .type("SYSTEM")
                    .category("TRANSACTION_CANCELLED")
                    .priority("HIGH")
                    .actionUrl("/user/bid/won-products/order/" + txn.getTransactionId())
                    .actionLabel("Xem chi tiết")
                    .build();
            notificationService.sendNotification(buyerReq);

            // Notify Seller
            NotificationRequest sellerReq = NotificationRequest.builder()
                    .userId(txn.getSeller().getUserId())
                    .title("❌ Giao dịch bị hủy")
                    .message(message)
                    .type("SYSTEM")
                    .category("TRANSACTION_CANCELLED")
                    .priority("HIGH")
                    .actionUrl("/seller/orders")
                    .actionLabel("Xem chi tiết")
                    .build();
            notificationService.sendNotification(sellerReq);

            log.info("Sent transaction cancelled notifications for txn: {}", txn.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to send transaction cancelled notifications", e);
        }
    }
}
