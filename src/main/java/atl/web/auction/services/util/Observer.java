package atl.web.auction.services.util;

import atl.web.auction.model.AutoBid;
import atl.web.auction.model.Bid;
import atl.web.auction.model.Lot;

public interface Observer {
    void notify(Lot lot);
    void notifyAddBid(Bid bid, Long lotId);    
    void notifyAddAutoBid(AutoBid autoBid, Long lotId);
    void notifyRemoveAutoBid(Long autoBidId);
}
