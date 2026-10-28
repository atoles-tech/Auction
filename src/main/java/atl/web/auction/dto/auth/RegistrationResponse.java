package atl.web.auction.dto.auth;

import atl.web.auction.model.util.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RegistrationResponse {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
}
