package atl.web.auction.exceptions.auth;

public class CredentialNotFoundException extends RuntimeException{
    public CredentialNotFoundException(String email){
        super("Credential with email '" + email + "' not found");
    }
}
