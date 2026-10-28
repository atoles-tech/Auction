package atl.web.auction.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import atl.web.auction.mappers.BidMapper;
import atl.web.auction.model.AutoBid;
import atl.web.auction.model.Bid;
import atl.web.auction.model.Lot;
import atl.web.auction.services.LotService;
import atl.web.auction.services.util.Observer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NotificationController implements Observer{
    
    private final LotService lotService;
    
    private final SimpMessagingTemplate messagingTemplate;
    
    private final BidMapper bidMapper;

    @PostConstruct
    public void init(){
        lotService.subscribe(this);
    }

    @Override
    public void notify(Lot lot) {
        return;        
    }

    @Override
    public void notifyAddAutoBid(AutoBid autoBid, Long lotId) {
        return;        
    }

    @Override
    public void notifyAddBid(Bid bid, Long lotId) {
        messagingTemplate.convertAndSend("/topic/lots/" + lotId, bidMapper.toBidResponse(bid));
    }

    @Override
    public void notifyRemoveAutoBid(Long autoBidId) {
        return;        
    }

}
