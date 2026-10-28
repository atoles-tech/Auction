package atl.web.auction.services.bid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.exceptions.bid.AutoBidNotFoundException;
import atl.web.auction.mappers.AutoBidMapper;
import atl.web.auction.model.AutoBid;
import atl.web.auction.repositories.AutoBidRepository;
import atl.web.auction.services.Impl.bid.DefaultAutoBidService;

@ExtendWith(MockitoExtension.class)
public class DefaultAutoBidServiceTest {

    @Mock
    private AutoBidRepository autoBidRepository;

    @Mock
    private AutoBidMapper autoBidMapper;

    @InjectMocks
    private DefaultAutoBidService autoBidService;

    private static AutoBid autoBid;
    private static AutoBidResponse autoBidResponse;

    @BeforeAll
    public static void init(){
        autoBid = new AutoBid(2L, null, null, 200.);
        autoBidResponse = new AutoBidResponse(2L, null, null, 200.);
    }

    @Test
    public void givenBidId_whenBidIsExists_thenReturnBid(){
        // Given
        Long bidId = 2L;

        // When
        when(autoBidRepository.findById(bidId)).thenReturn(Optional.of(autoBid));
        when(autoBidMapper.toAutoBidResponse(autoBid)).thenReturn(autoBidResponse);

        // Then
        AutoBidResponse result = autoBidService.findById(bidId);
        assertEquals(autoBidResponse, result);
    }

    @Test
    public void givenBidId_whenBidNotExists_thenThrowException(){
        // Given
        Long bidId = 2L;

        // When | Then
        when(autoBidRepository.findById(bidId)).thenReturn(Optional.empty());
        assertThrows(AutoBidNotFoundException.class,() -> autoBidService.findById(bidId));
    }
}
