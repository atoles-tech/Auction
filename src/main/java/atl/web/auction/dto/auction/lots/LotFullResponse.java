package atl.web.auction.dto.auction.lots;

import java.util.List;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.dto.auction.BidResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LotFullResponse implements LotResponse{
    private LotPublicResponse lotInfo;
    private List<BidResponse> bids;
    private List<AutoBidResponse> autoBids;
    private BidResponse winnerBid;
}
