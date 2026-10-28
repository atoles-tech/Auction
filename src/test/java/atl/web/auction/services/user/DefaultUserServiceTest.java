package atl.web.auction.services.user;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.user.info.UserFullInfoResponse;
import atl.web.auction.dto.user.info.UserResponse;
import atl.web.auction.dto.user.info.UserShortInfoResponse;
import atl.web.auction.mappers.LotMapper;
import atl.web.auction.mappers.UserMapper;
import atl.web.auction.mappers.UserShortInfoMapper;
import atl.web.auction.model.AutoBid;
import atl.web.auction.model.Bid;
import atl.web.auction.model.Credential;
import atl.web.auction.model.Lot;
import atl.web.auction.model.User;
import atl.web.auction.model.util.Role;
import atl.web.auction.repositories.UserRepository;
import atl.web.auction.services.AuthService;
import atl.web.auction.services.AutoBidService;
import atl.web.auction.services.BidService;
import atl.web.auction.services.Impl.user.DefaultUserService;

@ExtendWith(MockitoExtension.class)
public class DefaultUserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;
    
    @Mock
    private BidService bidService;
    
    @Mock
    private AutoBidService autoBidService;

    @Mock
    private LotMapper lotMapper;
    
    @Mock
    private UserMapper userMapper;

    @Mock
    private UserShortInfoMapper userShortInfoMapper;

    @InjectMocks
    private DefaultUserService userService;

    private User user;
    private UserShortInfoResponse userShortInfoResponse;
    private UserFullInfoResponse userFullInfoResponse;
    
    private Credential credential;
    private Lot lot; // user IS owner
    private Lot anotherLot; // user IS NOT owner
    private Bid bid; // bid owner IS user
    private AutoBid autoBid;// autobid owner IS user



    @BeforeEach
    public void init(){
        credential = new Credential(1L, "email@gmail.com", null, Role.ROLE_ADMIN, null, null);
        user = new User(1L, credential, "name", "lastname", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        lot = new Lot(1L, "My lot", null, user, null, null, null, null, null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()); 
        anotherLot = new Lot(2L, "Another Lot", null, null, null, null, null, null, null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        bid = new Bid(1L, anotherLot, user, 200., null);
        autoBid = new AutoBid(1L, anotherLot, user, 300.);
        
        credential.setUser(user);

        user.setLots(List.of(lot));

        user.addBid(bid);
        user.addAutoBid(autoBid);
        anotherLot.addBid(bid);
        anotherLot.addAutoBid(autoBid);

        userShortInfoResponse = new UserShortInfoResponse(1L, "name", "lastname", "email@gmail.com", Role.ROLE_ADMIN);
        userFullInfoResponse = new UserFullInfoResponse(
            userShortInfoResponse, 
            List.of(), // don't check lotresponse
            null, 
            List.of(new BidResponse(1L, 1L, 1L, 200., null)), 
            List.of(new AutoBidResponse(1L, 1L, 1L, 300.)));
    }

    @Test
    public void givenUserEmail_whenUserIsAdmin_returnUser(){
        // Given
        String email = "email@gmail.com";

        // When
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponseList(any())).thenReturn(List.of(userFullInfoResponse));

        // Then
        List<? extends UserResponse> result = userService.findAllUsers(email);
        assertTrue(result.size() == result.stream().filter(x->x instanceof UserFullInfoResponse).count());
    }

    @Test
    public void givenUserEmail_whenUserIsNotAdmin_returnUser(){
        // Given
        String email = "email@gmail.com";
        credential.setRole(Role.ROLE_USER);

        // When
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userShortInfoMapper.toUserInfoResponseList(any())).thenReturn(List.of(userShortInfoResponse));

        // Then
        List<? extends UserResponse> result = userService.findAllUsers(email);
        assertTrue(result.size() == result.stream().filter(x->x instanceof UserShortInfoResponse).count());
    }

    @Test
    public void givenEmailAndUser_whenFindingUserExistsAndCurrentUserIsAdmin_thenReturnFullResponse(){
        // Given
        String email = "user@gmail.com";
        String currentEmail = "email@gmail.com";
        User fUser = new User(2L, null, "NAME", "LASTNAME", null, null, null, null);
        UserFullInfoResponse userFullResponse = new UserFullInfoResponse(new UserShortInfoResponse(2L,"name", "lastname", email, null), null, null, null, null);

        // When
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(fUser));
        when(userRepository.findByEmail(currentEmail)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(fUser)).thenReturn(userFullResponse);
        
        // Then
        UserResponse result = userService.findByEmail(email, currentEmail);
        assertTrue(result instanceof UserFullInfoResponse);
    }

    @Test
    public void givenEmailAndUser_whenFindingUserExistsAndCurrentUserIsNotAdmin_thenReturnFullResponse(){
        // Given
        String email = "user@gmail.com";
        String currentEmail = "email@gmail.com";
        User fUser = new User(2L, null, "NAME", "LASTNAME", null, null, null, null);
        UserShortInfoResponse response = new UserShortInfoResponse(2L,"name", "lastname", email, null);
        credential.setRole(Role.ROLE_USER);

        // When
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(fUser));
        when(userRepository.findByEmail(currentEmail)).thenReturn(Optional.of(user));
        when(userShortInfoMapper.toUserInfoResponse(fUser)).thenReturn(response);
        
        // Then
        UserResponse result = userService.findByEmail(email, currentEmail);
        assertTrue(result instanceof UserShortInfoResponse);
    }

    @Test
    public void givenUserRoleUser_whenBecomeSeller_thenRoleChangedToSeller() {
        // Given
        credential.setRole(Role.ROLE_USER);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userFullInfoResponse);

        // When
        userService.becomeSeller(user.getId());

        // Then
        assertEquals(Role.ROLE_SELLER, user.getCredential().getRole());
    }
}
