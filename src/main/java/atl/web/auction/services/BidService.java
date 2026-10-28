package atl.web.auction.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.model.Bid;

public interface BidService {
    BidResponse findById(Long bidId);
    List<BidResponse> findByUserId(Long userId);
    List<BidResponse> findByLotId(Long lotId);
    Page<BidResponse> findByUserId(Long userId, Pageable pageable);
    Page<BidResponse> findByLotId(Long lotId, Pageable pageable);

    BidResponse createBid(Bid bid);

    //ENTITY
    Bid saveBid(Bid bid);
}
