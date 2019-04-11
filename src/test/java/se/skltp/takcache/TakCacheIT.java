package se.skltp.takcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.skltp.takcache.services.TakServiceImpl.ENDPOINT_ADDRESS_PROPERTY_NAME;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;
import se.skltp.takcache.util.SokVagvalsInfoMockWebService;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration("classpath*:spring-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TakCacheIT {
    public static final String TAK_SOKVALSINFO_ADDRESS = "http://localhost:%d/sokvagvalsinfo";

    private static SokVagvalsInfoMockWebService mockWebService;

    @Autowired
    TakCache takCache;

    @Autowired
    VagvalCache vagvalCache;

    @Autowired
    BehorighetCache behorighetCache;

    @BeforeClass
    public static void before(){
        String address = String.format(TAK_SOKVALSINFO_ADDRESS, SocketUtils.findAvailableTcpPort(8080));
        System.setProperty(ENDPOINT_ADDRESS_PROPERTY_NAME, address);
        mockWebService = new SokVagvalsInfoMockWebService(address);
    }

    @Test
    public void simpleSuccessfulRefreshTest() throws Exception {

        mockWebService.start();

        TakCacheLog takCacheLog  = takCache.refresh();

        assertTrue( takCacheLog.isRefreshSuccessful() );
        assertEquals( RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus() );
        assertEquals( 5, takCacheLog.getNumberBehorigheter());
        assertEquals( 5, takCacheLog.getNumberVagval());

        mockWebService.stop();

    }

    @Test
    public void simpleSuccessfulVagvalRefreshTest() throws Exception {

        mockWebService.start();

        TakCacheStatus takCacheLog  = vagvalCache.refresh();

        assertTrue( takCacheLog.isRefreshSuccessful() );
        assertEquals( RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus() );
        assertEquals( 5, takCacheLog.getNumberInCache());

        mockWebService.stop();

    }

    @Test
    public void simpleSuccessfulBehorighetRefreshTest() throws Exception {

        mockWebService.start();

        TakCacheStatus takCacheLog  = behorighetCache.refresh();

        assertTrue( takCacheLog.isRefreshSuccessful() );
        assertEquals( RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus() );
        assertEquals( 5, takCacheLog.getNumberInCache());

        mockWebService.stop();

    }
}
