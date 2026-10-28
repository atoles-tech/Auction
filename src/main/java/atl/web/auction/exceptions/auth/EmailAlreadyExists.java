package atl.web.auction.exceptions.auth;

public class EmailAlreadyExists extends RuntimeException{
    public EmailAlreadyExists(String email){
        super("Email '" + email + "' already exists in system");
    }
}
