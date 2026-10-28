package atl.web.auction.controllers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import atl.web.auction.exceptions.auth.CredentialNotFoundException;
import atl.web.auction.exceptions.auth.EmailAlreadyExists;
import atl.web.auction.exceptions.auth.EmailNotFoundOAuth2Exception;
import atl.web.auction.exceptions.auth.IncorrectPasswordOrEmailException;
import atl.web.auction.exceptions.auth.IncorrectTypeAuthException;
import atl.web.auction.exceptions.auth.RefreshTokenNotFoundException;
import atl.web.auction.exceptions.bid.AutoBidConflictException;
import atl.web.auction.exceptions.bid.AutoBidNotFoundException;
import atl.web.auction.exceptions.bid.BidNotFoundException;
import atl.web.auction.exceptions.category.CategoryNotFoundException;
import atl.web.auction.exceptions.lot.LotNotFoundException;
import atl.web.auction.exceptions.lot.LotTimeWrongException;
import atl.web.auction.exceptions.lot.MediaTypesWrongException;
import atl.web.auction.exceptions.user.UserNotFoundException;

@ControllerAdvice
public class ExceptionController {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
        ex.printStackTrace(); 
        return ResponseEntity.status(500).body(ex.getMessage());
    }

    @ExceptionHandler({
        CredentialNotFoundException.class,
        RefreshTokenNotFoundException.class,
        AutoBidNotFoundException.class,
        BidNotFoundException.class,
        CategoryNotFoundException.class,
        LotNotFoundException.class,
        UserNotFoundException.class,
        EmailNotFoundOAuth2Exception.class
    })
    public ResponseEntity<String> handleNotFoundExceptions(RuntimeException exception){
        return new ResponseEntity<String>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
        IncorrectPasswordOrEmailException.class,
        IncorrectTypeAuthException.class
    })
    public ResponseEntity<String> handleWrongPasswordOrEmail(RuntimeException exception){
        return new ResponseEntity<String>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
        AutoBidConflictException.class,
        EmailAlreadyExists.class
    })
    public ResponseEntity<String> handleExistsException(RuntimeException exception){
        return new ResponseEntity<String>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
        MediaTypesWrongException.class,
        LotTimeWrongException.class,
    })
    public ResponseEntity<String> handleWrongInput(RuntimeException exception){
        return new ResponseEntity<String>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }


}
