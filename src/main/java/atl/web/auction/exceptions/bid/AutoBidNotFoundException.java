package atl.web.auction.exceptions.bid;

public class AutoBidNotFoundException extends RuntimeException{
    public AutoBidNotFoundException(Long id){
        super("AutoBid not found with id '" + id + "'");
    }
}
