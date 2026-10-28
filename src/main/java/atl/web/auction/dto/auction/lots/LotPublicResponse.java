package atl.web.auction.dto.auction.lots;

import java.time.LocalDateTime;
import java.util.List;

import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.auction.MediaResponse;
import atl.web.auction.dto.user.info.UserShortInfoResponse;
import atl.web.auction.model.util.LotStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LotPublicResponse implements LotResponse{
    private Long id;
    private String title;
    private String category;
    private UserShortInfoResponse owner; // Short info about user
    private LocalDateTime startIn;
    private LocalDateTime endIn;
    private LotStatus status;
    private Double startPrice;
    private Double minStep;
    private List<MediaResponse> media;
    private BidResponse winnerBid;
}
