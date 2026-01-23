package se.skltp.takcache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_OK;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.xmlunit.assertj.XmlAssert;
import se.skltp.takcache.exceptions.TakServiceException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;

@SpringJUnitConfig(locations = "classpath*:spring-context.xml")
@ExtendWith(MockitoExtension.class)
public class VagvalCacheTest {

  @TempDir
  Path testFolder;

  @Mock
  TakService takService;

  @InjectMocks
  @Autowired
  private VagvalCacheImpl vagvalCache;

  @BeforeAll
  public static void beforeClass() {
  }

  @BeforeEach
  public void beforeTest() {

    // Reset internal cache between tests
    vagvalCache.restoreCache(null);
    vagvalCache.setLocalTakCacheFileName(null);
  }

  @Test
  public void getBehorigheterShouldNotBeCalledDuringRefresh() throws Exception {
    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
    Mockito.lenient().when(takService.getBehorigheter())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));
    TakCacheLog takCacheLog = vagvalCache.refresh();

    Mockito.verify(takService, times(1)).getVirtualiseringar();
    Mockito.verify(takService, times(0)).getBehorigheter();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(5, takCacheLog.getNumberVagval());
  }

  @Test
  public void restoreFromCacheShouldWork()
      throws Exception {

    Mockito.when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("TEST: Mock-exception on call to takService.getVirtualiseringar() (for cache fallback)")));

    vagvalCache.setLocalTakCacheFileName(getLocalCacheResource());

    TakCacheLog takCacheLog = vagvalCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberVagval());
    assertEquals(0, takCacheLog.getNumberBehorigheter());

  }

  @Test
  public void saveToCacheShouldWork()
      throws Exception {

    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());

    String cachefileName = testFolder.resolve("localcache-test.xml").toString();
    vagvalCache.setLocalTakCacheFileName(cachefileName);

    TakCacheLog takCacheLog = vagvalCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    XmlAssert.assertThat(java.nio.file.Files.readString(testFolder.resolve("localcache-test.xml")))
        .hasXPath("/persistentCache/virtualiseringsInfo");

  }

  private String getLocalCacheResource() {
    return TakCacheTest.class.getClassLoader().getResource("tklocalcache-test.xml").getFile();
  }
}
