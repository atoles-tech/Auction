package atl.web.auction.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import atl.web.auction.dto.user.info.UserShortInfoResponse;
import atl.web.auction.model.User;

@Mapper(componentModel = "spring", uses = {BidMapper.class, AutoBidMapper.class})
public interface UserShortInfoMapper {
    
    @Mapping(target = "role", source = "credential.role")
    @Mapping(target = "email", source = "credential.email")
    UserShortInfoResponse toUserInfoResponse(User user);

    List<UserShortInfoResponse> toUserInfoResponseList(List<User> users);

    default Page<UserShortInfoResponse> toUserInfoResponsePage(Page<User> users){
        List<User> listUsers = users.getContent();
        
        if(listUsers.isEmpty()){
            return Page.empty();
        }

        return new PageImpl<>(
            this.toUserInfoResponseList(listUsers), 
            users.getPageable(), 
            users.getSize());
    }
}
