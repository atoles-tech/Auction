package atl.web.auction.services.bid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.exceptions.bid.BidNotFoundException;
import atl.web.auction.mappers.BidMapper;
import atl.web.auction.model.Bid;
import atl.web.auction.repositories.BidRepository;
import atl.web.auction.services.Impl.bid.DefaultBidService;

@ExtendWith(MockitoExtension.class)
public class DefaultBidServiceTest {
    
    @Mock
    private BidRepository bidRepository;

    @Mock
    private BidMapper bidMapper;

    @InjectMocks
    private DefaultBidService bidService;

    private static Bid bid;
    private static BidResponse bidResponse;


    @BeforeAll
    public static void init(){
        bid = new Bid(2L, null, null, 200.,LocalDateTime.of(2000, 1, 1, 1, 1));
        bidResponse = new BidResponse(2L, null, null, 200., LocalDateTime.of(2000, 1, 1, 1, 1));
    }

    @Test
    public void givenBidId_whenBidIsExists_thenReturnBid(){
        // Given
        Long bidId = 2L;

        // When
        when(bidRepository.findById(bidId)).thenReturn(Optional.of(bid));
        when(bidMapper.toBidResponse(bid)).thenReturn(bidResponse);

        // Then
        BidResponse result = bidService.findById(bidId);
        assertEquals(bidResponse, result);
    }

    @Test
    public void givenBidId_whenBidNotExists_thenThrowException(){
        // Given
        Long bidId = 2L;

        // When | Then
        when(bidRepository.findById(bidId)).thenReturn(Optional.empty());
        assertThrows(BidNotFoundException.class,() -> bidService.findById(bidId));
    }
}
