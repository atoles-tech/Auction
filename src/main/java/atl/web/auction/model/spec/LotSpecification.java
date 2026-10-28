package atl.web.auction.model.spec;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import atl.web.auction.model.Lot;
import atl.web.auction.model.filters.LotFilter;
import atl.web.auction.model.util.LotStatus;

public class LotSpecification{
    public static final String TITLE = "title";
    public static final String START_IN = "startIn";
    public static final String END_IN = "endIn";
    public static final String STATUS = "status";

    public static Specification<Lot> filterBy(LotFilter lotFilter){
        return hasTitle(lotFilter.title())
                    .and(hasStatus(lotFilter.status()))
                    .and(afterStartIn(lotFilter.startIn()))
                    .and(beforeEndIn(lotFilter.endIn()));
    }

    public static Specification<Lot> hasTitle(String title){
        return ((root, query, cb)-> title == null || title.isEmpty()?cb.conjunction():cb.like(root.get(TITLE),"%" + title.toLowerCase() + "%"));
    }

    public static Specification<Lot> afterStartIn(LocalDateTime startIn){
        return ((root, query, cb)-> startIn == null?cb.conjunction():cb.greaterThan(root.get(START_IN), startIn));
    }

    public static Specification<Lot> beforeEndIn(LocalDateTime endIn){
        return ((root, query, cb)-> endIn == null?cb.conjunction():cb.lessThan(root.get(END_IN), cb.literal(endIn)));
    }

    public static Specification<Lot> hasStatus(LotStatus status){
        return ((root, query, cb)-> status == null?cb.conjunction():cb.equal(root.get(STATUS), cb.literal(status)));
    }
}
