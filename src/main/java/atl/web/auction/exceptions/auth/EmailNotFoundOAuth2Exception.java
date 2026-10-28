package atl.web.auction.exceptions.auth;

public class EmailNotFoundOAuth2Exception extends RuntimeException{
    public EmailNotFoundOAuth2Exception(){
        super("Email not found in your oauth2");
    }
}
