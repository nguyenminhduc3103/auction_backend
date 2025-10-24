package vn.team9.auction_system.auction.mapper;

import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.common.dto.auction.AuctionRequest;
import vn.team9.auction_system.common.dto.auction.AuctionResponse;
import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuctionMapper {

    // AuctionRequest -> Auction entity
    Auction toEntity(AuctionRequest request);

    // Auction entity -> AuctionResponse
    @Mapping(source = "product.productId", target = "productId")
    AuctionResponse toResponse(Auction auction);

    // BidRequest -> Bid entity
    Bid toEntity(BidRequest request);

    // Bid entity -> BidResponse
    @Mapping(source = "auction.auctionId", target = "auctionId")
    @Mapping(source = "bidder.userId", target = "bidderId")
    BidResponse toResponse(Bid bid);
}