package atl.web.auction.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FavoriteLotRequest {
    @NotNull(message = "Lot id is required")
    private Long lotId;
}
