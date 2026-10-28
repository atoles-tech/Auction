package atl.web.auction.services;

import java.util.List;

import atl.web.auction.dto.auction.CategoryResponse;
import atl.web.auction.model.Category;

public interface CategoryService {
    CategoryResponse findById(Long categoryId);
    CategoryResponse findByTitle(String title);

    List<CategoryResponse> findAll();
    Category findEntityByTitle(String title);
}
