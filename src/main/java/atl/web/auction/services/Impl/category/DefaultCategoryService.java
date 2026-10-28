package atl.web.auction.services.Impl.category;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.auction.dto.auction.CategoryResponse;
import atl.web.auction.exceptions.category.CategoryNotFoundException;
import atl.web.auction.mappers.CategoryMapper;
import atl.web.auction.model.Category;
import atl.web.auction.repositories.CategoryRepository;
import atl.web.auction.services.CategoryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class DefaultCategoryService implements CategoryService{
    
    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;
    
    @PostConstruct
    public void checkCategories(){
        if(!categoryRepository.findAll().isEmpty()){
            return;
        }

        Category c1 = new Category(null, "Автомобили");
        Category c2 = new Category(null, "Одежда");
        Category c3 = new Category(null, "Ценности");

        categoryRepository.save(c1);
        categoryRepository.save(c2);
        categoryRepository.save(c3);

        log.info("Not found any catageries. Create default categories");
    }

    @Override
    public List<CategoryResponse> findAll() {
        log.info("Find all categories");
        return categoryMapper.toCategoryResponseList(categoryRepository.findAll());
    }

    @Override
    public CategoryResponse findById(Long categoryId) {
        log.info("Find category by id: {}", categoryId);
        return categoryMapper.toCategoryResponse(
                categoryRepository.findById(categoryId)
                    .orElseThrow(()->new CategoryNotFoundException(categoryId))
        );
    }

    @Override
    public CategoryResponse findByTitle(String title) {
        log.info("Find category by title: {}", title);
        return categoryMapper.toCategoryResponse(
                categoryRepository.findByTitle(title)
                    .orElseThrow(()->new CategoryNotFoundException(title))
        );
    }

    @Override
    public Category findEntityByTitle(String title) {
        log.info("Find category entity by title: {}", title);
        return categoryRepository.findByTitle(title)
            .orElseThrow(()->new CategoryNotFoundException(title));
    }
    
}
