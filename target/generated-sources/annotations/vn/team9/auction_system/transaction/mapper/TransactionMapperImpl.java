package vn.team9.auction_system.transaction.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.common.dto.account.AccountTransactionRequest;
import vn.team9.auction_system.common.dto.account.AccountTransactionResponse;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionRequest;
import vn.team9.auction_system.common.dto.transaction.TransactionAfterAuctionResponse;
import vn.team9.auction_system.transaction.model.AccountTransaction;
import vn.team9.auction_system.transaction.model.TransactionAfterAuction;
import vn.team9.auction_system.user.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-30T23:18:08+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionAfterAuction toEntity(TransactionAfterAuctionRequest request) {
        if ( request == null ) {
            return null;
        }

        TransactionAfterAuction transactionAfterAuction = new TransactionAfterAuction();

        transactionAfterAuction.setAmount( request.getAmount() );

        return transactionAfterAuction;
    }

    @Override
    public TransactionAfterAuctionResponse toResponse(TransactionAfterAuction entity) {
        if ( entity == null ) {
            return null;
        }

        TransactionAfterAuctionResponse transactionAfterAuctionResponse = new TransactionAfterAuctionResponse();

        transactionAfterAuctionResponse.setBuyerId( entityBuyerUserId( entity ) );
        transactionAfterAuctionResponse.setSellerId( entitySellerUserId( entity ) );
        transactionAfterAuctionResponse.setUpdatedAt( entity.getUpdatedAt() );
        transactionAfterAuctionResponse.setAmount( entity.getAmount() );
        transactionAfterAuctionResponse.setStatus( entity.getStatus() );

        return transactionAfterAuctionResponse;
    }

    @Override
    public AccountTransaction toEntity(AccountTransactionRequest request) {
        if ( request == null ) {
            return null;
        }

        AccountTransaction.AccountTransactionBuilder accountTransaction = AccountTransaction.builder();

        accountTransaction.amount( request.getAmount() );
        accountTransaction.type( request.getType() );

        return accountTransaction.build();
    }

    @Override
    public AccountTransactionResponse toResponse(AccountTransaction entity) {
        if ( entity == null ) {
            return null;
        }

        AccountTransactionResponse accountTransactionResponse = new AccountTransactionResponse();

        accountTransactionResponse.setUserId( entityUserUserId( entity ) );
        accountTransactionResponse.setCreatedAt( entity.getCreatedAt() );
        accountTransactionResponse.setAmount( entity.getAmount() );
        accountTransactionResponse.setStatus( entity.getStatus() );
        accountTransactionResponse.setType( entity.getType() );

        return accountTransactionResponse;
    }

    private Long entityBuyerUserId(TransactionAfterAuction transactionAfterAuction) {
        if ( transactionAfterAuction == null ) {
            return null;
        }
        User buyer = transactionAfterAuction.getBuyer();
        if ( buyer == null ) {
            return null;
        }
        Long userId = buyer.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private Long entitySellerUserId(TransactionAfterAuction transactionAfterAuction) {
        if ( transactionAfterAuction == null ) {
            return null;
        }
        User seller = transactionAfterAuction.getSeller();
        if ( seller == null ) {
            return null;
        }
        Long userId = seller.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private Long entityUserUserId(AccountTransaction accountTransaction) {
        if ( accountTransaction == null ) {
            return null;
        }
        User user = accountTransaction.getUser();
        if ( user == null ) {
            return null;
        }
        Long userId = user.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }
}
