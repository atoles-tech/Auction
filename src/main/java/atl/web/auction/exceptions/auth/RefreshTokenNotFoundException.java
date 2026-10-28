package atl.web.auction.exceptions.auth;

public class RefreshTokenNotFoundException extends RuntimeException{
    public RefreshTokenNotFoundException(){
        super("Refresh token not found! Check your token");
    }
}
