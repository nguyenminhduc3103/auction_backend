package vn.team9.auction_system.common.service;

import vn.team9.auction_system.common.dto.product.WonProductResponse;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionRequest;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionResponse;
import java.util.List;

public interface ITransactionAfterAuctionService {
    TransactionAfterAuctionResponse updateTransactionStatus(Long id, String status);
    List<TransactionAfterAuctionResponse> getTransactionsByUser(Long userId);

    TransactionAfterAuctionResponse getTransactionByAuction(Long auctionId);
    TransactionAfterAuctionResponse cancelTransaction(Long txnId, String reason);
    public List<WonProductResponse> getWonProducts(Long userId,String status, Long txnId);
}
