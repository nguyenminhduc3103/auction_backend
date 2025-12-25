package vn.team9.auction_system.auction.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import vn.team9.auction_system.auction.event.trigger.*;

@Slf4j
@Component
public class AuctionEventListener {

    @EventListener
    public void handleOutbid(OutbidEvent event) {
        log.info("User {} got outbid in auction {}",
                event.getOutbidUser().getUsername(),
                event.getAuction().getAuctionId());
    }

    @EventListener
    public void handleAutoBidTriggered(AutoBidTriggeredEvent event) {
        log.info("Auto-bid reactivated for auction {}",
                event.getAuction().getAuctionId());
    }

    @EventListener
    public void handleAuctionEnded(AuctionEndedEvent event) {
        log.info("Auction {} has ended.",
                event.getAuction().getAuctionId());
    }
}