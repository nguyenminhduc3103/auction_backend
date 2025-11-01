package vn.team9.auction_system.auction.event.trigger;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import vn.team9.auction_system.auction.model.Auction;

@Getter
public class AutoBidTriggeredEvent extends ApplicationEvent {
    private final Auction auction;

    public AutoBidTriggeredEvent(Object source, Auction auction) {
        super(source);
        this.auction = auction;
    }
}
