package vn.team9.auction_system.auction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.common.service.IAuctionService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;
    private final IAuctionService auctionService; // dùng service hiện tại

    // Chạy mỗi phút
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAuctions() {
        LocalDateTime now = LocalDateTime.now();

        List<Auction> auctions = auctionRepository.findAll();

        for (Auction auction : auctions) {
            try {
                // Auto-start PENDING auction
                if ("PENDING".equals(auction.getStatus()) && auction.getStartTime().isBefore(now)) {
                    auctionService.startAuction(auction.getAuctionId());
                    System.out.println("Started auction: " + auction.getAuctionId());
                }

                // Auto-close OPEN auction
                if ("OPEN".equals(auction.getStatus()) && auction.getEndTime().isBefore(now)) {
                    auctionService.closeAuction(auction.getAuctionId());
                    System.out.println("Closed auction: " + auction.getAuctionId());
                }
            } catch (Exception e) {
                System.err.println("Error processing auction " + auction.getAuctionId() + ": " + e.getMessage());
            }
        }
    }
}
