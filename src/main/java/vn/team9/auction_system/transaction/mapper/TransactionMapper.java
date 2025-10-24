package vn.team9.auction_system.transaction.mapper;

import vn.team9.auction_system.transaction.model.AccountTransaction;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionRequest;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionResponse;
import vn.team9.auction_system.common.dto.account.AccountTransactionRequest;
import vn.team9.auction_system.common.dto.account.AccountTransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    // TransactionRequest -> TransactionAfterAuction entity
    vn.team9.auction_system.transaction.model.TransactionAfterAuction toEntity(TransactionAfterAuctionRequest request);

    // TransactionAfterAuction entity -> TransactionResponse
    @Mapping(source = "buyer.userId", target = "buyerId")
    @Mapping(source = "seller.userId", target = "sellerId")
    TransactionAfterAuctionResponse toResponse(vn.team9.auction_system.transaction.model.TransactionAfterAuction entity);

    // AccountTransactionRequest -> AccountTransaction entity
    AccountTransaction toEntity(AccountTransactionRequest request);

    // AccountTransaction entity -> AccountTransactionResponse
    @Mapping(source = "user.userId", target = "userId")
    AccountTransactionResponse toResponse(AccountTransaction entity);
}
