package se.skltp.takcache;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_OK;
import static se.skltp.takcache.services.TakServiceImpl.ENDPOINT_ADDRESS_PROPERTY_NAME;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration("classpath*:spring-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TakCacheIT {
    public static final String TAK_SOKVALSINFO_ADDRESS = "http://localhost:%d/sokvagvalsinfo";

    private static SokVagvalsInfoMockWebService mockWebService;

    @Autowired
    TakCache takCache;

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
        assertEquals( REFRESH_OK, takCacheLog.getBehorigheterRefreshStatus() );
        assertEquals( REFRESH_OK, takCacheLog.getVagvalRefreshStatus() );
        assertEquals( 5, takCacheLog.getNumberBehorigheter());
        assertEquals( 5, takCacheLog.getNumberVagval());

        mockWebService.stop();

    }
}
