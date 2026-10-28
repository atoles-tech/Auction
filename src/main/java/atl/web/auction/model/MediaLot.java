package atl.web.auction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "media_lot")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MediaLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lot_id", referencedColumnName = "id")
    private Lot lot;

    @Column(name = "url", length = 255, nullable = true)
    private String url;

    @Column(name = "is_main")
    private Boolean isMain;
    

}
