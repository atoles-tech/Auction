package atl.web.auction.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import atl.web.auction.dto.auction.BidRequest;
import atl.web.auction.model.AutoBid;
import atl.web.auction.model.Bid;
import atl.web.auction.model.Lot;
import atl.web.auction.model.util.LotStatus;
import atl.web.auction.services.util.Observer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class LotChecker implements Observer {

    private final LotService lotService;
    private final TaskScheduler taskScheduler;

    private List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();
    private CopyOnWriteArrayList<Lot> startedLots = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Lot> createdLots = new CopyOnWriteArrayList<>();
    private final Set<Long> lotsInProgress = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        lotService.subscribe(this);

        List<Lot> allLots = lotService.findAll();
        List<Lot> lotsToUpdate = new ArrayList<>();

        for (Lot lot : allLots) {

            LotStatus oldStatus = lot.getStatus();
            LocalDateTime now = LocalDateTime.now();

            if (lot.getEndIn().isBefore(now)) {
                lot.setStatus(LotStatus.FINISHED);
            } else if (lot.getStartIn().isBefore(now)) {
                lot.setStatus(LotStatus.STARTED);
            }

            if (oldStatus != lot.getStatus()) {
                lotsToUpdate.add(lot);
            }

            if (lot.getStatus() != LotStatus.FINISHED) {
                if (lot.getStatus() == LotStatus.STARTED) {
                    startedLots.add(lot);
                    scheduledTasks.add(scheduleLotEnd(lot.getId(), lot.getEndIn()));
                } else {
                    createdLots.add(lot);
                    scheduledTasks.add(scheduleLotStart(lot.getId(), lot.getStartIn()));
                }
            }
        }

        if (!lotsToUpdate.isEmpty()) {
            lotService.saveAll(lotsToUpdate);
        }
    }

    public void checkLot(Lot l) {
        while (true) {
            Lot lot = lotService.findEntityLot(l.getId());

            if (lot.getAutoBids() == null || lot.getAutoBids().isEmpty()) {
                return;
            }

            Bid currentMaxBidEntity = lot.getBids() == null || lot.getBids().isEmpty()
                    ? null
                    : lot.getBids().stream()
                            .max(Comparator.comparingDouble(Bid::getPrice))
                            .orElse(null);
            Double currentMaxBid = currentMaxBidEntity == null ? lot.getStartPrice() : currentMaxBidEntity.getPrice();
            Long currentLeaderUserId = currentMaxBidEntity == null ? null : currentMaxBidEntity.getUser().getId();

            removeReachedAutoBids(lot, currentMaxBid, currentLeaderUserId);
            lot = lotService.findEntityLot(l.getId());

            if (lot.getAutoBids() == null || lot.getAutoBids().isEmpty()) {
                return;
            }

            currentMaxBidEntity = lot.getBids() == null || lot.getBids().isEmpty()
                    ? null
                    : lot.getBids().stream()
                            .max(Comparator.comparingDouble(Bid::getPrice))
                            .orElse(null);
            currentMaxBid = currentMaxBidEntity == null ? lot.getStartPrice() : currentMaxBidEntity.getPrice();
            currentLeaderUserId = currentMaxBidEntity == null ? null : currentMaxBidEntity.getUser().getId();
            final Long leaderUserId = currentLeaderUserId;
            final double maxBid = currentMaxBid;
            final double minStep = lot.getMinStep();

            AutoBid challengerAutoBid = lot.getAutoBids().stream()
                    .filter(ab -> leaderUserId == null || !Objects.equals(ab.getUser().getId(), leaderUserId))
                    .filter(ab -> ab.getPriceWillBe() >= maxBid + minStep)
                    .max(Comparator
                            .comparingDouble(AutoBid::getPriceWillBe)
                            .thenComparing(AutoBid::getId, Comparator.reverseOrder()))
                    .orElse(null);

            if (challengerAutoBid == null) {
                return;
            }

            AutoBid currentLeaderAutoBid = leaderUserId == null
                    ? null
                    : lot.getAutoBids().stream()
                            .filter(ab -> Objects.equals(ab.getUser().getId(), leaderUserId))
                            .findFirst()
                            .orElse(null);

            double nextBid = currentMaxBid + minStep;

            if (currentLeaderAutoBid != null) {
                nextBid = Math.min(nextBid, challengerAutoBid.getPriceWillBe());
            }

            if (nextBid <= currentMaxBid) {
                return;
            }

            lotService.createBid(
                    new BidRequest(lot.getId(), nextBid),
                    challengerAutoBid.getUser().getId());
        }
    }

    private void removeReachedAutoBids(Lot lot, Double currentMaxBid, Long currentLeaderUserId) {
        if (lot.getAutoBids() == null || lot.getAutoBids().isEmpty()) {
            return;
        }

        List<Long> reachedAutoBidIds = lot.getAutoBids().stream()
                .filter(autoBid -> currentLeaderUserId == null || !Objects.equals(autoBid.getUser().getId(), currentLeaderUserId))
                .filter(autoBid -> autoBid.getPriceWillBe() <= currentMaxBid)
                .map(AutoBid::getId)
                .toList();

        for (Long autoBidId : reachedAutoBidIds) {
            lotService.deleteAutoBid(autoBidId);
        }
    }

    @PreDestroy
    public void cleanup() {
        for (ScheduledFuture<?> task : scheduledTasks) {
            task.cancel(true);
        }
        startedLots.clear();
        createdLots.clear();
    }

    @Override
    public void notify(Lot lot) {
        createdLots.add(lot);
        log.info("Lot with id:{} was added to LotCheker", lot.getId());
        ScheduledFuture<?> s = scheduleLotStart(lot.getId(), lot.getStartIn());
        scheduledTasks.add(s);
    }

    @Override
    public void notifyAddAutoBid(AutoBid autoBid, Long lotId) {
        for (Lot lot : startedLots) {
            if (lot.getId().equals(lotId)) {
                lot.addAutoBid(autoBid);
                triggerCheckLot(lot);
                return;
            }
        }

        for (Lot lot : createdLots) {
            if (lot.getId().equals(lotId)) {
                lot.addAutoBid(autoBid);
                triggerCheckLot(lot);
                return;
            }
        }
    }

    @Override
    public void notifyAddBid(Bid bid, Long lotId) {
        for (Lot lot : startedLots) {
            if (lot.getId().equals(lotId)) {
                lot.addBid(bid);
                triggerCheckLot(lot);
                return;
            }
        }

        for (Lot lot : createdLots) {
            if (lot.getId().equals(lotId)) {
                lot.addBid(bid);
                triggerCheckLot(lot);
                return;
            }
        }
    }

    @Override
    public void notifyRemoveAutoBid(Long autoBidId) {
        for (Lot lot : startedLots) {
            lot.removeAutoBid(autoBidId);
        }

        for (Lot lot : createdLots) {
            lot.removeAutoBid(autoBidId);
        }
    }

    private ScheduledFuture<?> scheduleLotStart(Long lotId, LocalDateTime startTime) {
        log.info("Created schedule for lot with id: {}, to: {}", lotId,
                startTime.atZone(ZoneId.of("Africa/Addis_Ababa")).toInstant());
        return taskScheduler.schedule(() -> startLot(lotId),
                startTime.atZone(ZoneId.of("Africa/Addis_Ababa")).toInstant());
    }

    private ScheduledFuture<?> scheduleLotEnd(Long lotId, LocalDateTime endTime) {
        return taskScheduler.schedule(() -> endLot(lotId),
                endTime.toInstant(ZoneOffset.UTC));
    }

    private void startLot(Long lotId) {
        try {
            log.info("Start {} executed at {}", lotId, LocalDateTime.now());

            lotService.changeLotStatus(lotId, LotStatus.STARTED);

            createdLots.removeIf(l -> l.getId().equals(lotId));
            Lot fresh = lotService.findEntityLot(lotId);
            startedLots.add(fresh);
            triggerCheckLot(fresh);

            log.info("Lot {} successfully started, new status: {}", lotId, fresh.getStatus());
        } catch (Exception e) {
            log.error("Failed to start lot {}", lotId, e);
        }
    }

    private void endLot(Long lotId) {
        try {
            log.info("Finish {} executed at {}", lotId, LocalDateTime.now());

            lotService.changeLotStatus(lotId, LotStatus.FINISHED);

            startedLots.removeIf(l -> l.getId().equals(lotId));

        } catch (Exception e) {
            log.error("Failed to finish lot {}", lotId, e);
        }

    }

    private void triggerCheckLot(Lot lot) {
        if (!lotsInProgress.add(lot.getId())) {
            return;
        }
        try {
            checkLot(lot);
        } finally {
            lotsInProgress.remove(lot.getId());
        }
    }

}
