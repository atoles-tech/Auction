package atl.web.auction.controllers;

import java.time.LocalDateTime;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import atl.web.auction.dto.auction.BidRequest;
import atl.web.auction.dto.auction.lots.LotRequest;
import atl.web.auction.jwt.UserDetailsImpl;
import atl.web.auction.model.filters.LotFilter;
import atl.web.auction.model.util.LotStatus;
import atl.web.auction.services.AutoBidService;
import atl.web.auction.services.BidService;
import atl.web.auction.services.LotService;
import atl.web.auction.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class LotController {

    private final LotService lotService;
    private final BidService bidService;
    private final AutoBidService autoBidService;

    @GetMapping("/lots")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> findAllLots(
            @RequestParam(required = false) LotStatus status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) LocalDateTime startIn,
            @RequestParam(required = false) LocalDateTime endIn,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "0") Integer size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

        if (size == 0) {
            return ResponseEntity.ok(lotService.findAllLots());
        }

        Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        return ResponseEntity.ok(lotService.findAllLots(new LotFilter(title, startIn, endIn, status), PageRequest.of(page, size, sort)).getContent());
    }

    @GetMapping("/lots/{id}")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<?> findLot(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseEntity.ok(lotService.findById(id, userDetailsImpl.getUsername()));
    }

    @GetMapping("/lots/{id}/bids")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    public ResponseEntity<?> findLotBids(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseEntity.ok(bidService.findByLotId(id));
    }

    @GetMapping("/lots/{id}/auto-bids")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findLotAutoBids(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseEntity.ok(autoBidService.findByLotId(id));
    }

    @GetMapping("/lots/{id}/my-auto-bid")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SELLER')")
    public ResponseEntity<?> findMyLotAutoBid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseEntity.ok(lotService.findMyAutoBidByLot(id, userDetailsImpl.getId()));
    }

    @PostMapping("/bids")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> createBid(
        @RequestBody @Valid BidRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl
    ){
        return ResponseEntity.ok(lotService.createBid(request, userDetailsImpl.getId()));
    }

    @PostMapping("/auto-bids")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> createAutoBid(
        @RequestBody @Valid BidRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl
    ){
        return ResponseEntity.ok(lotService.createAutoBid(request, userDetailsImpl.getId()));
    }

    @PostMapping("/lots")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> createLot(
        @RequestBody LotRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl
    ){
        return ResponseEntity.ok(lotService.createLot(request, userDetailsImpl.getId()));
    }

    @DeleteMapping("/auto-bids/{id}")
    @PreAuthorize("hasAnyRole('ADMIN') or (hasAnyRole('USER','SELLER') and @defaultUserService.isUserOwner(#id, #userDetailsImpl.getId()))")
    public ResponseEntity<?> deleteAutoBid(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl userDetailsImpl
    ){
        lotService.deleteAutoBid(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
