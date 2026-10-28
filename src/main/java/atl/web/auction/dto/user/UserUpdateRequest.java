package atl.web.auction.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserUpdateRequest {
    @Size(max = 50, message = "Max size of firstname is 50 characters")
    private String firstname;

    @Size(max = 50, message = "Max size of lastname is 50 characters")
    private String lastname;
}
