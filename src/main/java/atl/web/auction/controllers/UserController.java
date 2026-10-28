package atl.web.auction.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import atl.web.auction.dto.user.UserCreateRequest;
import atl.web.auction.dto.user.UserUpdateRequest;
import atl.web.auction.jwt.UserDetailsImpl;
import atl.web.auction.services.LotService;
import atl.web.auction.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class UserController {

    private final UserService userService;
    private final LotService lotService;

    @GetMapping("/users/me")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> findCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseEntity.ok(userService.findByEmail(userDetailsImpl.getUsername(), userDetailsImpl.getUsername()));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> findAllUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "0") Integer size,
            @RequestParam(defaultValue = "lastname") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        if (size == 0) {
            return ResponseEntity.ok(userService.findAllUsers(userDetailsImpl.getUsername()));
        }
        Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        return ResponseEntity
                .ok(userService.findAllUsers(userDetailsImpl.getUsername(), PageRequest.of(page, size, sort)));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseEntity.ok(userService.findById(id, userDetailsImpl.getUsername()));
    }

    @GetMapping("/users/{id}/lots")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> findUsersLots(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "0") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        if (size == 0) {
            return ResponseEntity.ok(lotService.findByUserId(id, userDetailsImpl.getUsername()));
        }

        Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity
                .ok(lotService.findByUserId(id, PageRequest.of(page, size, sort), userDetailsImpl.getUsername()));
    }

    @GetMapping("/users/{id}/favorite-lots")
    @PreAuthorize("hasRole('ADMIN') or ((hasRole('USER') or hasRole('SELLER')) and #id==#userDetailsImpl.getId())")
    public ResponseEntity<?> findFavoriteLots(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "0") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

        if (size == 0) {
            return ResponseEntity.ok(userService.findFavoriteLotsForUser(id));
        }

        Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity
                .ok(userService.findFavoriteLotsForUser(id, PageRequest.of(page, size, sort)));
    }

    @PostMapping("/users/favorite-lot/{id}")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> addFavoriteLot(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseEntity.ok(userService.addLotToFavorite(lotService.findEntityLot(id), userDetailsImpl.getId()));
    }

    @DeleteMapping("/users/favorite-lot/{id}")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> deleteFavoriteLot(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        userService.deleteFavoriteLot(id, userDetailsImpl.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/users")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> updateUser(
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        userService.updateUser(userDetailsImpl.getId(), request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/users/become-seller")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> becomeSeller(
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseEntity.ok(userService.becomeSeller(userDetailsImpl.getId()));
    }

    @GetMapping("/users/{id}/bids")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> findBids(
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "0") Integer size,
        @RequestParam(defaultValue = "lot") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @PathVariable Long id
    ) {
        if (size == 0) {
            return ResponseEntity.ok(userService.findBids(id));
        }

        Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        return ResponseEntity.ok(userService.findBids(id, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/users/{id}/auto-bids")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> findAutoBids(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "0") Integer size,
            @RequestParam(defaultValue = "lot") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @PathVariable Long id
    ) {
        if (size == 0) {
            return ResponseEntity.ok(userService.findAutoBids(id));
        }

        Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        return ResponseEntity.ok(userService.findAutoBids(id, PageRequest.of(page, size, sort)));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserCreateRequest request){
        return ResponseEntity.ok(userService.createUser(request));
    }

}
