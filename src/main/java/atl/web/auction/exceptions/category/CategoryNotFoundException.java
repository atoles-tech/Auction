package atl.web.auction.exceptions.category;

public class CategoryNotFoundException extends RuntimeException{
    public CategoryNotFoundException(Long id){
        super("Category not found with id '" + id + "'");
    }

    public CategoryNotFoundException(String title){
        super("Category not found with title '" + title + "'");
    }
}
