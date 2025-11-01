package vn.team9.auction_system.auction.event.trigger;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.user.model.User;

@Getter
public class OutbidEvent extends ApplicationEvent {
    private final Auction auction;
    private final User outbidUser;

    public OutbidEvent(Object source, Auction auction, User outbidUser) {
        super(source);
        this.auction = auction;
        this.outbidUser = outbidUser;
    }
}
