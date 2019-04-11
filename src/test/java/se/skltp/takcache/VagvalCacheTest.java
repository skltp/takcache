package se.skltp.takcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static se.skltp.takcache.util.TestTakDataDefines.ADDRESS_1;
import static se.skltp.takcache.util.TestTakDataDefines.NAMNRYMD_1;
import static se.skltp.takcache.util.TestTakDataDefines.NAMNRYMD_2;
import static se.skltp.takcache.util.TestTakDataDefines.RECEIVER_1;
import static se.skltp.takcache.util.TestTakDataDefines.RECEIVER_2;
import static se.skltp.takcache.util.TestTakDataDefines.RIV20;
import static se.skltp.takcache.util.TestTakDataDefines.RIV21;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.TakServiceException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;

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
    vagvalCache.vagvalCache = null;
    vagvalCache.tjanstegranssnittFilter = null;
    vagvalCache.setLocalTakCacheFileName(null);
  }

  @Test
  public void simpleSuccessfulRefreshTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheStatus takCacheLog = vagvalCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus());
    assertEquals(5, takCacheLog.getNumberInCache());
  }



  @Test
  public void simpleRoutingInfoTest() throws Exception {
    mockTakServiceDefaultValues();
    assertEquals(0, vagvalCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(1, vagvalCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
    assertEquals(2, vagvalCache.getRoutingInfo(NAMNRYMD_2, RECEIVER_2).size());
  }

  @Test
  public void routingInfoValuesShouldExistTest() throws Exception {
    mockTakServiceDefaultValues();
    List<RoutingInfo> routingInfoList = vagvalCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_2);
    assertEquals(1, routingInfoList.size());
    assertEquals(ADDRESS_1, routingInfoList.get(0).getAddress());
    assertEquals(RIV21, routingInfoList.get(0).getRivProfile());
  }

  @Test
  public void simpleRoutingAddressTest() throws Exception {
    mockTakServiceDefaultValues();
    assertEquals(ADDRESS_1, vagvalCache.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
    assertEquals(ADDRESS_1, vagvalCache.getRoutingAddress(NAMNRYMD_2, RECEIVER_2, RIV20));
  }

  @Test
  public void filterShouldRemoveNotMatchingNamespaceInVagvalTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheStatus takCacheLog = vagvalCache.refresh(NAMNRYMD_1);
    assertEquals(RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus());
    assertEquals(3, takCacheLog.getNumberInCache());
    assertEquals(0, vagvalCache.getRoutingInfo(NAMNRYMD_2, RECEIVER_2).size());
  }

  @Test
  public void filterShouldNotEffectRemainingVagvalTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheStatus takCacheLog = vagvalCache.refresh(NAMNRYMD_1);

    assertEquals(0, vagvalCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(1, vagvalCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
    assertEquals(ADDRESS_1, vagvalCache.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
  }

  @Test
  public void noVagvalFromTakShouldGiveEmptyLisOfRoutingInfosTest() throws Exception {
    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(Collections.<VirtualiseringsInfoType>emptyList());
    TakCacheStatus takCacheLog = vagvalCache.refresh();
    assertEquals(0, vagvalCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(0, vagvalCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
  }

  @Test
  public void noContactWithTjanstekatalogenAlwaysReultsInLocalCacheIsRead()
      throws Exception {

    vagvalCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("Failed get virtualizations from TAK")));

    TakCacheStatus takCacheLog = vagvalCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberInCache());

  }

  @Test
  public void emptyAnswerFromTjanstekatalogenAlwaysReultsInLocalCacheIsRead()
      throws Exception {

    vagvalCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getVirtualiseringar()).thenReturn(new ArrayList<>());

    TakCacheStatus takCacheLog = vagvalCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberInCache());

  }


  @Test
  public void noDataAfterFilterShouldReultsInLocalCacheIsRead()
      throws Exception {

    vagvalCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());

    TakCacheStatus takCacheLog = vagvalCache.refresh("NamespaceNoMatch");

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberInCache());

  }

  @Test
  public void contactWithTjanstekatalogenAlwaysReultsInLocalCacheIsUpdated()
      throws Exception {

    mockTakServiceDefaultValues();

    String cachefileName = String.format("%s/localcache-test.xml", testFolder.getRoot().getPath());
    vagvalCache.setLocalTakCacheFileName(cachefileName);

    TakCacheStatus takCacheLog = vagvalCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus());
    XMLAssert.assertXpathExists("/persistentCache/virtualiseringsInfo",
        new InputSource(new FileReader(cachefileName)));
  }

  @Test
  public void ifExistingCacheExistItShouldBeUsedIfRefreshFailes()
      throws Exception {

    mockTakServiceDefaultValues();

    TakCacheStatus takCacheLog = vagvalCache.refresh();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals( RefreshStatus.REFRESH_OK, takCacheLog.getRefreshStatus() );
    assertEquals( 5, takCacheLog.getNumberInCache());


    Mockito.when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));;

    takCacheLog = vagvalCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RefreshStatus.REUSING_EXISTING_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(5, takCacheLog.getNumberInCache());
  }

  private String getLocalCacheResource() {
    return TakCacheTest.class.getClassLoader().getResource("tklocalcache-test.xml").getFile();
  }

  private void mockTakServiceDefaultValues() throws TakServiceException {
    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
  }

}
