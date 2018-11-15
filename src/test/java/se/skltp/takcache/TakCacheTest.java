package se.skltp.takcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_OK;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.REUSING_EXISTING_CACHE;
import static se.skltp.takcache.util.TestTakDataDefines.ADDRESS_1;
import static se.skltp.takcache.util.TestTakDataDefines.NAMNRYMD_1;
import static se.skltp.takcache.util.TestTakDataDefines.NAMNRYMD_2;
import static se.skltp.takcache.util.TestTakDataDefines.RECEIVER_1;
import static se.skltp.takcache.util.TestTakDataDefines.RECEIVER_2;
import static se.skltp.takcache.util.TestTakDataDefines.RIV20;
import static se.skltp.takcache.util.TestTakDataDefines.RIV21;
import static se.skltp.takcache.util.TestTakDataDefines.SENDER_1;
import static se.skltp.takcache.util.TestTakDataDefines.SENDER_3;

import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.TakServiceException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring-context.xml")
@TestPropertySource("classpath:test-properties.properties")
public class TakCacheTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Mock
  TakService takService;

  @InjectMocks
  @Autowired
  private TakCacheImpl takCache;

  @BeforeClass
  public static void beforeClass() {

  }

  @Before
  public void beforeTest() {
    MockitoAnnotations.initMocks(this);

    // Reset internal cache between tests
    takCache.behorighetCache = null;
    takCache.vagvalCache = null;
    takCache.tjanstegranssnittFilter = null;
    takCache.setLocalTakCacheFileName(null);
    takCache.setUseBehorighetCache(true);
    takCache.setUseVagvalCache(true);
  }

  @Test
  public void simpleSuccessfulRefreshTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheLog takCacheLog = takCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    assertEquals(5, takCacheLog.getNumberBehorigheter());
    assertEquals(5, takCacheLog.getNumberVagval());
  }

//  @Test
//  public void simpleSuccessfulRefreshTest() throws Exception {
//    mockTakServiceDefaultValues();
//    TakCacheLog takCacheLog = takCache.refresh();
//
//    assertTrue(takCacheLog.isRefreshSuccessful());
//    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
//    assertEquals(5, takCacheLog.getNumberBehorigheter());
//    assertEquals(5, takCacheLog.getNumberVagval());
//  }


  @Test
  public void simpleBehorighetTest() throws Exception {
    mockTakServiceDefaultValues();
    assertTrue(takCache.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
    assertTrue(takCache.isAuthorized(SENDER_1, NAMNRYMD_2, RECEIVER_1));
    assertFalse(takCache.isAuthorized(SENDER_3, NAMNRYMD_1, RECEIVER_1));
  }

  @Test
  public void filterShouldRemoveNotMatchingBehorighetTest() throws Exception {
    mockTakServiceDefaultValues();

    TakCacheLog takCacheLog = takCache.refresh(NAMNRYMD_1);
    assertEquals(4, takCacheLog.getNumberBehorigheter());
    assertTrue(takCache.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
    assertFalse(takCache.isAuthorized(SENDER_1, NAMNRYMD_2, RECEIVER_1));
  }


  @Test
  public void simpleRoutingInfoTest() throws Exception {
    mockTakServiceDefaultValues();
    assertEquals(0, takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(1, takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
    assertEquals(2, takCache.getRoutingInfo(NAMNRYMD_2, RECEIVER_2).size());
  }

  @Test
  public void routingInfoValuesShouldExistTest() throws Exception {
    mockTakServiceDefaultValues();
    List<RoutingInfo> routingInfoList = takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_2);
    assertEquals(1, routingInfoList.size());
    assertEquals(ADDRESS_1, routingInfoList.get(0).getAddress());
    assertEquals(RIV21, routingInfoList.get(0).getRivProfile());
  }

  @Test
  public void simpleRoutingAddressTest() throws Exception {
    mockTakServiceDefaultValues();
    assertEquals(ADDRESS_1, takCache.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
    assertEquals(ADDRESS_1, takCache.getRoutingAddress(NAMNRYMD_2, RECEIVER_2, RIV20));
  }

  @Test
  public void filterShouldRemoveNotMatchingNamespaceInVagvalTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheLog takCacheLog = takCache.refresh(NAMNRYMD_1);
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    assertEquals(3, takCacheLog.getNumberVagval());
    assertEquals(0, takCache.getRoutingInfo(NAMNRYMD_2, RECEIVER_2).size());
  }

  @Test
  public void filterShouldNotEffectRemainingVagvalTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheLog takCacheLog = takCache.refresh(NAMNRYMD_1);

    assertEquals(0, takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(1, takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
    assertEquals(ADDRESS_1, takCache.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
  }

  @Test
  public void noVagvalFromTakShouldGiveEmptyLisOfRoutingInfosTest() throws Exception {
    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(Collections.<VirtualiseringsInfoType>emptyList());
    TakCacheLog takCacheLog = takCache.refresh();
    assertEquals(0, takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(0, takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
  }

  @Test
  public void noBehorighetFromTakShouldGiveBehorighetFalseTest() throws Exception {
    Mockito.when(takService.getBehorigheter())
        .thenReturn(Collections.<AnropsBehorighetsInfoType>emptyList());
    TakCacheLog takCacheLog = takCache.refresh();
    assertFalse(takCache.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
  }

  @Test
  public void noContactWithTjanstekatalogenAlwaysReultsInLocalCacheIsRead()
      throws Exception {

    takCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("Failed get virtualizations from TAK")));
    Mockito.when(takService.getBehorigheter())
        .thenThrow(new TakServiceException(new Exception("Failed get behorigheter from TAK")));

    TakCacheLog takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(1, takCacheLog.getNumberVagval());

  }

  @Test
  public void emptyAnswerFromTjanstekatalogenAlwaysReultsInLocalCacheIsRead()
      throws Exception {


    takCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getVirtualiseringar()).thenReturn(new ArrayList<>());
    Mockito.when(takService.getBehorigheter()).thenReturn(new ArrayList<>());

    TakCacheLog takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(1, takCacheLog.getNumberVagval());

  }

  @Test
  public void emptyBehorigheterFromTjanstekatalogenAlwaysReultsInLocalCacheIsRead()
      throws Exception {


    takCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getVirtualiseringar()).thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
    Mockito.when(takService.getBehorigheter()).thenReturn(new ArrayList<>());

    TakCacheLog takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(1, takCacheLog.getNumberVagval());

  }


  @Test
  public void noDataAfterFilterShouldReultsInLocalCacheIsRead()
      throws Exception {

     takCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
    Mockito.when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());

    TakCacheLog takCacheLog = takCache.refresh("NamespaceNoMatch");

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(1, takCacheLog.getNumberVagval());

  }

  @Test
  public void contactWithTjanstekatalogenAlwaysReultsInLocalCacheIsUpdated()
      throws Exception {

    mockTakServiceDefaultValues();

    String cachefileName = String
        .format("%s/localcache-test.xml", testFolder.getRoot().getPath());
    takCache.setLocalTakCacheFileName(cachefileName);

    TakCacheLog takCacheLog = takCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    XMLAssert.assertXpathExists("/persistentCache/virtualiseringsInfo",
        new InputSource(new FileReader(cachefileName)));
    XMLAssert.assertXpathExists("/persistentCache/anropsBehorighetsInfo",
        new InputSource(new FileReader(cachefileName)));
  }

  @Test
  public void ifUseVagValIsFalseGetVirtualiseringarShouldNotBeCalled() throws Exception {
    Mockito.when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));
    Mockito.when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());
    takCache.setUseVagvalCache(false);
    TakCacheLog takCacheLog = takCache.refresh();

    Mockito.verify(takService, times(0)).getVirtualiseringar();
    Mockito.verify(takService, times(1)).getBehorigheter();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(5, takCacheLog.getNumberBehorigheter());

  }

  @Test
  public void ifUseBehorighetIsFalseGetBehorigheterShouldNotBeCalled() throws Exception {
    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
    Mockito.when(takService.getBehorigheter())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));
    takCache.setUseBehorighetCache(false);
    TakCacheLog takCacheLog = takCache.refresh();

    Mockito.verify(takService, times(1)).getVirtualiseringar();
    Mockito.verify(takService, times(0)).getBehorigheter();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(5, takCacheLog.getNumberVagval());

  }

  @Test
  public void restoreFromCacheShouldWorkWithOnlyUseVagval()
      throws Exception {

    Mockito.when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));

    takCache.setLocalTakCacheFileName(getLocalCacheResource());
    takCache.setUseVagvalCache(true);
    takCache.setUseBehorighetCache(false);

    TakCacheLog takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberVagval());
    assertEquals(0, takCacheLog.getNumberBehorigheter());

  }

  @Test
  public void restoreFromCacheShouldWorkWithOnlyUseBehorighet()
      throws Exception {

    Mockito.when(takService.getBehorigheter())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));

    takCache.setLocalTakCacheFileName(getLocalCacheResource());
    takCache.setUseVagvalCache(false);
    takCache.setUseBehorighetCache(true);

    TakCacheLog takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(0, takCacheLog.getNumberVagval());

  }

  @Test
  public void saveToCacheShouldWorkWithOnlyUseVagval()
      throws Exception {

    mockTakServiceDefaultValues();

    String cachefileName = String
        .format("%s/localcache-test.xml", testFolder.getRoot().getPath());
    takCache.setLocalTakCacheFileName(cachefileName);
    takCache.setUseVagvalCache(true);
    takCache.setUseBehorighetCache(false);

    TakCacheLog takCacheLog = takCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    XMLAssert.assertXpathExists("/persistentCache/virtualiseringsInfo",
        new InputSource(new FileReader(cachefileName)));

  }


  @Test
  public void saveToCacheShouldWorkWithOnlyUseBehorighet()
      throws Exception {

    mockTakServiceDefaultValues();

    String cachefileName = String
        .format("%s/localcache-test.xml", testFolder.getRoot().getPath());
    takCache.setLocalTakCacheFileName(cachefileName);
    takCache.setUseVagvalCache(false);
    takCache.setUseBehorighetCache(true);

    TakCacheLog takCacheLog = takCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    XMLAssert.assertXpathExists("/persistentCache/anropsBehorighetsInfo",
        new InputSource(new FileReader(cachefileName)));

  }

  @Test
  public void ifExistingCacheExistItShopuldBeUsedIfRefreshFailes()
      throws Exception {

    mockTakServiceDefaultValues();

    TakCacheLog takCacheLog = takCache.refresh();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals( REFRESH_OK, takCacheLog.getRefreshStatus() );
    assertEquals( 5, takCacheLog.getNumberBehorigheter());
    assertEquals( 5, takCacheLog.getNumberVagval());


    Mockito.when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("Should not happen")));;

    takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(REUSING_EXISTING_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(5, takCacheLog.getNumberVagval());
    assertEquals(5, takCacheLog.getNumberBehorigheter());
  }

  private String getLocalCacheResource() {
    return TakCacheTest.class.getClassLoader().getResource("tklocalcache-test.xml").getFile();
  }

  private void mockTakServiceDefaultValues() throws TakServiceException {
    Mockito.when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());
    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
  }
}