package se.skltp.takcache;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;
import se.skltp.takcache.vagval.VagvalHandler;

import static org.junit.Assert.*;
import static se.skltp.takcache.util.TestTakDataDefines.*;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.createRouting;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration("classpath*:spring-context.xml")
@TestPropertySource("classpath:test-properties.properties")
public class TakCacheTest {
    @Mock
    TakService takService;

    @Mock
    Environment env;

    @InjectMocks
    private TakCacheImpl takCache;

    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void simpleSuccessfulRefreshTest() throws Exception {
        Mockito.when(takService.getBehorigheter()).thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());
        Mockito.when(takService.getVirtualiseringar()).thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
        TakCacheLog takCacheLog = new TakCacheLog();
        boolean rc = takCache.refresh(takCacheLog);

        assertTrue( rc );
        assertTrue( takCacheLog.isRefreshSuccessful() );
        assertTrue( takCacheLog.isBehorigheterRefreshSuccessful() );
        assertTrue( takCacheLog.isVagvalRefreshSuccessful() );
        assertEquals( 4, takCacheLog.getNumberBehorigheter());
        assertEquals( 5, takCacheLog.getNumberVagval());
    }
    @Test
    public void simpleBehorighetTest() throws Exception {
        Mockito.when(takService.getBehorigheter()).thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());
        assertTrue( takCache.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
        assertFalse( takCache.isAuthorized(SENDER_3, NAMNRYMD_1, RECEIVER_1));
     }

    @Test
    public void simpleVagvalTest() throws Exception {
        Mockito.when(takService.getVirtualiseringar()).thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
        assertEquals( 0, takCache.getRoutingRivProfiles(NAMNRYMD_1, RECEIVER_1).size());
        assertEquals( 1, takCache.getRoutingRivProfiles(NAMNRYMD_1, RECEIVER_2).size());
        assertEquals( ADDRESS_1, takCache.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
        assertEquals( ADDRESS_1, takCache.getRoutingAddress(NAMNRYMD_2, RECEIVER_2, RIV20));
    }

}