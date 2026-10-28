package atl.web.auction.exceptions.auth;

public class IncorrectPasswordOrEmailException extends RuntimeException{
    public IncorrectPasswordOrEmailException(){
        super("Incorrect password or email. Check your data again");
    }
}
