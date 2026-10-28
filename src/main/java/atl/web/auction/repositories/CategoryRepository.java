package atl.web.auction.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import atl.web.auction.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByTitle(String title);
}
