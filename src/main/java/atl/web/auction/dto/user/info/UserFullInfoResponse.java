package atl.web.auction.dto.user.info;

import java.io.Serializable;
import java.util.List;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.auction.lots.LotFullResponse;
import atl.web.auction.dto.auction.lots.LotPublicResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserFullInfoResponse implements UserResponse, Serializable { // it's for admin and owner
    private UserShortInfoResponse userInfo;
    private List<LotFullResponse> lots; // change to full lotresponse
    private List<LotPublicResponse> favoriteLots;
    private List<BidResponse> bids;
    private List<AutoBidResponse> autoBids;
}
