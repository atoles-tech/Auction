package atl.web.auction.dto.user.info;

import atl.web.auction.model.util.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserShortInfoResponse implements UserResponse, Serializable { // it's a min set of data about user
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
}
