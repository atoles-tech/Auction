package atl.web.auction.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.dto.auction.lots.LotFullResponse;
import atl.web.auction.dto.auction.lots.LotPublicResponse;
import atl.web.auction.model.Bid;
import atl.web.auction.model.Lot;

@Mapper(componentModel = "spring", uses = {UserShortInfoMapper.class, BidMapper.class, AutoBidMapper.class, MediaMapper.class})
public interface LotMapper {
    
    @Mapping(target = "category", source = "category.title")
    @Mapping(target = "winnerBid", expression = "java(findWinningBid(lot.getBids(), bidMapper))")
    LotPublicResponse toLotPublicResponse(Lot lot);

    @Mapping(target = "winnerBid", expression = "java(findWinningBid(lot.getBids(), bidMapper))")
    @Mapping(target = "lotInfo", source = "lot")
    LotFullResponse toLotFullResponse(Lot lot);

    List<LotPublicResponse> toLotPublicResponseList(List<Lot> lots);

    List<LotFullResponse> toLotFullResponseList(List<Lot> lots);

    default Page<LotPublicResponse> toLotPublicResponsePage(List<Lot> lots, Pageable pageable){
        if(lots.isEmpty()){
            return Page.empty();
        }

        return new PageImpl<>(
            this.toLotPublicResponseList(lots), 
            pageable, 
            lots.size());
    }

    default Page<LotPublicResponse> toLotPublicResponsePage(Page<Lot> lots, Pageable pageable){
        if(lots.isEmpty()){
            return Page.empty();
        }

        return new PageImpl<>(
            this.toLotPublicResponseList(lots.getContent()), 
            pageable, 
            lots.getContent().size());
    }

    default Page<LotFullResponse> toLotFullResponsePage(Page<Lot> lots, Pageable pageable){
        if(lots.isEmpty()){
            return Page.empty();
        }

        return new PageImpl<>(
            this.toLotFullResponseList(lots.getContent()), 
            pageable, 
            lots.getContent().size());
    }

    @Named("findWinningBid")
    default BidResponse findWinningBid(List<Bid> bids, BidMapper bidMapper) {
        if (bids == null || bids.isEmpty()) {
            return null;
        }
        return bids.stream()
            .max((a, b) -> Double.compare(a.getPrice(), b.getPrice()))
            .map(bidMapper::toBidResponse)
            .orElse(null);
    }
}
