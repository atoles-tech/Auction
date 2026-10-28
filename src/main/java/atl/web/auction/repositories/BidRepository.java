package atl.web.auction.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import atl.web.auction.model.Bid;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("SELECT b FROM bids b JOIN FETCH b.lot WHERE b.lot.id = :lotId")
    List<Bid> findByLotId(@Param("lotId") Long lotId);

    @Query(value = "SELECT b FROM bids b JOIN FETCH b.lot WHERE b.lot.id = :lotId")
    Page<Bid> findByLotId(@Param("lotId") Long lotId, Pageable pageable);

    @Query("SELECT b FROM bids b JOIN FETCH b.user WHERE b.user.id = :userId")
    List<Bid> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM bids b JOIN FETCH b.user WHERE b.user.id = :userId")
    Page<Bid> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
