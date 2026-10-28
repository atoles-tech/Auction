package atl.web.auction.services.lot;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import atl.web.auction.services.LotChecker;
import atl.web.auction.services.LotService;

@ExtendWith(MockitoExtension.class)
public class DefaultLotCheckerTest {

    @Mock
    private LotService lotService;
    
    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private LotChecker lotChecker;
}
