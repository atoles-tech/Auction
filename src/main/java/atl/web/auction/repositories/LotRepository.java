package atl.web.auction.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import atl.web.auction.model.Category;
import atl.web.auction.model.Lot;
import atl.web.auction.model.util.LotStatus;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    Page<Lot> findAll(Specification<Lot> spec, Pageable pageable);

    List<Lot> findByCategory(Category category);

    Page<Lot> findByCategory(Category category, Pageable pageable);

    List<Lot> findByStatus(LotStatus status);

    Page<Lot> findByStatus(LotStatus status, Pageable pageable);

    @Query("SELECT l FROM lots l WHERE l.title LIKE %:title%")
    List<Lot> findByTitle(@Param("title") String title);

    @Query("SELECT l FROM lots l WHERE LOWER(l.title) LIKE %:title%")
    Page<Lot> findByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT l FROM lots l WHERE l.owner.id=:userId")
    List<Lot> findByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM lots l WHERE l.owner.id=:userId")
    Page<Lot> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT l FROM lots l LEFT JOIN FETCH l.bids LEFT JOIN FETCH l.autoBids WHERE l.id = :id")
    Optional<Lot> findWithBidsAndAutoBids(@Param("id") Long id);

}
