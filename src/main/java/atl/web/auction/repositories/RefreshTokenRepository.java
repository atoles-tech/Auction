package atl.web.auction.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import atl.web.auction.model.Credential;
import atl.web.auction.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByCredential(Credential credential);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
