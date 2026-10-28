package atl.web.auction.model.filters;

import java.time.LocalDateTime;

import atl.web.auction.model.util.LotStatus;

public record LotFilter(String title, LocalDateTime startIn, LocalDateTime endIn, LotStatus status) {
} 
