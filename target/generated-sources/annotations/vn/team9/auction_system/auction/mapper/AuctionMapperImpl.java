package vn.team9.auction_system.auction.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.common.dto.auction.AuctionRequest;
import vn.team9.auction_system.common.dto.auction.AuctionResponse;
import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import vn.team9.auction_system.product.model.Product;
import vn.team9.auction_system.user.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-28T01:15:30+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class AuctionMapperImpl implements AuctionMapper {

    @Override
    public Auction toEntity(AuctionRequest request) {
        if ( request == null ) {
            return null;
        }

        Auction auction = new Auction();

        auction.setEndTime( request.getEndTime() );
        auction.setStartTime( request.getStartTime() );

        return auction;
    }

    @Override
    public AuctionResponse toResponse(Auction auction) {
        if ( auction == null ) {
            return null;
        }

        AuctionResponse auctionResponse = new AuctionResponse();

        auctionResponse.setProductId( auctionProductProductId( auction ) );
        auctionResponse.setAuctionId( auction.getAuctionId() );
        auctionResponse.setBidStepAmount( auction.getBidStepAmount() );
        auctionResponse.setEndTime( auction.getEndTime() );
        auctionResponse.setStartTime( auction.getStartTime() );
        auctionResponse.setStatus( auction.getStatus() );

        return auctionResponse;
    }

    @Override
    public Bid toEntity(BidRequest request) {
        if ( request == null ) {
            return null;
        }

        Bid bid = new Bid();

        bid.setBidAmount( request.getBidAmount() );
        bid.setStepAutoBidAmount( request.getStepAutoBidAmount() );

        return bid;
    }

    @Override
    public BidResponse toResponse(Bid bid) {
        if ( bid == null ) {
            return null;
        }

        BidResponse.BidResponseBuilder bidResponse = BidResponse.builder();

        bidResponse.auctionId( bidAuctionAuctionId( bid ) );
        bidResponse.bidderId( bidBidderUserId( bid ) );
        bidResponse.bidAmount( bid.getBidAmount() );
        bidResponse.createdAt( bid.getCreatedAt() );
        bidResponse.stepAutoBidAmount( bid.getStepAutoBidAmount() );

        return bidResponse.build();
    }

    private Long auctionProductProductId(Auction auction) {
        if ( auction == null ) {
            return null;
        }
        Product product = auction.getProduct();
        if ( product == null ) {
            return null;
        }
        Long productId = product.getProductId();
        if ( productId == null ) {
            return null;
        }
        return productId;
    }

    private Long bidAuctionAuctionId(Bid bid) {
        if ( bid == null ) {
            return null;
        }
        Auction auction = bid.getAuction();
        if ( auction == null ) {
            return null;
        }
        Long auctionId = auction.getAuctionId();
        if ( auctionId == null ) {
            return null;
        }
        return auctionId;
    }

    private Long bidBidderUserId(Bid bid) {
        if ( bid == null ) {
            return null;
        }
        User bidder = bid.getBidder();
        if ( bidder == null ) {
            return null;
        }
        Long userId = bidder.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }
}
