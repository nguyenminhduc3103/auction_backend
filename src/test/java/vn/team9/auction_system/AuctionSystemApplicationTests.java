package vn.team9.auction_system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.repository.AuctionRepository;

@SpringBootTest
class AuctionSystemApplicationTests {

    @Autowired
    private AuctionRepository auctionRepository;

    @Test
    void testCheckEndTime() {
        Long id = 62L; // đổi sang auction_id bạn đang test

        String endFromDb = auctionRepository.checkEnd(id);
        System.out.println(">>> DB_END_TIME = " + endFromDb);

        Auction a = auctionRepository.findById(id).orElseThrow();
        System.out.println(">>> ENTITY_END_TIME = " + a.getEndTime());
    }

}
