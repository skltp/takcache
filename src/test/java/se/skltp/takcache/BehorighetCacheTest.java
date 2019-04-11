package se.skltp.takcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static se.skltp.takcache.util.TestTakDataDefines.NAMNRYMD_1;
import static se.skltp.takcache.util.TestTakDataDefines.NAMNRYMD_2;
import static se.skltp.takcache.util.TestTakDataDefines.RECEIVER_1;
import static se.skltp.takcache.util.TestTakDataDefines.SENDER_1;
import static se.skltp.takcache.util.TestTakDataDefines.SENDER_3;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.InputSource;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.takcache.exceptions.TakServiceException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;

public class BehorighetCacheTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Mock
  TakService takService;


  @InjectMocks
  @Autowired
  private BehorighetCacheImpl behorighetCache;

  @BeforeClass
  public static void beforeClass() {

  }

  @Before
  public void beforeTest() {
    MockitoAnnotations.initMocks(this);

    // Reset internal cache between tests
    behorighetCache.behorighetCache = null;
    behorighetCache.tjanstegranssnittFilter = null;
    behorighetCache.setLocalTakCacheFileName(null);
  }

  @Test
  public void simpleSuccessfulRefreshTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheStatus takCacheStatus = behorighetCache.refresh();

    assertTrue(takCacheStatus.isRefreshSuccessful());
    assertEquals(RefreshStatus.REFRESH_OK, takCacheStatus.getRefreshStatus());
    assertEquals(5, takCacheStatus.getNumberInCache());
  }

  @Test
  public void simpleBehorighetTest() throws Exception {
    mockTakServiceDefaultValues();
    assertTrue(behorighetCache.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
    assertTrue(behorighetCache.isAuthorized(SENDER_1, NAMNRYMD_2, RECEIVER_1));
    assertFalse(behorighetCache.isAuthorized(SENDER_3, NAMNRYMD_1, RECEIVER_1));
  }

  @Test
  public void filterShouldRemoveNotMatchingBehorighetTest() throws Exception {
    mockTakServiceDefaultValues();

    TakCacheStatus takCacheStatus = behorighetCache.refresh(NAMNRYMD_1);
    assertEquals(4, takCacheStatus.getNumberInCache());
    assertTrue(behorighetCache.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
    assertFalse(behorighetCache.isAuthorized(SENDER_1, NAMNRYMD_2, RECEIVER_1));
  }

  @Test
  public void noBehorighetFromTakShouldGiveBehorighetFalseTest() throws Exception {
    Mockito.when(takService.getBehorigheter())
        .thenReturn(Collections.<AnropsBehorighetsInfoType>emptyList());
    TakCacheStatus takCacheLog = behorighetCache.refresh();
    assertFalse(behorighetCache.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
  }

  @Test
  public void noContactWithTjanstekatalogenAlwaysReultsInLocalCacheIsRead()
      throws Exception {

    behorighetCache.setLocalTakCacheFileName(getLocalCacheResource());

     Mockito.when(takService.getBehorigheter())
        .thenThrow(new TakServiceException(new Exception("Failed get behorigheter from TAK")));

    TakCacheStatus takCacheLog = behorighetCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberInCache());
  }

  @Test
  public void emptyAnswerFromTjanstekatalogenAlwaysReultsInLocalCacheIsRead()
      throws Exception {


    behorighetCache.setLocalTakCacheFileName(getLocalCacheResource());

     Mockito.when(takService.getBehorigheter()).thenReturn(new ArrayList<>());

    TakCacheStatus takCacheLog = behorighetCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberInCache());

  }

  @Test
  public void emptyBehorigheterFromTjanstekatalogenAlwaysReultsInLocalCacheIsRead()
      throws Exception {


    behorighetCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getBehorigheter()).thenReturn(new ArrayList<>());

    TakCacheStatus takCacheLog = behorighetCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberInCache());

  }


  @Test
  public void noDataAfterFilterShouldReultsInLocalCacheIsRead()
      throws Exception {

    behorighetCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());

    TakCacheStatus  takCacheLog = behorighetCache.refresh("NamespaceNoMatch");

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberInCache());

  }

  @Test
  public void contactWithTjanstekatalogenAlwaysReultsInLocalCacheIsUpdated()
      throws Exception {

    mockTakServiceDefaultValues();

    String cachefileName = String.format("%s/localcache-test.xml", testFolder.getRoot().getPath());
    behorighetCache.setLocalTakCacheFileName(cachefileName);

    TakCacheStatus  takCacheLog = behorighetCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus());
    XMLAssert.assertXpathExists("/persistentCache/anropsBehorighetsInfo",
        new InputSource(new FileReader(cachefileName)));
  }

  @Test
  public void ifExistingCacheExistItShouldBeUsedIfRefreshFailes()
      throws Exception {

    mockTakServiceDefaultValues();

    TakCacheStatus takCacheLog = behorighetCache.refresh();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals( RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus() );
    assertEquals( 5, takCacheLog.getNumberInCache());

    Mockito.when(takService.getBehorigheter())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));;

    takCacheLog = behorighetCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.REUSING_EXISTING_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(5, takCacheLog.getNumberInCache());
  }

  private String getLocalCacheResource() {
    return TakCacheTest.class.getClassLoader().getResource("tklocalcache-test.xml").getFile();
  }

  private void mockTakServiceDefaultValues() throws TakServiceException {
    Mockito.when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());
  }
}
