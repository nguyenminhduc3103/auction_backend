package vn.team9.auction_system.auction.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.event.trigger.*;
import vn.team9.auction_system.user.model.User;

@Component
@RequiredArgsConstructor
public class AuctionEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishOutbidEvent(Auction auction, User outbidUser) {
        publisher.publishEvent(new OutbidEvent(this, auction, outbidUser));
    }

    public void publishAutoBidTriggeredEvent(Auction auction) {
        publisher.publishEvent(new AutoBidTriggeredEvent(this, auction));
    }

    public void publishAuctionEndedEvent(Auction auction) {
        publisher.publishEvent(new AuctionEndedEvent(this, auction));
    }
}
