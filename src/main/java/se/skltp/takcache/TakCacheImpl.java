package se.skltp.takcache;

import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.TakCacheLog.RefreshStatus;
import se.skltp.takcache.TakCachePersistentHandler.PersistentCache;
import se.skltp.takcache.behorighet.BehorighetHandler;
import se.skltp.takcache.exceptions.PersistentCacheException;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.vagval.VagvalHandler;


@Service
public class TakCacheImpl implements TakCache {

  private static final Logger LOGGER = LogManager.getLogger(TakCacheImpl.class);

  public static final String MSG_INITIALIZE_TAK_CACHE = "Initialize TAK cache resources...";
  public static final String MSG_SAVE_TO_LOCAL_CACHE_FILE = "Succeeded to get virtualizations and/or permissions from TAK, save to local TAK copy...";
  public static final String MSG_SAVED_TO_LOCAL_CACHE_FILE = "Succesfully saved virtualizations and permissions to local TAK copy: ";
  public static final String MSG_SAVE_TO_LOCAL_CACHE_FAILED = "Failed to save virtualizations and permissions to local TAK copy: ";
  public static final String MSG_REASON_FOR_FAILURE = "Reason for failure: ";
  public static final String MSG_FAILED_USE_EXISTING_CACHE = "Failed to get virtualizations and/or permissions from TAK, see logfiles for details. Will continue to use already loaded TAK data.";
  public static final String MSG_FAILED_RESTORE_FROM_FILE ="Failed to get virtualizations and/or permissions from TAK, see logfiles for details. Restore from local TAK copy...";
  public static final String SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY = "Succesfully restored virtualizations and permissions from local TAK copy: ";
  public static final String MSG_RESTORE_FROM_FILE_FAILED = "Failed to restore virtualizations and permissions from local TAK copy: ";


  TakService takService;

  protected BehorighetHandler behorighetCache;
  protected VagvalHandler vagvalCache;

  @Value("#{new Boolean('${takcache.use.behorighet.cache:true}')}")
  protected boolean useBehorighetCache;

  @Value("#{new Boolean('${takcache.use.vagval.cache:true}')}")
  protected boolean useVagvalCache;

  @Value("${takcache.persistent.file.name:#{null}}")
  protected String localTakCacheFileName;

  @Value("${takcache.tjanstegranssnitt.filter:#{null}}")
  protected String tjanstegranssnittFilter;

  private Calendar calendar = Calendar.getInstance();

  @Autowired
  public TakCacheImpl(TakService takService) {
    this.takService = takService;
  }

  @Override
  public TakCacheLog refresh(String tjanstegranssnittFilter) {
    this.tjanstegranssnittFilter = tjanstegranssnittFilter;
    return refresh();
  }


  @Override
  public TakCacheLog refresh() {
    TakCacheLog takCacheLog = new TakCacheLog();
    logStartInitializeTakCache(takCacheLog);
    takCacheLog.setRefreshSuccessful(true);

    if (useVagvalCache) {
      refreshVagvalCache(takCacheLog);
    }

    if (useBehorighetCache && takCacheLog.isRefreshSuccessful()) {
      refreshBehorighetCache(takCacheLog);
    }

    if (takCacheLog.isRefreshSuccessful()) {
      takCacheLog.setRefreshStatus(RefreshStatus.REFRESH_OK);
      saveToLocalFileCache(takCacheLog);
    } else if (isExistingCacheAvailable()) {
      takCacheLog.addLog(MSG_FAILED_USE_EXISTING_CACHE);
      takCacheLog.setRefreshStatus(RefreshStatus.REUSING_EXISTING_CACHE);
    } else {
      takCacheLog.addLog(MSG_FAILED_RESTORE_FROM_FILE);
      restoreFromLocalFileCache(takCacheLog);
    }


    logEndInitializeTakCache(takCacheLog);
    return takCacheLog;
  }

 @Override
  public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
    if (behorighetCache == null && useBehorighetCache) {
      refresh();
    }
    return behorighetCache != null && behorighetCache.isAuthorized(senderId, tjanstegranssnitt, receiverAddress);
  }

  @Override
  public List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress) {
    if (vagvalCache == null && useVagvalCache) {
      refresh();
    }
    if (vagvalCache == null) {
      return Collections.<RoutingInfo>emptyList();
    }
    return toRoutingInfo(vagvalCache.geVirtualiseringar(tjanstegranssnitt, receiverAddress));
  }

  private List<RoutingInfo> toRoutingInfo(List<VirtualiseringsInfoType> virtualiseringsInfoTypes) {
    List<RoutingInfo> routingInfoList = new ArrayList<>();
    for (VirtualiseringsInfoType virtualiseringsInfoType : virtualiseringsInfoTypes) {
      RoutingInfo routingInfo = new RoutingInfo();
      routingInfo.setAddress(virtualiseringsInfoType.getAdress());
      routingInfo.setRivProfile(virtualiseringsInfoType.getRivProfil());
      routingInfoList.add(routingInfo);
    }
    return routingInfoList;
  }

  @Override
  @Deprecated
  public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos() {
    return behorighetCache == null ? Collections.<AnropsBehorighetsInfoType>emptyList() : behorighetCache.getAnropsBehorighetsInfos();
  }

  @Override
  public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress,
      String rivProfile) throws RoutingException {
    if (vagvalCache == null && useVagvalCache) {
      refresh();
    }
    return vagvalCache == null ? null : vagvalCache.getRoutingAddress(tjanstegranssnitt, receiverAddress, rivProfile);
  }

  private void saveToLocalFileCache(TakCacheLog takCacheLog) {
    try {
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FILE);
      List<VirtualiseringsInfoType> virtualiseringsInfoTypes = useVagvalCache ? vagvalCache.getVirtualiseringsInfos() : null;
      List<AnropsBehorighetsInfoType> anropsBehorighetsInfoTypes = useBehorighetCache ? behorighetCache.getAnropsBehorighetsInfos() : null;
      TakCachePersistentHandler.saveToLocalCache(localTakCacheFileName, virtualiseringsInfoTypes, anropsBehorighetsInfoTypes);
      takCacheLog.addLog(MSG_SAVED_TO_LOCAL_CACHE_FILE + localTakCacheFileName);
    } catch (PersistentCacheException e) {
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FAILED + localTakCacheFileName);
      takCacheLog.addLog( MSG_REASON_FOR_FAILURE + e.getMessage());
    }
  }

  private void restoreFromLocalFileCache(TakCacheLog takCacheLog) {

    try {
      PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(localTakCacheFileName);
      if (useVagvalCache) {
        vagvalCache = new VagvalHandler(persistentCache.virtualiseringsInfo);
      }
      if (useBehorighetCache) {
        behorighetCache = new BehorighetHandler(persistentCache.anropsBehorighetsInfo);
      }

      LOGGER.warn("Restored from local cache file: "+ localTakCacheFileName );
      takCacheLog.addLog(SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY + localTakCacheFileName);
      takCacheLog.setRefreshStatus(RESTORED_FROM_LOCAL_CACHE);

    } catch (PersistentCacheException e) {
      takCacheLog.addLog(MSG_RESTORE_FROM_FILE_FAILED +localTakCacheFileName);
      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());
    }

  }

  private void refreshVagvalCache(TakCacheLog takCacheLog) {
    takCacheLog.setRefreshSuccessful(false);
    try {
      List<VirtualiseringsInfoType> virtualiseringar = takService.getVirtualiseringar();
      if (virtualiseringar == null || virtualiseringar.isEmpty()) {
        LOGGER.warn("Got empty result of virtualisations from TAK service");
        return;
      }

      LOGGER.info("Init number of virtualizations: {}", virtualiseringar.size());
      List<VirtualiseringsInfoType> filteredVirtualiseringar = filterVirtualiseringar(virtualiseringar);
      if (filteredVirtualiseringar == null || filteredVirtualiseringar.isEmpty()) {
        LOGGER.error( "No VirtualiseringsInfo after filtering on: "  + tjanstegranssnittFilter);
        return;
      }

      LOGGER.info("Number of filtered virtualizations: {}", filteredVirtualiseringar.size());
      takCacheLog.setRefreshStatus(TakCacheLog.RefreshStatus.REFRESH_OK);
      vagvalCache = new VagvalHandler(filteredVirtualiseringar);
      takCacheLog.setRefreshSuccessful(true);
    } catch (Exception e) {
      LOGGER.error( "Exception from TakService when getting virtualizations:",  e);
    }
  }

  private List<VirtualiseringsInfoType> filterVirtualiseringar(
      List<VirtualiseringsInfoType> virtualiseringar) {
    if (tjanstegranssnittFilter != null && !tjanstegranssnittFilter.isEmpty()) {
      return virtualiseringar
          .stream()
          .filter(virt -> tjanstegranssnittFilter.equals(virt.getTjansteKontrakt()))
          .collect(Collectors.toList());
    }
    return virtualiseringar;
  }

  private void refreshBehorighetCache(TakCacheLog takCacheLog) {
    takCacheLog.setRefreshSuccessful(false);
    try {
      List<AnropsBehorighetsInfoType> behorigheter = takService.getBehorigheter();
      if (behorigheter == null || behorigheter.isEmpty()) {
        LOGGER.warn(" Got empty result of behorigheter from TAK service");
        return;
      }

      LOGGER.info("Init number of permissions: {}", behorigheter.size());
      List<AnropsBehorighetsInfoType> filteredBehorigheter = filterBehorigheter(behorigheter);
      if (filteredBehorigheter == null || filteredBehorigheter.isEmpty()) {
        LOGGER.warn("No behorigheter after filtering on " + tjanstegranssnittFilter);
        return;
      }

      LOGGER.info("Number of filtered permissions: {}", filteredBehorigheter.size());
      behorighetCache = new BehorighetHandler(filteredBehorigheter);
      takCacheLog.setRefreshSuccessful(true);
    } catch (Exception e) {
      LOGGER.error( "Exception from TakService when getting behorigheter:", e);
    }
  }


  private List<AnropsBehorighetsInfoType> filterBehorigheter(
      List<AnropsBehorighetsInfoType> behorigheter) {
    if (tjanstegranssnittFilter != null && !tjanstegranssnittFilter.isEmpty()) {
      return behorigheter
          .stream()
          .filter(behorighet -> tjanstegranssnittFilter.equals(behorighet.getTjansteKontrakt()))
          .collect(Collectors.toList());
    }
    return behorigheter;
  }


  private void logStartInitializeTakCache(TakCacheLog takCacheLog){
    takCacheLog.addLog("Host: "+getHostName());
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    takCacheLog.addLog("Time: " + df.format(calendar.getTime()));
    takCacheLog.addLog(MSG_INITIALIZE_TAK_CACHE);
  }

  private void logEndInitializeTakCache(TakCacheLog takCacheLog) {
    takCacheLog.setNumberVagval(vagvalCache == null ? 0 : vagvalCache.count());
    takCacheLog.setNumberBehorigheter(behorighetCache == null ? 0 : behorighetCache.count());

    takCacheLog.addLog("Init TAK cache loaded number of permissions: " + takCacheLog.getNumberBehorigheter());
    takCacheLog.addLog("Init TAK cache loaded number of virtualizations: " + takCacheLog.getNumberVagval());
    takCacheLog.addLog("Init done, was successful: " + takCacheLog.isRefreshSuccessful());
  }

  private String getHostName() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      return "UNKNOWN";
    }
  }

  private boolean isExistingCacheAvailable() {
    return !(useVagvalCache && vagvalCache == null || useBehorighetCache && behorighetCache == null);
  }



  // Methods to increase testability

  public void setUseBehorighetCache(boolean useBehorighetCache) {
    this.useBehorighetCache = useBehorighetCache;
  }

  public void setUseVagvalCache(boolean useVagvalCache) {
    this.useVagvalCache = useVagvalCache;
  }

  public void setLocalTakCacheFileName(String fileName) {
    this.localTakCacheFileName = fileName;
  }

}
