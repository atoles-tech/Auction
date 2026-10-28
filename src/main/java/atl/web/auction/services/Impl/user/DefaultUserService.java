package atl.web.auction.services.Impl.user;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.auction.lots.LotPublicResponse;
import atl.web.auction.dto.user.UserCreateRequest;
import atl.web.auction.dto.user.UserUpdateRequest;
import atl.web.auction.dto.user.info.UserFullInfoResponse;
import atl.web.auction.dto.user.info.UserResponse;
import atl.web.auction.exceptions.user.UserNotFoundException;
import atl.web.auction.mappers.LotMapper;
import atl.web.auction.mappers.UserMapper;
import atl.web.auction.mappers.UserShortInfoMapper;
import atl.web.auction.model.Lot;
import atl.web.auction.model.User;
import atl.web.auction.model.util.Role;
import atl.web.auction.repositories.UserRepository;
import atl.web.auction.services.AuthService;
import atl.web.auction.services.AutoBidService;
import atl.web.auction.services.BidService;
import atl.web.auction.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;

    private final AuthService authService;
    private final BidService bidService;
    private final AutoBidService autoBidService;

    private final LotMapper lotMapper;
    private final UserMapper userMapper;

    private final UserShortInfoMapper userShortInfoMapper;

    @Override
    @Transactional
    public LotPublicResponse addLotToFavorite(Lot lot, Long userId) {
        User user = findUserEntityById(userId);
        user.addFavoriteLot(lot);

        userRepository.save(user);

        log.info("Added lot with id: {} to favorite for user with id: ", lot.getId(), userId);

        return lotMapper.toLotPublicResponse(lot);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "users", allEntries = true)
    public UserFullInfoResponse createUser(UserCreateRequest request) {
        User user = authService.createUser(request);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void deleteFavoriteLot(Long lotId, Long userId) {
        User user = findUserEntityById(userId);
        user.deleteFavoriteLot(lotId);
        log.info("Deleted lot from favorites with id: {} for user with id: {}", lotId, userId);
    }

    @Override
    public List<? extends UserResponse> findAllUsers(String currentUserEmail) {
        List<User> users = userRepository.findAll();

        User currentUser = users.stream().filter((u) -> u.getCredential().getEmail().equals(currentUserEmail))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with email: {} not found", currentUserEmail);
                    return new UserNotFoundException(currentUserEmail);
                });

        if (currentUser.getCredential().getRole() == Role.ROLE_ADMIN) {
            return userMapper.toUserResponseList(users);
        }

        return userShortInfoMapper.toUserInfoResponseList(users);
    }

    @Override
    public Page<? extends UserResponse> findAllUsers(String currentUserEmail, Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        User currentUser = users.stream().filter((u) -> u.getCredential().getEmail().equals(currentUserEmail))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with email: {} not found", currentUserEmail);
                    return new UserNotFoundException(currentUserEmail);
                });

        if (currentUser.getCredential().getRole() == Role.ROLE_ADMIN) {
            return userMapper.toUserResponsePage(users);
        }

        return userShortInfoMapper.toUserInfoResponsePage(users);
    }

    @Override
    public List<AutoBidResponse> findAutoBids(Long userId) {
        return autoBidService.findByUserId(userId);
    }

    @Override
    public Page<AutoBidResponse> findAutoBids(Long userId, Pageable pageable) {
        return autoBidService.findByUserId(userId, pageable);
    }

    @Override
    public List<BidResponse> findBids(Long userId) {
        return bidService.findByUserId(userId);
    }

    @Override
    public Page<BidResponse> findBids(Long userId, Pageable pageable) {
        return bidService.findByUserId(userId, pageable);
    }

    @Override
    @Cacheable(cacheNames = "users", key = "#email")
    public UserResponse findByEmail(String email, String currentUserEmail) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with email: {} not found", email);
                    return new UserNotFoundException(email);
                });

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with email: {} not found", currentUserEmail);
                    return new UserNotFoundException(currentUserEmail);
                });

        if (currentUser.getCredential().getRole() == Role.ROLE_ADMIN || currentUser.equals(user)) {
            return userMapper.toUserResponse(user);
        }

        return userShortInfoMapper.toUserInfoResponse(user);
    }

    @Override
    @Cacheable(cacheNames = "users", key = "#userId")
    public UserResponse findById(Long userId, String currentUserEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with id: {} not found", userId);
                    return new UserNotFoundException(userId);
                });

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with email: {} not found", currentUserEmail);
                    return new UserNotFoundException(currentUserEmail);
                });

        if (currentUser.getCredential().getRole() == Role.ROLE_ADMIN || currentUser.equals(user)) {
            return userMapper.toUserResponse(user);
        }

        return userMapper.toUserResponse(user);
    }

    @Override
    public List<LotPublicResponse> findFavoriteLotsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with id: {} not found", userId);
                    return new UserNotFoundException(userId);
                });

        return lotMapper.toLotPublicResponseList(user.getFavoriteLots());
    }

    @Override
    public Page<LotPublicResponse> findFavoriteLotsForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with id: {} not found", userId);
                    return new UserNotFoundException(userId);
                });

        return lotMapper.toLotPublicResponsePage(user.getFavoriteLots(), pageable);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "users", allEntries = true)
    public UserFullInfoResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with id: {} not found", userId);
                    return new UserNotFoundException(userId);
                });

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());

        User savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "users", allEntries = true)
    public UserFullInfoResponse becomeSeller(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Become seller failed: user with id: {} not found", userId);
                    return new UserNotFoundException(userId);
                });

        if (user.getCredential().getRole() == Role.ROLE_USER) {
            user.getCredential().setRole(Role.ROLE_SELLER);
            userRepository.save(user);
            log.info("User with id: {} became seller", userId);
        } else {
            log.info("Become seller skipped for user with id: {} and role: {}", userId, user.getCredential().getRole());
        }

        return userMapper.toUserResponse(user);
    }

    @Override
    public User findUserEntityById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with id: {} not found", id);
                    return new UserNotFoundException(id);
                });
        return user;
    }

    @Override
    public User findUserEntityByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Find user failed: user with email: {} not found", email);
                    return new UserNotFoundException(email);
                });

        return user;
    }

    @Override
    public Boolean isUserOwner(Long autoBidId, Long userId) {
        return autoBidService.findById(autoBidId).getOwnerId().equals(userId);
    }

}
