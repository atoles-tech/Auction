package atl.web.auction.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import atl.web.auction.dto.auction.AutoBidResponse;
import atl.web.auction.model.AutoBid;

@Mapper(componentModel = "spring")
public interface AutoBidMapper {
    
    @Mapping(target = "lotId", source = "lot.id")
    @Mapping(target = "ownerId", source = "user.id")
    AutoBidResponse toAutoBidResponse(AutoBid autoBid);

    List<AutoBidResponse> toAutoBidResponseList(List<AutoBid> autoBids);

    default Page<AutoBidResponse> toAutoBidResponsePage(Page<AutoBid> bids){
        List<AutoBid> listBids = bids.getContent();
        
        if(listBids.isEmpty()){
            return Page.empty();
        }

        return new PageImpl<>(
            this.toAutoBidResponseList(listBids), 
            bids.getPageable(), 
            bids.getSize());
    }
}
