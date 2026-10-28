package atl.web.auction.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import atl.web.auction.dto.auction.BidResponse;
import atl.web.auction.model.Bid;

@Mapper(componentModel = "spring")
public interface BidMapper {
    
    @Mapping(target = "lotId", source = "lot.id")
    @Mapping(target = "ownerId", source = "user.id")
    BidResponse toBidResponse(Bid bid);

    List<BidResponse> toBidResponseList(List<Bid> bids);
    
    default Page<BidResponse> toBidResponsePage(Page<Bid> bids){
        List<Bid> listBids = bids.getContent();
        
        if(listBids.isEmpty()){
            return Page.empty();
        }

        return new PageImpl<>(
            this.toBidResponseList(listBids), 
            bids.getPageable(), 
            bids.getSize());
    }
}
