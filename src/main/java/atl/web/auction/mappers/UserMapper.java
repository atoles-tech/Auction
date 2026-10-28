package atl.web.auction.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import atl.web.auction.dto.user.info.UserFullInfoResponse;
import atl.web.auction.model.User;

@Mapper(componentModel = "spring", uses = {
    BidMapper.class, 
    AutoBidMapper.class, 
    UserShortInfoMapper.class , 
    LotMapper.class}
)
public interface UserMapper {
    
    @Mapping(target = "userInfo", source = "user")
    UserFullInfoResponse toUserResponse(User user);

    List<UserFullInfoResponse> toUserResponseList(List<User> users);

    default Page<UserFullInfoResponse> toUserResponsePage(Page<User> users){
        List<User> listUsers = users.getContent();
        
        if(listUsers.isEmpty()){
            return Page.empty();
        }

        return new PageImpl<>(
            this.toUserResponseList(listUsers), 
            users.getPageable(), 
            users.getSize());
    }
}
