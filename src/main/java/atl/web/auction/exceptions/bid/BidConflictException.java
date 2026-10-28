package atl.web.auction.exceptions.bid;

public class BidConflictException extends RuntimeException{
    public BidConflictException(Double price){
        super("Can't create bid with price=" + price);
    }
}
