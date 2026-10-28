package atl.web.auction.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "bids")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", referencedColumnName = "id",nullable = false)
    private Lot lot;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "user_id", nullable = false)
    private User user;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
}
