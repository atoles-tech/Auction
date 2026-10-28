package atl.web.auction.services.Impl.lot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.dto.auction.BidRequest;
import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.auction.lots.LotRequest;
import atl.web.auction.dto.auction.lots.LotResponse;
import atl.web.auction.exceptions.bid.AutoBidConflictException;
import atl.web.auction.exceptions.bid.BidConflictException;
import atl.web.auction.exceptions.lot.LotNotFoundException;
import atl.web.auction.exceptions.lot.LotTimeWrongException;
import atl.web.auction.exceptions.lot.MediaTypesWrongException;
import atl.web.auction.mappers.AutoBidMapper;
import atl.web.auction.mappers.BidMapper;
import atl.web.auction.mappers.LotMapper;
import atl.web.auction.model.AutoBid;
import atl.web.auction.model.Bid;
import atl.web.auction.model.Category;
import atl.web.auction.model.Lot;
import atl.web.auction.model.MediaLot;
import atl.web.auction.model.User;
import atl.web.auction.model.filters.LotFilter;
import atl.web.auction.model.spec.LotSpecification;
import atl.web.auction.model.util.LotStatus;
import atl.web.auction.model.util.Role;
import atl.web.auction.repositories.LotRepository;
import atl.web.auction.services.AutoBidService;
import atl.web.auction.services.BidService;
import atl.web.auction.services.CategoryService;
import atl.web.auction.services.LotService;
import atl.web.auction.services.UserService;
import atl.web.auction.services.util.Observer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class DefaultLotService implements LotService {

    private final UserService userService;
    private final AutoBidService autoBidService;
    private final BidService bidService;
    private final CategoryService categoryService;

    private final LotRepository lotRepository;

    private final LotMapper lotMapper;
    private final BidMapper bidMapper;
    private final AutoBidMapper autoBidMapper;

    @Value("${auction.min-time:7200}")
    private Integer minSeconds;

    @Value("${auction.max-time:604800}")
    private Integer maxSeconds;

    private List<Observer> observers = new ArrayList<>();

    @Override
    @Transactional
    public AutoBidResponse createAutoBid(BidRequest autoBidRequest, Long userId) {
        Lot lot = lotRepository.findById(autoBidRequest.getLotId())
                .orElseThrow(() -> {
                    log.warn("Create auto bid failed: not found lot with id {}", autoBidRequest.getLotId());
                    return new LotNotFoundException(autoBidRequest.getLotId());
                });

        if (lot.getStatus() != LotStatus.STARTED) {
            log.warn("Create auto bid failed: status is not 'started' in lot with id: {}", autoBidRequest.getLotId());
            throw new LotNotFoundException(autoBidRequest.getLotId());
        }

        if (autoBidRequest.getPrice() < getMaxBid(lot) + lot.getMinStep()) {
            log.warn("Create auto bid failed: lot current '{} + {} >= {}'", getMaxBid(lot), lot.getMinStep(),
                    autoBidRequest.getPrice());
            throw new AutoBidConflictException(autoBidRequest.getPrice());
        }

        if (lot.getAutoBids().stream()
                .filter(ab -> Objects.equals(ab.getUser().getId(), userId))
                .findFirst()
                .isPresent()) {
            log.info("Create auto bid failed: this combination (lot and user) there is");
            throw new AutoBidConflictException();
        }

        AutoBid autoBid = new AutoBid(null, lot, userService.findUserEntityById(userId), autoBidRequest.getPrice());
        AutoBid saveAutoBid = autoBidService.saveAutoBid(autoBid);

        lot.addAutoBid(saveAutoBid);
        lotRepository.save(lot);

        notifyAllObserversCreateAutoBid(autoBid, lot.getId());
        log.info("Created autobid with id: {}", saveAutoBid.getId());

        return autoBidMapper.toAutoBidResponse(saveAutoBid);
    }

    @Override
    @Transactional
    public BidResponse createBid(BidRequest bidRequest, Long userId) {
        Lot lot = lotRepository.findById(bidRequest.getLotId())
                .orElseThrow(() -> {
                    log.warn("Create bid failed: not found lot with id {}", bidRequest.getLotId());
                    return new LotNotFoundException(bidRequest.getLotId());
                });

        if (lot.getStatus() != LotStatus.STARTED) {
            log.warn("Create bid failed: not found lot with id {}", bidRequest.getLotId());
            throw new LotNotFoundException(bidRequest.getLotId());
        }

        if (bidRequest.getPrice() < getMaxBid(lot) + lot.getMinStep()) {
            log.warn("Create bid failed: lot current '{} + {} >= {}'", getMaxBid(lot), lot.getMinStep(),
                    bidRequest.getPrice());
            throw new BidConflictException(bidRequest.getPrice());
        }

        Bid bid = new Bid(null, lot, userService.findUserEntityById(userId), bidRequest.getPrice(),
                LocalDateTime.now());
        Bid savedBid = bidService.saveBid(bid);

        lot.addBid(savedBid);
        lotRepository.save(lot);

        notifyAllObserversCreateBid(savedBid, lot.getId());
        log.info("Created bid with id: {}", savedBid.getId());

        return bidMapper.toBidResponse(savedBid);
    }

    @Override
    @Transactional
    public LotResponse createLot(LotRequest request, Long userId) {
        if (request.getStartIn().isAfter(request.getEndIn())) {
            log.warn("Created lot failed: start time must be before end time");
            throw new LotTimeWrongException();
        }

        if (request.getStartIn().isBefore(LocalDateTime.now())) {
            log.warn("Created lot failed: start time must be after now");
            throw new LotTimeWrongException();
        }

        long seconds = ChronoUnit.SECONDS.between(request.getStartIn(), request.getEndIn());

        if (seconds < minSeconds || seconds > maxSeconds) {
            log.warn("Created lot failed: time ");
            throw new LotTimeWrongException(seconds);
        }

        Lot lot = new Lot(
                null,
                request.getTitle(),
                categoryService.findEntityByTitle(request.getCategory()),
                userService.findUserEntityById(userId),
                LocalDateTime.now(),
                request.getStartIn(),
                request.getEndIn(),
                request.getStartPrice(),
                request.getMinStep(),
                LotStatus.CREATED,
                null,
                null,
                null);

        List<MediaLot> medias = request.getMediaUrls().stream()
                .map(media -> new MediaLot(null, lot, media.getUrl(), media.getIsMain()))
                .toList();

        if (medias.stream().filter(x -> x.getIsMain()).count() > 1) {
            throw new MediaTypesWrongException();
        }

        lot.setMedia(medias);

        Lot savedLot = lotRepository.save(lot);
        notifyAllObserversCreateLot(savedLot);

        log.info("Lot created with id: {}", savedLot.getId());

        return lotMapper.toLotFullResponse(savedLot);
    }

    @Override
    @Transactional
    public void deleteAutoBid(Long autoBidId) {
        log.info("Delete auto bid with id: {}", autoBidId);
        notifyAllObserversRemoveAutoBid(autoBidId);
        autoBidService.deleteAutoBid(autoBidId);
    }

    @Override
    public List<AutoBidResponse> findAutoBidByLot(Long lotId) {
        log.info("Find auto bids for lot with id: {}", lotId);
        return autoBidService.findByLotId(lotId);
    }

    @Override
    public AutoBidResponse findMyAutoBidByLot(Long lotId, Long userId) {
        log.info("Find auto bid for lot with id: {} and user with id: {}", lotId, userId);
        return autoBidService.findByLotIdAndUserId(lotId, userId).orElse(null);
    }

    @Override
    public Page<AutoBidResponse> findAutoBidByLot(Long lotId, Pageable pageable) {
        log.info("Find auto bids for lot with id: {}", lotId);
        return autoBidService.findByLotId(lotId, pageable);
    }

    @Override
    public List<BidResponse> findBidByLot(Long lotId) {
        log.info("Find bids for lot with id: {}", lotId);
        return bidService.findByLotId(lotId);
    }

    @Override
    public Page<BidResponse> findBidByLot(Long lotId, Pageable pageable) {
        log.info("Find bids for lot with id: {}", lotId);
        return bidService.findByLotId(lotId, pageable);
    }

    @Override
    public List<? extends LotResponse> findByCategory(Category category, String userEmail) {
        User user = userService.findUserEntityByEmail(userEmail);

        List<Lot> lots = lotRepository.findByCategory(category);

        log.info("Find lots by category: {}", category.getTitle());

        if (user.getCredential().getRole() == Role.ROLE_ADMIN) {
            return lotMapper.toLotFullResponseList(lots);
        }

        return lotMapper.toLotPublicResponseList(lots);
    }

    @Override
    public Page<? extends LotResponse> findByCategory(Category category, Pageable pageable, String userEmail) {
        User user = userService.findUserEntityByEmail(userEmail);

        Page<Lot> lots = lotRepository.findByCategory(category, pageable);

        log.info("Find lots by category: {}", category.getTitle());

        if (user.getCredential().getRole() == Role.ROLE_ADMIN) {
            return lotMapper.toLotFullResponsePage(lots, pageable);
        }

        return lotMapper.toLotPublicResponsePage(lots, pageable);
    }

    @Override
    public LotResponse findById(Long lotId, String userEmail) {
        log.info("Find lot by id: {}", lotId);

        User user = userService.findUserEntityByEmail(userEmail);

        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new LotNotFoundException(lotId));

        if (user.getCredential().getRole() == Role.ROLE_ADMIN ||
                lot.getOwner().getId().equals(user.getId())) {
            return lotMapper.toLotFullResponse(lot);
        }

        return lotMapper.toLotPublicResponse(lot);
    }

    @Override
    public List<? extends LotResponse> findByStatus(LotStatus status, String userEmail) {
        log.info("Find lots by status: {}", status);

        User user = userService.findUserEntityByEmail(userEmail);

        List<Lot> lots = lotRepository.findByStatus(status);

        if (user.getCredential().getRole() == Role.ROLE_ADMIN) {
            return lotMapper.toLotFullResponseList(lots);
        }

        return lotMapper.toLotPublicResponseList(lots);
    }

    @Override
    public Page<? extends LotResponse> findByStatus(LotStatus status, Pageable pageable, String userEmail) {
        log.info("Find lots by status: {}", status);

        User user = userService.findUserEntityByEmail(userEmail);

        Page<Lot> lots = lotRepository.findByStatus(status, pageable);

        if (user.getCredential().getRole() == Role.ROLE_ADMIN) {
            return lotMapper.toLotFullResponsePage(lots, pageable);
        }

        return lotMapper.toLotPublicResponsePage(lots, pageable);
    }

    @Override
    public List<? extends LotResponse> findByTitle(String title, String userEmail) {
        log.info("Find lots by title: {}", title);

        User user = userService.findUserEntityByEmail(userEmail);

        List<Lot> lots = lotRepository.findByTitle(title);

        if (user.getCredential().getRole() == Role.ROLE_ADMIN) {
            return lotMapper.toLotFullResponseList(lots);
        }

        return lotMapper.toLotPublicResponseList(lots);
    }

    @Override
    public Page<? extends LotResponse> findByTitle(String title, Pageable pageable, String userEmail) {
        log.info("Find lots by title: {}", title);

        User user = userService.findUserEntityByEmail(userEmail);

        Page<Lot> lots = lotRepository.findByTitle(title.toLowerCase(), pageable);

        if (user.getCredential().getRole() == Role.ROLE_ADMIN) {
            return lotMapper.toLotFullResponsePage(lots, pageable);
        }

        return lotMapper.toLotPublicResponsePage(lots, pageable);
    }

    @Override
    public List<? extends LotResponse> findByUserId(Long userId, String userEmail) {
        log.info("Find lots by user with id: {}", userId);

        User user = userService.findUserEntityByEmail(userEmail);

        List<Lot> lots = lotRepository.findByUserId(userId);

        if (user.getCredential().getRole() == Role.ROLE_ADMIN ||
                user.getId().equals(userId)) {
            return lotMapper.toLotFullResponseList(lots);
        }

        return lotMapper.toLotPublicResponseList(lots);
    }

    @Override
    public Page<? extends LotResponse> findByUserId(Long userId, Pageable pageable, String userEmail) {
        log.info("Find lots by user with id: {}", userId);

        User user = userService.findUserEntityByEmail(userEmail);

        Page<Lot> lots = lotRepository.findByUserId(userId, pageable);

        if (user.getCredential().getRole() == Role.ROLE_ADMIN ||
                user.getId().equals(userId)) {
            return lotMapper.toLotFullResponsePage(lots, pageable);
        }

        return lotMapper.toLotPublicResponsePage(lots, pageable);
    }

    private Double getMaxBid(Lot lot) {
        Bid bid = lot.getBids().stream()
                .max((a, b) -> Double.compare(a.getPrice(), b.getPrice()))
                .orElse(null);

        if (bid == null) {
            return lot.getStartPrice();
        }

        return bid.getPrice();
    }

    @Override
    public List<? extends LotResponse> findAllLots() {
        log.info("Find lots all lots");
        return lotMapper.toLotPublicResponseList(lotRepository.findAll());
    }

    @Override
    public Page<? extends LotResponse> findAllLots(LotFilter filter, Pageable pageable) {
        log.info("Find lots lots with filter: {}", filter);
        return lotMapper.toLotPublicResponsePage(lotRepository.findAll(LotSpecification.filterBy(filter), pageable),
                pageable);
    }

    @Override
    public List<Lot> findAll() {
        log.info("Find all entity lots");
        return lotRepository.findAll();
    }

    @Override
    public List<Lot> findEntityLotsByStatus(LotStatus status) {
        log.info("Find all entity lots by status: {}", status);
        return lotRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public List<Lot> saveAll(List<Lot> lots) {
        log.info("Save all lots");
        return lotRepository.saveAll(lots);
    }

    @Override
    @Transactional
    public void saveLot(Lot lot) {
        Lot savedLot = lotRepository.save(lot);
        log.info("Saved lot: {}", savedLot.getId());
    }

    @Override
    @Transactional
    public void changeLotStatus(Long lotId, LotStatus status) {
        Lot lot = findEntityLot(lotId);
        lot.setStatus(status);
        lotRepository.save(lot);
        log.info("Changed lot with id: {} status from {} to {}", lotId, lot.getStatus(), status);
    }

    @Override
    public void subscribe(Observer observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(observer);
    }

    private void notifyAllObserversCreateLot(Lot lot) {
        observers.forEach(observer -> {
            observer.notify(lot);
        });
    }

    private void notifyAllObserversCreateBid(Bid bid, Long lotId) {
        observers.forEach(observer -> {
            observer.notifyAddBid(bid, lotId);
        });
    }

    private void notifyAllObserversCreateAutoBid(AutoBid bid, Long lotId) {
        observers.forEach(observer -> {
            observer.notifyAddAutoBid(bid, lotId);
        });
    }

    private void notifyAllObserversRemoveAutoBid(Long autoBidId) {
        observers.forEach(observer -> {
            observer.notifyRemoveAutoBid(autoBidId);
        });
    }

    @Override
    public Lot findEntityLot(Long id) {
        return lotRepository.findById(id).orElseThrow(() -> {
            log.warn("Find lot failed: lot with id: {} not found", id);
            return new LotNotFoundException(id);
        });
    }

    public Lot findWithBidsAndAutoBids(Long id) {
        return lotRepository.findWithBidsAndAutoBids(id).orElseThrow(() -> new LotNotFoundException(id));
    }

}
