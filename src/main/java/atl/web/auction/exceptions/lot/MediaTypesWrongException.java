package atl.web.auction.exceptions.lot;

public class MediaTypesWrongException extends RuntimeException{
    public MediaTypesWrongException(){
        super("Lot can't have more then 1 main media file");
    }
}
