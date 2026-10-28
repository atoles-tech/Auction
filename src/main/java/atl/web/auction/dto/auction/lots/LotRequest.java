package atl.web.auction.dto.auction.lots;

import java.time.LocalDateTime;
import java.util.List;

import atl.web.auction.dto.auction.MediaRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LotRequest {
    private String title;
    private String category;
    private LocalDateTime startIn;
    private LocalDateTime endIn;
    private Double startPrice;
    private Double minStep;
    private List<MediaRequest> mediaUrls;
}
