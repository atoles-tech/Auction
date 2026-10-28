package atl.web.auction.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import atl.web.auction.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    
    @Query("SELECT u FROM users u JOIN FETCH u.credential c WHERE c.email=:email")
    Optional<User> findByEmail(@Param("email") String email); 

    Page<User> findAll(Pageable pageable);

}
