package vn.team9.auction_system.auction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.team9.auction_system.auction.event.AuctionEventPublisher;
import vn.team9.auction_system.auction.model.Auction;
import vn.team9.auction_system.auction.model.Bid;
import vn.team9.auction_system.auction.repository.AuctionRepository;
import vn.team9.auction_system.auction.repository.BidRepository;
import vn.team9.auction_system.common.dto.auction.BidRequest;
import vn.team9.auction_system.common.dto.auction.BidResponse;
import vn.team9.auction_system.user.model.User;
import vn.team9.auction_system.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class AutoBidServiceImpl extends AbstractBidService implements IAutoBidService {

    private final AuctionEventPublisher eventPublisher;

    public AutoBidServiceImpl(AuctionRepository auctionRepository,
                              BidRepository bidRepository,
                              UserRepository userRepository,
                              AuctionEventPublisher eventPublisher) {
        super(auctionRepository, bidRepository, userRepository);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public BidResponse placeAutoBid(BidRequest request) {
        Auction auction = findAuction(request.getAuctionId());
        User bidder = findUser(request.getBidderId());

        if (!"OPEN".equalsIgnoreCase(auction.getStatus())) {
            throw new RuntimeException("Auction is not open for bidding");
        }

        BigDecimal startPrice = auction.getProduct().getStartPrice();
        BigDecimal currentHighest = auction.getHighestCurrentPrice() != null
                ? auction.getHighestCurrentPrice()
                : startPrice;

        // Kiểm tra hợp lệ
        if (request.getMaxAutoBidAmount().compareTo(currentHighest) <= 0) {
            throw new RuntimeException("Max auto bid must be greater than current highest bid");
        }

        // Tạo auto-bid ban đầu
        Bid firstBid = createAutoBid(auction, bidder, currentHighest.add(request.getStepAutoBidAmount()),
                request.getMaxAutoBidAmount(), request.getStepAutoBidAmount());

        // Cập nhật giá cao nhất
        auction.setHighestCurrentPrice(firstBid.getBidAmount());
        auctionRepository.save(auction);

        // Kiểm tra nếu có auto-bid khác => kích hoạt đấu lại
        triggerAutoBidCompetition(auction);

        return mapToResponse(firstBid);
    }

    @Override
    public void handleManualBid(Long auctionId) {
        Auction auction = findAuction(auctionId);
        triggerAutoBidCompetition(auction);
    }

    private void triggerAutoBidCompetition(Auction auction) {
        boolean hasNewBid = true;

        while (hasNewBid) {
            hasNewBid = false;

            // 1) Lấy bid cao nhất hiện tại
            Bid currentHighestBid = bidRepository
                    .findTopByAuction_AuctionIdOrderByBidAmountDesc(auction.getAuctionId())
                    .orElse(null);

            BigDecimal currentHighestAmount = currentHighestBid != null
                    ? currentHighestBid.getBidAmount()
                    : BigDecimal.ZERO;

            // 2) Lấy latest auto-bid record cho mỗi bidder
            List<Bid> allBids = bidRepository.findByAuction_AuctionId(auction.getAuctionId());

            Map<Long, Bid> latestAutoByBidder = allBids.stream()
                    .filter(Bid::getIsAuto)
                    .filter(bid -> bid.getMaxAutobidAmount() != null)
                    .collect(Collectors.toMap(b -> b.getBidder().getUserId(), Function.identity(), BinaryOperator.maxBy(Comparator.comparing(Bid::getCreatedAt))));

            if (latestAutoByBidder.isEmpty())  break;

            // 3) Sắp xếp theo maxAuto desc
            List<Bid> autoBidders = latestAutoByBidder.values().stream()
                    .sorted(Comparator.comparing(Bid::getMaxAutobidAmount).reversed())
                    .toList();

            // 4) TÌM CHALLENGER: người có max cao nhất KHÔNG PHẢI current highest bidder
            Optional<Bid> challengerOpt = autoBidders.stream()
                    .filter(challenger -> {
                        // Skip current highest bidder
                        if (currentHighestBid != null &&
                                challenger.getBidder().getUserId().equals(currentHighestBid.getBidder().getUserId())) {
                            return false;
                        }
                        // Chỉ lấy người có max > current highest
                        return challenger.getMaxAutobidAmount().compareTo(currentHighestAmount) > 0;
                    })
                    .findFirst();

            if (challengerOpt.isEmpty()) break;

            Bid challenger = challengerOpt.get();

            // 5) Tính proposed amount
            BigDecimal step = challenger.getStepAutoBidAmount() != null
                    ? challenger.getStepAutoBidAmount()
                    : new BigDecimal("1000");

            if (step.compareTo(BigDecimal.ZERO) <= 0) {
                step = new BigDecimal("1000");
            }

            BigDecimal proposed = currentHighestAmount.add(step);

            // Giới hạn bởi max của challenger
            if (proposed.compareTo(challenger.getMaxAutobidAmount()) > 0) {
                proposed = challenger.getMaxAutobidAmount();
            }

            // Kiểm tra proposed có thực sự > current highest không
            if (proposed.compareTo(currentHighestAmount) <= 0) break;

            // 6) Tạo counter bid
            if (currentHighestBid != null) {
                currentHighestBid.setIsHighest(false);
                bidRepository.save(currentHighestBid);
            }

            createAutoBid(
                    auction,
                    challenger.getBidder(),
                    proposed,
                    challenger.getMaxAutobidAmount(),
                    challenger.getStepAutoBidAmount()
            );

            // Cập nhật auction
            auction.setHighestCurrentPrice(proposed);
            auctionRepository.save(auction);

            // Publish event
            eventPublisher.publishAutoBidTriggeredEvent(auction);

            hasNewBid = true;
        }
    }

    private Bid createAutoBid(Auction auction, User bidder, BigDecimal bidAmount,
                              BigDecimal maxAmount, BigDecimal stepAmount) {
        // Reset highest flag của tất cả bids cũ
        resetHighestBidFlags(auction.getAuctionId());

        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setBidAmount(bidAmount);
        bid.setMaxAutobidAmount(maxAmount);
        bid.setStepAutoBidAmount(stepAmount);
        bid.setIsAuto(true);
        bid.setIsHighest(true);
        bid.setCreatedAt(LocalDateTime.now());
        bidRepository.save(bid);
        return bid;
    }
}