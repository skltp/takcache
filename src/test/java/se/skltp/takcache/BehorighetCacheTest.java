package se.skltp.takcache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_OK;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;

import java.nio.file.Path;
import java.util.Objects;

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
class BehorighetCacheTest {
  @TempDir
  Path testFolder;

  @Mock
  TakService takService;

  @InjectMocks
  @Autowired
  private BehorigheterCacheImpl behorigheterCache;

  @BeforeEach
  void beforeTest() {

    // Reset internal cache between tests
    behorigheterCache.restoreCache(null);
    behorigheterCache.setLocalTakCacheFileName(null);
  }

  @Test
  void getVirtualiseringarShouldNotBeCalledDuringRefresh() throws Exception {
    Mockito.lenient().when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));
    Mockito.when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());
    TakCacheLog takCacheLog = behorigheterCache.refresh();

    Mockito.verify(takService, times(0)).getVirtualiseringar();
    Mockito.verify(takService, times(1)).getBehorigheter();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(5, takCacheLog.getNumberBehorigheter());

  }

  @Test
  void restoreFromCacheShouldWork()
      throws Exception {

    Mockito.when(takService.getBehorigheter())
        .thenThrow(new TakServiceException(new Exception("TEST: Mock-exception on call to takService.getBehorigheter() (for cache fallback)")));

    behorigheterCache.setLocalTakCacheFileName(getLocalCacheResource());

    TakCacheLog takCacheLog = behorigheterCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(0, takCacheLog.getNumberVagval());

  }

  @Test
  void saveToCacheShouldWork()
      throws Exception {

    Mockito.when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());

    String cachefileName = testFolder.resolve("localcache-test.xml").toString();
    behorigheterCache.setLocalTakCacheFileName(cachefileName);

    TakCacheLog takCacheLog = behorigheterCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    XmlAssert.assertThat(java.nio.file.Files.readString(testFolder.resolve("localcache-test.xml")))
        .hasXPath("/persistentCache/anropsBehorighetsInfo");

  }


  private String getLocalCacheResource() {
    return Objects.requireNonNull(TakCacheTest.class.getClassLoader().getResource("tklocalcache-test.xml")).getFile();
  }


}
