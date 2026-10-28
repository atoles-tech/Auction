package atl.web.auction.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.dto.auction.BidRequest;
import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.auction.lots.LotRequest;
import atl.web.auction.dto.auction.lots.LotResponse;
import atl.web.auction.model.Category;
import atl.web.auction.model.Lot;
import atl.web.auction.model.filters.LotFilter;
import atl.web.auction.model.util.LotStatus;
import atl.web.auction.services.util.Observable;

public interface LotService extends Observable {
    LotResponse findById(Long lotId, String userEmail);

    List<? extends LotResponse> findByTitle(String title, String userEmail);

    Page<? extends LotResponse> findByTitle(String title, Pageable pageable, String userEmail);

    List<? extends LotResponse> findByUserId(Long userId, String userEmail);

    Page<? extends LotResponse> findByUserId(Long userId, Pageable pageable, String userEmail);

    List<? extends LotResponse> findByStatus(LotStatus status, String userEmail);

    Page<? extends LotResponse> findByStatus(LotStatus status, Pageable pageable, String userEmail);

    List<? extends LotResponse> findByCategory(Category category, String userEmail);

    Page<? extends LotResponse> findByCategory(Category category, Pageable pageable, String userEmail);

    List<? extends LotResponse> findAllLots();

    Page<? extends LotResponse> findAllLots(LotFilter filter, Pageable pageable);

    List<BidResponse> findBidByLot(Long lotId);

    Page<BidResponse> findBidByLot(Long lotId, Pageable pageable);

    List<AutoBidResponse> findAutoBidByLot(Long lotId);

    AutoBidResponse findMyAutoBidByLot(Long lotId, Long userId);

    Page<AutoBidResponse> findAutoBidByLot(Long lotId, Pageable pageable);

    BidResponse createBid(BidRequest bidRequest, Long userId);

    AutoBidResponse createAutoBid(BidRequest autoBidRequest, Long userId);

    void deleteAutoBid(Long autoBidId);

    LotResponse createLot(LotRequest request, Long userId);

    void changeLotStatus(Long lotId, LotStatus status);

    Lot findWithBidsAndAutoBids(Long id);

    // Entity methods(utils)
    List<Lot> findAll();

    List<Lot> findEntityLotsByStatus(LotStatus status);

    List<Lot> saveAll(List<Lot> lots);

    void saveLot(Lot lot);

    Lot findEntityLot(Long id);
}
