package se.skltp.takcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_OK;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;

import java.io.FileReader;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.InputSource;
import se.skltp.takcache.exceptions.TakServiceException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring-context.xml")
@TestPropertySource("classpath:test-properties.properties")
public class VagvalCacheTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Mock
  TakService takService;

  @InjectMocks
  @Autowired
  private VagvalCacheImpl vagvalCache;

  @BeforeClass
  public static void beforeClass() {
  }

  @Before
  public void beforeTest() {
    MockitoAnnotations.initMocks(this);

    // Reset internal cache between tests
    vagvalCache.restoreCache(null);
    vagvalCache.setLocalTakCacheFileName(null);
  }

  @Test
  public void getBehorigheterShouldNotBeCalledDuringRefresh() throws Exception {
    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
    Mockito.when(takService.getBehorigheter())
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
        .thenThrow(new TakServiceException(new Exception("Should not happen")));

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

    String cachefileName = String
        .format("%s/localcache-test.xml", testFolder.getRoot().getPath());
    vagvalCache.setLocalTakCacheFileName(cachefileName);

    TakCacheLog takCacheLog = vagvalCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    XMLAssert.assertXpathExists("/persistentCache/virtualiseringsInfo",
        new InputSource(new FileReader(cachefileName)));

  }

  private String getLocalCacheResource() {
    return TakCacheTest.class.getClassLoader().getResource("tklocalcache-test.xml").getFile();
  }
}
