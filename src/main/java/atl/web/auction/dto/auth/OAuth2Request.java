package atl.web.auction.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OAuth2Request {
    private String email;
    private String firstname;
    private String lastname;
}
