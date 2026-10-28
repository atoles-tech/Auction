package atl.web.auction.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import atl.web.auction.model.Credential;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long>{

    Optional<Credential> findByEmail(String email);

    @Query("SELECT c FROM credentials c JOIN FETCH c.refreshToken JOIN FETCH c.user WHERE c.email=:email")
    Optional<Credential> findByEmailWithJoins(@Param("email") String email);
    
    Boolean existsByEmail(String email);
}
