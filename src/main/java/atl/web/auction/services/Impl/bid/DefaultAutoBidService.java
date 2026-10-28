package atl.web.auction.services.Impl.bid;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.exceptions.bid.AutoBidNotFoundException;
import atl.web.auction.mappers.AutoBidMapper;
import atl.web.auction.model.AutoBid;
import atl.web.auction.repositories.AutoBidRepository;
import atl.web.auction.services.AutoBidService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DefaultAutoBidService implements AutoBidService {

    private final AutoBidRepository autoBidRepository;

    private final AutoBidMapper autoBidMapper;

    @Override
    public AutoBidResponse findById(Long bidId) {
        return autoBidMapper.toAutoBidResponse(autoBidRepository
                .findById(bidId)
                .orElseThrow(() -> new AutoBidNotFoundException(bidId)));
    }

    @Override
    public List<AutoBidResponse> findByLotId(Long lotId) {
        return autoBidMapper
                .toAutoBidResponseList(autoBidRepository.findByLotId(lotId));
    }

    @Override
    public Optional<AutoBidResponse> findByLotIdAndUserId(Long lotId, Long userId) {
        return autoBidRepository.findByLotIdAndUserId(lotId, userId)
                .map(autoBidMapper::toAutoBidResponse);
    }

    @Override
    public Page<AutoBidResponse> findByLotId(Long lotId, Pageable pageable) {
        return autoBidMapper
                .toAutoBidResponsePage(autoBidRepository.findByLotId(lotId, pageable));
    }

    @Override
    public List<AutoBidResponse> findByUserId(Long userId) {
        return autoBidMapper
                .toAutoBidResponseList(autoBidRepository.findByUserId(userId));
    }

    @Override
    public Page<AutoBidResponse> findByUserId(Long userId, Pageable pageable) {
        return autoBidMapper
                .toAutoBidResponsePage(autoBidRepository.findByUserId(userId, pageable));
    }

    @Override
    @Transactional
    public AutoBidResponse createAutoBid(AutoBid autoBid) {
        return autoBidMapper.toAutoBidResponse(autoBidRepository.save(autoBid));
    }

    @Override
    @Transactional
    public void deleteAutoBid(Long autoBidId) {
        autoBidRepository.deleteById(autoBidId);        
    }

    @Override
    public AutoBid saveAutoBid(AutoBid autoBid) {
        return autoBidRepository.save(autoBid);
    }

}
