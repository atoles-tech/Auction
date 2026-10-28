package atl.web.auction.exceptions.lot;

public class LotTimeWrongException extends RuntimeException{
    public LotTimeWrongException(){
        super("Start time must be before end time");
    }

    public LotTimeWrongException(long hours){
        super(hours + " hours time doesnt enter in our timezone");
    }
}
