package se.skltp.takcache;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import org.springframework.test.util.TestSocketUtils;
import se.skltp.takcache.util.SokVagvalsInfoMockWebService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_OK;
import static se.skltp.takcache.services.TakServiceImpl.ENDPOINT_ADDRESS_PROPERTY_NAME;

@SpringJUnitConfig(locations = "classpath*:spring-context.xml")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class TakCacheIT {
    public static final String TAK_SOKVALSINFO_ADDRESS = "http://localhost:%d/sokvagvalsinfo";

    private static SokVagvalsInfoMockWebService mockWebService;
    private static String originalEndpointProperty;

    @Autowired
    TakCache takCache;

    @BeforeAll
    public static void before(){
        // Save original property value
        originalEndpointProperty = System.getProperty(ENDPOINT_ADDRESS_PROPERTY_NAME);

        String address = String.format(TAK_SOKVALSINFO_ADDRESS, TestSocketUtils.findAvailableTcpPort());
        System.setProperty(ENDPOINT_ADDRESS_PROPERTY_NAME, address);
        mockWebService = new SokVagvalsInfoMockWebService(address);
    }

    @AfterAll
    public static void afterAll() {
        // Clean up system property
        if (originalEndpointProperty == null) {
            System.clearProperty(ENDPOINT_ADDRESS_PROPERTY_NAME);
        } else {
            System.setProperty(ENDPOINT_ADDRESS_PROPERTY_NAME, originalEndpointProperty);
        }

        // Ensure mock web service is stopped
        if (mockWebService != null) {
            try {
                mockWebService.stop();
            } catch (Exception e) {
                // Ignore if already stopped
            }
        }
    }

    @Test
    public void simpleSuccessfulRefreshTest() throws Exception {

        mockWebService.start();

        TakCacheLog takCacheLog  = takCache.refresh();

        assertTrue( takCacheLog.isRefreshSuccessful() );
        assertEquals( REFRESH_OK, takCacheLog.getRefreshStatus() );
        assertEquals( 5, takCacheLog.getNumberBehorigheter());
        assertEquals( 5, takCacheLog.getNumberVagval());

        mockWebService.stop();

    }
}
