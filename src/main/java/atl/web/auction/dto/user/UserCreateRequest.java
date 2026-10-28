package atl.web.auction.dto.user;

import jakarta.validation.constraints.Size;
import atl.web.auction.model.util.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserCreateRequest {
    @Email(message = "Email must be email:)")
    @Size(max = 80, message = "Max size of email is 80 characters")
    private String email;

    @NotBlank(message = "Password can't be empty or constatins only spaces")
    @Size(min = 6, max = 24, message = "Password can be only between 6 and 24 characters")
    private String password; //raw

    @Size(max = 50, message = "Max size of firstname is 50 characters")
    private String firstname;

    @Size(max = 50, message = "Max size of lastname is 50 characters")
    private String lastname;

    @NotNull(message = "Role is required")
    private Role role;
}
