package atl.web.auction.exceptions.bid;

public class AutoBidConflictException extends RuntimeException{
    public AutoBidConflictException(Double price){
        super("Can't create auto bid with price=" + price);
    }

    public AutoBidConflictException(){
        super("You have another auto bid for this lot");
    }
}
