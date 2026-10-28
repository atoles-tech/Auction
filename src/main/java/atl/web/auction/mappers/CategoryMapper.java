package atl.web.auction.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import atl.web.auction.dto.auction.CategoryResponse;
import atl.web.auction.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);
}
