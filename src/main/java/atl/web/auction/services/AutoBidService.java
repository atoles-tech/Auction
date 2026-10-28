package atl.web.auction.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.model.AutoBid;

public interface AutoBidService {
    AutoBidResponse findById(Long bidId);
    List<AutoBidResponse> findByUserId(Long userId);
    List<AutoBidResponse> findByLotId(Long lotId);
    Optional<AutoBidResponse> findByLotIdAndUserId(Long lotId, Long userId);
    Page<AutoBidResponse> findByUserId(Long userId, Pageable pageable);
    Page<AutoBidResponse> findByLotId(Long lotId, Pageable pageable);

    AutoBidResponse createAutoBid(AutoBid autoBid);
    void deleteAutoBid(Long autoBidId);

    //ENTITY
    AutoBid saveAutoBid(AutoBid autoBid);
}
