package atl.web.auction.model;

import atl.web.auction.model.util.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "credentials")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Credential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, length = 80, unique = true)
    private String email;

    @Column(name = "hash_password", length = 255)
    private String hashPassword;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "credential")
    private User user;

    @OneToOne(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;
}
