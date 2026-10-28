package atl.web.auction.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import atl.web.auction.model.AutoBid;

@Repository
public interface AutoBidRepository extends JpaRepository<AutoBid, Long> {
    
    @Query("SELECT b FROM auto_bids b JOIN FETCH b.lot WHERE b.lot.id = :lotId")
    List<AutoBid> findByLotId(@Param("lotId") Long lotId);

    @Query("SELECT b FROM auto_bids b JOIN FETCH b.lot JOIN FETCH b.user WHERE b.lot.id = :lotId AND b.user.id = :userId")
    Optional<AutoBid> findByLotIdAndUserId(@Param("lotId") Long lotId, @Param("userId") Long userId);

    @Query(value = "SELECT b FROM auto_bids b JOIN FETCH b.lot WHERE b.lot.id = :lotId")
    Page<AutoBid> findByLotId(@Param("lotId") Long lotId, Pageable pageable);

    @Query("SELECT b FROM auto_bids b JOIN FETCH b.user WHERE b.user.id = :userId")
    List<AutoBid> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM auto_bids b JOIN FETCH b.user WHERE b.user.id = :userId")
    Page<AutoBid> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
