package atl.web.auction.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "credential_id", referencedColumnName = "id", nullable = false)
    private Credential credential;

    @Column(name = "firstname", length = 50)
    private String firstname;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @OneToMany(mappedBy = "owner")
    private List<Lot> lots;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(
        name = "favorite_lots",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "lot_id")
    )
    private List<Lot> favoriteLots;

    @OneToMany(mappedBy = "user")
    private List<Bid> bids;

    @OneToMany(mappedBy = "user")
    private List<AutoBid> autoBids;

    public void addFavoriteLot(Lot lot){
        this.favoriteLots.add(lot);
    }

    public boolean deleteFavoriteLot(Long lotId){
        Integer count = this.favoriteLots.size();

        this.favoriteLots = this.favoriteLots.stream()
            .filter(x->!x.getId().equals(lotId))
            .toList();
        
        return count != this.favoriteLots.size();
    }

    public void addBid(Bid bid){
        this.bids.add(bid);
    }

    public void addAutoBid(AutoBid autoBid){
        this.autoBids.add(autoBid);
    }
}
