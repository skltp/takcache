package se.skltp.takcache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import java.nio.file.Path;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.xmlunit.assertj.XmlAssert;
import se.skltp.takcache.exceptions.TakServiceException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;

@SpringJUnitConfig(locations = "classpath*:spring-context.xml")
@ExtendWith(MockitoExtension.class)
class TakCacheTest {

  @TempDir
  Path testFolder;

  @Mock
  TakService takService;

  @InjectMocks
  private TakCacheImpl takCache;

  @InjectMocks
  VagvalCacheImpl vagvalCache;

  @InjectMocks
  BehorigheterCacheImpl behorighetCache;

  @BeforeEach
  void beforeTest() {

    // Reset internal cache between tests
    takCache.resetCache();
    takCache.setLocalTakCacheFileName(null);
  }

  @Test
  void simpleSuccessfulRefreshTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheLog takCacheLog = takCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    assertEquals(5, takCacheLog.getNumberBehorigheter());
    assertEquals(5, takCacheLog.getNumberVagval());
  }

  @Test
  void simpleBehorighetTest() throws Exception {
    mockTakServiceDefaultValues();
    assertTrue(takCache.getBehorigeterCache().isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
    assertTrue(takCache.getBehorigeterCache().isAuthorized(SENDER_1, NAMNRYMD_2, RECEIVER_1));
    assertFalse(takCache.getBehorigeterCache().isAuthorized(SENDER_3, NAMNRYMD_1, RECEIVER_1));
  }

  @Test
  void filterShouldRemoveNotMatchingBehorighetTest() throws Exception {
    mockTakServiceDefaultValues();

    TakCacheLog takCacheLog = takCache.refresh(List.of(NAMNRYMD_1));
    assertEquals(4, takCacheLog.getNumberBehorigheter());
    assertTrue(takCache.getBehorigeterCache().isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
    assertFalse(takCache.getBehorigeterCache().isAuthorized(SENDER_1, NAMNRYMD_2, RECEIVER_1));
  }


  @Test
  void simpleRoutingInfoTest() throws Exception {
    mockTakServiceDefaultValues();
    assertEquals(0, takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(1, takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
    assertEquals(2, takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_2, RECEIVER_2).size());
  }

  @Test
  void routingInfoValuesShouldExistTest() throws Exception {
    mockTakServiceDefaultValues();
    List<RoutingInfo> routingInfoList = takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_1, RECEIVER_2);
    assertEquals(1, routingInfoList.size());
    assertEquals(ADDRESS_1, routingInfoList.get(0).getAddress());
    assertEquals(RIV21, routingInfoList.get(0).getRivProfile());
  }

  @Test
  void simpleRoutingAddressTest() throws Exception {
    mockTakServiceDefaultValues();
    assertEquals(ADDRESS_1, takCache.getVagvalCache().getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
    assertEquals(ADDRESS_1, takCache.getVagvalCache().getRoutingAddress(NAMNRYMD_2, RECEIVER_2, RIV20));
  }

  @Test
  void filterShouldRemoveNotMatchingNamespaceInVagvalTest() throws Exception {
    mockTakServiceDefaultValues();
    TakCacheLog takCacheLog = takCache.refresh(List.of(NAMNRYMD_1));
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    assertEquals(3, takCacheLog.getNumberVagval());
    assertEquals(0, takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_2, RECEIVER_2).size());
  }

  @Test
  void filterShouldNotEffectRemainingVagvalTest() throws Exception {
    mockTakServiceDefaultValues();

    takCache.refresh(List.of(NAMNRYMD_1));

    assertEquals(0, takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(1, takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
    assertEquals(ADDRESS_1, takCache.getVagvalCache().getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
  }

  @Test
  void noVagvalFromTakShouldGiveEmptyLisOfRoutingInfosTest() throws Exception {
    Mockito.when(takService.getVirtualiseringar())
        .thenReturn(Collections.emptyList());
    takCache.refresh();
    assertEquals(0, takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_1, RECEIVER_1).size());
    assertEquals(0, takCache.getVagvalCache().getRoutingInfo(NAMNRYMD_1, RECEIVER_2).size());
  }

  @Test
  void noBehorighetFromTakShouldGiveBehorighetFalseTest() throws Exception {
    Mockito.when(takService.getBehorigheter())
        .thenReturn(Collections.emptyList());
    takCache.refresh();
    assertFalse(takCache.getBehorigeterCache().isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
  }

  @Test
  void noContactWithTjanstekatalogenAlwaysResultsInLocalCacheIsRead()
      throws Exception {

    takCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.lenient().when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("TEST: Mock-exception on call to takService.getVirtualiseringar() (for cache fallback)")));
    Mockito.lenient().when(takService.getBehorigheter())
        .thenThrow(new TakServiceException(new Exception("TEST: Mock-exception on call to takService.getBehorigheter() (for cache fallback)")));

    TakCacheLog takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(1, takCacheLog.getNumberVagval());

  }

  @Test
  void emptyAnswerFromTjanstekatalogenAlwaysResultsInLocalCacheIsRead()
      throws Exception {

    takCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.lenient().when(takService.getVirtualiseringar()).thenReturn(new ArrayList<>());
    Mockito.lenient().when(takService.getBehorigheter()).thenReturn(new ArrayList<>());

    TakCacheLog takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(1, takCacheLog.getNumberVagval());

  }

  @Test
  void emptyBehorigheterFromTjanstekatalogenAlwaysResultsInLocalCacheIsRead()
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
  void noDataAfterFilterShouldResultsInLocalCacheIsRead()
      throws Exception {

    takCache.setLocalTakCacheFileName(getLocalCacheResource());

    Mockito.lenient().when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
    Mockito.lenient().when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());

    TakCacheLog takCacheLog = takCache.refresh(List.of("NamespaceNoMatch"));

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(RESTORED_FROM_LOCAL_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(1, takCacheLog.getNumberBehorigheter());
    assertEquals(1, takCacheLog.getNumberVagval());

  }

  @Test
  void contactWithTjanstekatalogenAlwaysResultsInLocalCacheIsUpdated()
      throws Exception {

    mockTakServiceDefaultValues();

    String cachefileName = testFolder.resolve("localcache-test.xml").toString();
    takCache.setLocalTakCacheFileName(cachefileName);

    TakCacheLog takCacheLog = takCache.refresh();

    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    String xmlContent = java.nio.file.Files.readString(testFolder.resolve("localcache-test.xml"));
    XmlAssert xmlAssert = XmlAssert.assertThat(xmlContent);
    xmlAssert.hasXPath("/persistentCache/virtualiseringsInfo");
    xmlAssert.hasXPath("/persistentCache/anropsBehorighetsInfo");
  }

  @Test
  void LocalCacheIsUpdated()
      throws Exception {

    mockTakServiceDefaultValues();

    String cachefileName = testFolder.resolve("localcache-test-2.xml").toString();
    vagvalCache.setLocalTakCacheFileName(cachefileName);
    behorighetCache.setLocalTakCacheFileName(cachefileName);

    TakCacheLog takCacheLog = vagvalCache.refresh();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());

    takCacheLog = behorighetCache.refresh();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());

    String xmlContent = java.nio.file.Files.readString(testFolder.resolve("localcache-test-2.xml"));
    XmlAssert xmlAssert = XmlAssert.assertThat(xmlContent);
    xmlAssert.hasXPath("/persistentCache/anropsBehorighetsInfo");
    xmlAssert.hasXPath("/persistentCache/virtualiseringsInfo");
  }

  @Test
  void ifExistingCacheExistItShouldBeUsedIfRefreshFails()
      throws Exception {

    mockTakServiceDefaultValues();

    TakCacheLog takCacheLog = takCache.refresh();
    assertTrue(takCacheLog.isRefreshSuccessful());
    assertEquals(REFRESH_OK, takCacheLog.getRefreshStatus());
    assertEquals(5, takCacheLog.getNumberBehorigheter());
    assertEquals(5, takCacheLog.getNumberVagval());

    Mockito.when(takService.getVirtualiseringar())
        .thenThrow(new TakServiceException(new Exception("TEST: Mock-exception on call to takService.getVirtualiseringar() (for cache fallback)")));

    takCacheLog = takCache.refresh();

    assertFalse(takCacheLog.isRefreshSuccessful());
    assertEquals(REUSING_EXISTING_CACHE, takCacheLog.getRefreshStatus());
    assertEquals(5, takCacheLog.getNumberVagval());
    assertEquals(5, takCacheLog.getNumberBehorigheter());
  }

  private String getLocalCacheResource() {
    return Objects.requireNonNull(TakCacheTest.class.getClassLoader().getResource("tklocalcache-test.xml")).getFile();
  }

  private void mockTakServiceDefaultValues() throws TakServiceException {
    Mockito.lenient().when(takService.getBehorigheter())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticBehorighetList());
    Mockito.lenient().when(takService.getVirtualiseringar())
        .thenReturn(VagvalSchemasTestListsUtil.getStaticVagvalList());
  }
}