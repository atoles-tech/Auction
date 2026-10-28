package atl.web.auction.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.auction.lots.LotPublicResponse;
import atl.web.auction.dto.user.UserCreateRequest;
import atl.web.auction.dto.user.UserUpdateRequest;
import atl.web.auction.dto.user.info.UserFullInfoResponse;
import atl.web.auction.dto.user.info.UserResponse;
import atl.web.auction.model.Lot;
import atl.web.auction.model.User;

public interface UserService {
    UserResponse findByEmail(String email, String currentUserEmail);
    UserResponse findById(Long userId, String currentUserEmail);
    List<? extends UserResponse> findAllUsers(String currentUserEmail);
    Page<? extends UserResponse> findAllUsers(String currentUserEmail, Pageable pageable);

    UserFullInfoResponse createUser(UserCreateRequest request);
    UserFullInfoResponse updateUser(Long userId, UserUpdateRequest request);
    UserFullInfoResponse becomeSeller(Long userId);

    List<LotPublicResponse> findFavoriteLotsForUser(Long userId);
    Page<LotPublicResponse> findFavoriteLotsForUser(Long userId, Pageable pageable);
    LotPublicResponse addLotToFavorite(Lot lot, Long userId);
    List<BidResponse> findBids(Long userId);
    Page<BidResponse> findBids(Long userId, Pageable pageable);
    List<AutoBidResponse> findAutoBids(Long userId);
    Page<AutoBidResponse> findAutoBids(Long userId, Pageable pageable);
    void deleteFavoriteLot(Long lotId, Long userId);

    User findUserEntityById(Long id);
    User findUserEntityByEmail(String email);
    
    Boolean isUserOwner(Long autoBidId, Long userId);
}
