package atl.web.auction.services.Impl.bid;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.exceptions.bid.BidNotFoundException;
import atl.web.auction.mappers.BidMapper;
import atl.web.auction.model.Bid;
import atl.web.auction.repositories.BidRepository;
import atl.web.auction.services.BidService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DefaultBidService implements BidService {

    private final BidRepository bidRepository;

    private final BidMapper bidMapper;

    @Override
    public BidResponse findById(Long bidId) {
        return bidMapper.toBidResponse(bidRepository.findById(bidId)
                .orElseThrow(() -> new BidNotFoundException(bidId)));
    }

    @Override
    public List<BidResponse> findByLotId(Long lotId) {
        return bidMapper.toBidResponseList(bidRepository.findByLotId(lotId));
    }

    @Override
    public Page<BidResponse> findByLotId(Long lotId, Pageable pageable) {
        return bidMapper.toBidResponsePage(bidRepository.findByLotId(lotId, pageable));
    }

    @Override
    public List<BidResponse> findByUserId(Long userId) {
        return bidMapper.toBidResponseList(bidRepository.findByUserId(userId));
    }

    @Override
    public Page<BidResponse> findByUserId(Long userId, Pageable pageable) {
        return bidMapper.toBidResponsePage(bidRepository.findByUserId(userId, pageable));
    }

    @Override
    @Transactional
    public BidResponse createBid(Bid bid) {
        return bidMapper.toBidResponse(bidRepository.save(bid));
    }

    @Override
    @Transactional
    public Bid saveBid(Bid bid) {
        return bidRepository.save(bid);
    }

}
