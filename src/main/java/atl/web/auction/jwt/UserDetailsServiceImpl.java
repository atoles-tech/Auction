package atl.web.auction.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import atl.web.auction.exceptions.auth.CredentialNotFoundException;
import atl.web.auction.model.Credential;
import atl.web.auction.repositories.CredentialRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService{

    private CredentialRepository credentialRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Credential credential = credentialRepository
            .findByEmailWithJoins(username)
            .orElseThrow(()->new CredentialNotFoundException(username));

        return UserDetailsImpl.builder()
            .id(credential.getUser().getId())
            .role(credential.getRole())
            .username(username)
            .hashPassword(credential.getHashPassword())
            .build();
    }
    
}
