package atl.web.auction.services.lot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.auction.BidRequest;
import atl.web.auction.exceptions.bid.AutoBidConflictException;
import atl.web.auction.dto.auction.lots.LotFullResponse;
import atl.web.auction.dto.auction.lots.LotPublicResponse;
import atl.web.auction.dto.auction.lots.LotRequest;
import atl.web.auction.dto.auction.lots.LotResponse;
import atl.web.auction.dto.user.info.UserShortInfoResponse;
import atl.web.auction.exceptions.lot.LotNotFoundException;
import atl.web.auction.exceptions.lot.LotTimeWrongException;
import atl.web.auction.mappers.AutoBidMapper;
import atl.web.auction.mappers.BidMapper;
import atl.web.auction.mappers.LotMapper;
import atl.web.auction.model.AutoBid;
import atl.web.auction.model.Bid;
import atl.web.auction.model.Credential;
import atl.web.auction.model.Lot;
import atl.web.auction.model.User;
import atl.web.auction.model.util.LotStatus;
import atl.web.auction.model.util.Role;
import atl.web.auction.repositories.LotRepository;
import atl.web.auction.services.AutoBidService;
import atl.web.auction.services.BidService;
import atl.web.auction.services.CategoryService;
import atl.web.auction.services.UserService;
import atl.web.auction.services.Impl.lot.DefaultLotService;

@ExtendWith(MockitoExtension.class)
public class DefaultLotServiceTest {
    
    @Mock
    private UserService userService;
    
    @Mock
    private AutoBidService autoBidService;
    
    @Mock
    private BidService bidService;
    
    @Mock
    private CategoryService categoryService;
    
    @Mock
    private LotRepository lotRepository;
    
    @Mock
    private LotMapper lotMapper;

    @Mock
    private AutoBidMapper autoBidMapper;

    @Mock
    private BidMapper bidMapper;

    @InjectMocks
    private DefaultLotService lotService;

    private Lot lot;
    private LotFullResponse lotFullResponse;
    private LotPublicResponse lotPublicResponse;

    private User owner;
    private Credential ownerCredential;
    
    private User user;
    private Credential userCredential;

    @BeforeEach
    public void init(){
        ReflectionTestUtils.setField(lotService, "minSeconds", 7200);
        ReflectionTestUtils.setField(lotService, "maxSeconds", 604800);

        ownerCredential = new Credential(
            1L, 
            "owner@gmail.com", 
            null, 
            Role.ROLE_USER, 
            null, 
            null);

        owner = new User(
            1L, 
            ownerCredential, 
            "firstname", 
            "lastname", 
            null, 
            null, 
            null, 
            null);

        ownerCredential.setUser(owner);

        userCredential = new Credential(
            2L, 
            "user@gmail.com", 
            null, 
            Role.ROLE_USER, 
            null, 
            null);

        user = new User(
            2L, 
            userCredential, 
            "user", 
            "user", 
            null, 
            null, 
            null, 
            null);

        userCredential.setUser(user);    

        lot = new Lot(
            1L, 
            "My lot", 
            null, 
            owner, 
            LocalDateTime.now(), 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(2), 
            100., 
            1., 
            LotStatus.CREATED, 
            new ArrayList<>(), 
            new ArrayList<>(), 
            new ArrayList<>()); 

        lotPublicResponse = new LotPublicResponse(
            1L, 
            "My lot", 
            null, 
            new UserShortInfoResponse(
                1L, 
                "firstname", 
                "lastname", 
                "owner@gmail.com", 
                Role.ROLE_USER
            ), 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(2),  
            LotStatus.CREATED, 
            100., 
            1., 
            null, 
            null);

        lotFullResponse = new LotFullResponse(
            lotPublicResponse, 
            new ArrayList<>(), 
            new ArrayList<>(), 
            null);    
    }

    @Test
    public void givenBidRequest_whenLotExistsAndActive_thenReturnAutoBidResponse(){
        // Given
        lot.setStatus(LotStatus.STARTED);
        BidRequest autoBidRequest = new BidRequest(1L, 200.); 
        Long userId = 2L;
        AutoBidResponse exceptedResponse = new AutoBidResponse(1L, lot.getId(), owner.getId(), 200.);
        AutoBid autoBid = new AutoBid(1L, lot, owner, 200.);

        // When
        when(lotRepository.findById(autoBidRequest.getLotId())).thenReturn(Optional.of(lot));
        when(autoBidService.saveAutoBid(any())).thenReturn(autoBid);
        when(autoBidMapper.toAutoBidResponse(autoBid)).thenReturn(exceptedResponse);

        // Then
        AutoBidResponse response = lotService.createAutoBid(autoBidRequest, userId);
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void givenBidRequest_whenLotExistsAndNotActive_thenReturnAutoBidResponse(){
        // Given
        lot.setStatus(LotStatus.CREATED);
        BidRequest autoBidRequest = new BidRequest(1L, 200.); 
        Long userId = 2L;

        // When | Then
        when(lotRepository.findById(autoBidRequest.getLotId())).thenReturn(Optional.of(lot));
        assertThrows(LotNotFoundException.class, ()->lotService.createAutoBid(autoBidRequest, userId));
    }

    @Test
    public void givenBidRequest_whenLotExistsAndActiveButThereIs_thenReturnAutoBidResponse(){
        // Given
        lot.setStatus(LotStatus.STARTED);
        BidRequest autoBidRequest = new BidRequest(1L, 300.);
        Long userId = 200L;
        User existingUser = new User(200L, null, null, null, null, null, null, null);
        AutoBid existingAutoBid = new AutoBid(99L, lot, existingUser, 250.);
        lot.addAutoBid(existingAutoBid);

        // When
        when(lotRepository.findById(autoBidRequest.getLotId())).thenReturn(Optional.of(lot));

        // Then
        assertThrows(AutoBidConflictException.class, () -> lotService.createAutoBid(autoBidRequest, userId));
    }

    @Test
    public void givenBidRequest_whenLotExistsAndActive_thenReturnBidResponse() {
        // Given
        lot.setStatus(LotStatus.STARTED);
        BidRequest bidRequest = new BidRequest(1L, 200.);
        Long userId = 2L;
        Bid savedBid = new Bid(10L, lot, user, 200., LocalDateTime.now());
        BidResponse expectedResponse = BidResponse.builder()
            .id(10L)
            .lotId(lot.getId())
            .ownerId(user.getId())
            .price(200.)
            .createdAt(savedBid.getCreatedAt())
            .build();

        // When
        when(lotRepository.findById(bidRequest.getLotId())).thenReturn(Optional.of(lot));
        when(userService.findUserEntityById(userId)).thenReturn(user);
        when(bidService.saveBid(any())).thenReturn(savedBid);
        when(bidMapper.toBidResponse(savedBid)).thenReturn(expectedResponse);

        // Then
        BidResponse response = lotService.createBid(bidRequest, userId);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void givenLotRequest_whenTimeIsRight_thenReturnLot(){
        // Given
        LotRequest request = new LotRequest(
            "My lot", 
            null, 
            lot.getStartIn(), 
            lot.getEndIn(), 
            100., 
            1., 
            new ArrayList<>());
        Long userId = 1L;
        
        // When
        when(categoryService.findEntityByTitle(request.getCategory())).thenReturn(null);
        when(userService.findUserEntityById(userId)).thenReturn(owner);
        when(lotRepository.save(any())).thenReturn(lot);
        when(lotMapper.toLotFullResponse(lot)).thenReturn(lotFullResponse);

        // Then
        LotResponse response = lotService.createLot(request, userId);
        assertTrue(response instanceof LotFullResponse);
    }

    @Test
    public void givenLotRequest_whenTimeIsNotRight_thenReturnLot(){
        // Given
        LotRequest request = new LotRequest(
            "My lot", 
            null, 
            LocalDateTime.now().plusDays(1), 
            LocalDateTime.now().plusDays(0), 
            100., 
            1., 
            new ArrayList<>());
        Long userId = 1L;
        
        // When | Then
        assertThrows(LotTimeWrongException.class, ()->lotService.createLot(request, userId));
    }

}
