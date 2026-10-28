package atl.web.auction.dto.auction;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MediaResponse implements Serializable {
    private Long id;
    private Long lotId;
    private String url;
    private Boolean isMain;    
}
