package atl.web.auction.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import atl.web.auction.dto.auction.MediaResponse;
import atl.web.auction.model.MediaLot;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    
    @Mapping(target = "lotId", source = "lot.id")
    MediaResponse toMediaResponse(MediaLot mediaLot);
}
