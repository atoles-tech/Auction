package atl.web.auction.exceptions.auth;

public class IncorrectTypeAuthException extends RuntimeException{
    public IncorrectTypeAuthException(){
        super("Incorrect type authentication. Try use oauth 2.0 services");
    }
}
