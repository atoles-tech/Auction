package atl.web.auction.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import atl.web.auction.model.util.LotStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "lots")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 80, nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "start_in", nullable = false)
    private LocalDateTime startIn;

    @Column(name = "end_in", nullable = false)
    private LocalDateTime endIn;

    @Column(name = "start_price", nullable = false)
    private Double startPrice;

    @Column(name = "min_step")
    private Double minStep; // for price

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15)
    private LotStatus status;

    @OneToMany(mappedBy = "lot", fetch = FetchType.EAGER)
    private List<Bid> bids;

    @OneToMany(mappedBy = "lot", fetch = FetchType.EAGER)
    private List<AutoBid> autoBids;

    @OneToMany(mappedBy = "lot", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaLot> media;

    public void addBid(Bid bid) {
        if(this.bids == null){
            this.bids = new ArrayList<>();
        }

        this.bids.add(bid);
    }

    public void addAutoBid(AutoBid autoBid) {
        if(this.autoBids == null){
            this.autoBids = new ArrayList<>();
        }
        this.autoBids.add(autoBid);
    }

    public void removeAutoBid(Long autoBidId){
        Optional<AutoBid> autoBid = this.autoBids.stream()
            .filter(ab->ab.getId().equals(autoBidId))
            .findFirst();

        if(autoBid.isPresent()){
            autoBids.remove(autoBid.get());
        }
    }
}
