package se.skltp.takcache;

import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;

import java.util.ArrayList;
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
import se.skltp.takcache.behorighet.BehorighetHandler;
import se.skltp.takcache.behorighet.BehorighetPersistentHandler;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.vagval.VagvalHandler;
import se.skltp.takcache.vagval.VagvalPersistentHandler;


@Service
public class TakCacheImpl implements TakCache {

  private final static Logger LOGGER = LogManager.getLogger(TakCacheImpl.class);

  public static final String MSG_EMPTY_VAGVAL_FROM_TAK = "Failed to refresh TAK data. Got empty result of virtualisations from TAK service";
  public static final String MSG_NO_VAGVAL_AFTER_FILTER = "Failed to refresh TAK data. No VirtualiseringsInfo after filter on ";
  public static final String MSG_REFRESH_VIRTUALISATION_EXCEPTION = "Failed to refresh TAK virtualisations. Exception from TakService: ";
  public static final String MSG_EMPTY_BEHORIGHETER_FROM_TAK = "Failed to refresh TAK behorigheter. Got empty result of behorigheter from TAK service";
  public static final String MSG_NO_BEHORIGHETER_AFTER_FILTER = "Failed to refresh TAK behorigheter. No behorigheter after filter on:";
  public static final String MSG_REFRESH_BEHORIGHETER_EXCEPTION = "Failed to refresh TAK behorigheter. Exception from TakService: ";

  TakService takService;

  protected BehorighetHandler behorighetCache;
  protected VagvalHandler vagvalCache;

  @Value("#{new Boolean('${takcache.use.behorighet.cache:true}')}")
  protected boolean useBehorighetCache;

  @Value("#{new Boolean('${takcache.use.vagval.cache:true}')}")
  protected boolean useVagvalCache;

  @Value("${takcache.behorighet.persistent.file.name:#{null}}")
  protected String behorighetFileName;

  @Value("${takcache.vagval.persistent.file.name:#{null}}")
  protected String vagvalFileName;

  @Value("${takcache.tjanstegranssnitt.filter:#{null}}")
  protected String tjanstegranssnittFilter;

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
    takCacheLog.setRefreshSuccessful(true);

    if (useVagvalCache) {
      refreshVagvalCache(takCacheLog);
      if (vagvalCache == null) {
        takCacheLog.setRefreshSuccessful(false);
        vagvalCache = restoreVagvalFromLocalFileCache(takCacheLog);
      }
    }

    if (useBehorighetCache) {
      refreshBehorighetCache(takCacheLog);
      if (behorighetCache == null) {
        takCacheLog.setRefreshSuccessful(false);
        behorighetCache = restoreBehorigheterFromLocalFileCache(takCacheLog);
      }
    }
    return takCacheLog;
  }

  private void filterCache(String tjanstegranssnitt) {
//    VagvalHandler vagvalHandlerTmp = new VagvalHandler();
//    vagvalCache.
  }

  @Override
  public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
    if (behorighetCache == null && useBehorighetCache) {
      refresh();
    }
    return behorighetCache == null ? false
        : behorighetCache.isAuthorized(senderId, tjanstegranssnitt, receiverAddress);
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
  public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress,
      String rivProfile) throws RoutingException {
    if (vagvalCache == null && useVagvalCache) {
      refresh();
    }
    return vagvalCache == null ? null
        : vagvalCache.getRoutingAddress(tjanstegranssnitt, receiverAddress, rivProfile);
  }

  protected void setLocalCacheFileNames(String behorighetFileName, String vagvalFileName) {
    this.behorighetFileName = behorighetFileName;
    this.vagvalFileName = vagvalFileName;
  }

  private BehorighetHandler restoreBehorigheterFromLocalFileCache(TakCacheLog takCacheLog) {
    List<AnropsBehorighetsInfoType> anropsBehorighetsInfoTypes = BehorighetPersistentHandler
        .restoreFromLocalCache(behorighetFileName);
    if (anropsBehorighetsInfoTypes == null) {
      takCacheLog.addLog("Failed restore behorigheter from local cache.");
      takCacheLog.setNumberBehorigheter(0);
      return null;
    }
    LOGGER.warn("Behorigheter restored from local cache");
    takCacheLog.addLog("Restored behorigheter from local cache.");
    takCacheLog.setBehorigheterRefreshStatus(RESTORED_FROM_LOCAL_CACHE);
    takCacheLog.setNumberBehorigheter(anropsBehorighetsInfoTypes.size());
    return new BehorighetHandler(anropsBehorighetsInfoTypes);
  }

  private VagvalHandler restoreVagvalFromLocalFileCache(TakCacheLog takCacheLog) {
    List<VirtualiseringsInfoType> virtualiseringsInfoTypes = VagvalPersistentHandler
        .restoreFromLocalCache(vagvalFileName);
    if (virtualiseringsInfoTypes == null) {
      takCacheLog.addLog("Failed restore vagval from local cache.");
      takCacheLog.setNumberVagval(0);
      return null;
    }
    LOGGER.warn("Virtualisations restored from local cache");
    takCacheLog.addLog("Restored vagval from local cache.");
    takCacheLog.setVagvalRefreshStatus(RESTORED_FROM_LOCAL_CACHE);
    takCacheLog.setNumberVagval(virtualiseringsInfoTypes.size());
    return new VagvalHandler(virtualiseringsInfoTypes);
  }

  private void refreshVagvalCache(TakCacheLog takCacheLog) {
    try {
      List<VirtualiseringsInfoType> virtualiseringar = takService.getVirtualiseringar();
      if (virtualiseringar == null || virtualiseringar.isEmpty()) {
        takCacheLog.addLog(MSG_EMPTY_VAGVAL_FROM_TAK);
        LOGGER.warn(MSG_EMPTY_VAGVAL_FROM_TAK);
        return;
      }

      LOGGER.info("Init number of virtualizations: {}", virtualiseringar.size());
      List<VirtualiseringsInfoType> filteredVirtualiseringar = filterVirtualiseringar(
          virtualiseringar);
      if (filteredVirtualiseringar == null || filteredVirtualiseringar.isEmpty()) {
        takCacheLog.addLog(MSG_NO_VAGVAL_AFTER_FILTER + tjanstegranssnittFilter);
        LOGGER.warn(MSG_NO_VAGVAL_AFTER_FILTER + tjanstegranssnittFilter);
        return;
      }

      LOGGER.info("Number of filtered virtualizations: {}", filteredVirtualiseringar.size());
      vagvalCache = new VagvalHandler(filteredVirtualiseringar);
      takCacheLog.setVagvalRefreshStatus(TakCacheLog.RefreshStatus.REFRESH_OK);
      takCacheLog.setNumberVagval(filteredVirtualiseringar.size());
      VagvalPersistentHandler.saveToLocalCache(vagvalFileName, filteredVirtualiseringar);
    } catch (Exception e) {
      takCacheLog.addLog(MSG_REFRESH_VIRTUALISATION_EXCEPTION + e.getMessage());
      LOGGER.error(MSG_REFRESH_VIRTUALISATION_EXCEPTION, e);
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
    try {
      List<AnropsBehorighetsInfoType> behorigheter = takService.getBehorigheter();
      if (behorigheter == null || behorigheter.isEmpty()) {
        takCacheLog.addLog(MSG_EMPTY_BEHORIGHETER_FROM_TAK);
        LOGGER.warn(MSG_EMPTY_BEHORIGHETER_FROM_TAK);
        return;
      }

      LOGGER.info("Init number of permissions: {}", behorigheter.size());
      List<AnropsBehorighetsInfoType> filteredBehorigheter = filterBehorigheter(behorigheter);
      if (filteredBehorigheter == null || filteredBehorigheter.isEmpty()) {
        takCacheLog.addLog( MSG_NO_BEHORIGHETER_AFTER_FILTER + tjanstegranssnittFilter);
        LOGGER.warn(MSG_NO_BEHORIGHETER_AFTER_FILTER + tjanstegranssnittFilter);
        return;
      }

      LOGGER.info("Number of filtered permissions: {}", filteredBehorigheter.size());
      behorighetCache = new BehorighetHandler(filteredBehorigheter);
      takCacheLog.setBehorigheterRefreshStatus(TakCacheLog.RefreshStatus.REFRESH_OK);
      takCacheLog.setNumberBehorigheter(filteredBehorigheter.size());
      BehorighetPersistentHandler.saveToLocalCache(behorighetFileName, filteredBehorigheter);

    } catch (Exception e) {
      takCacheLog.addLog(MSG_REFRESH_BEHORIGHETER_EXCEPTION + e.getMessage());
      LOGGER.error(MSG_REFRESH_BEHORIGHETER_EXCEPTION, e);
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

  // Methods to increase testability

  public void setUseBehorighetCache(boolean useBehorighetCache) {
    this.useBehorighetCache = useBehorighetCache;
  }

  public void setUseVagvalCache(boolean useVagvalCache) {
    this.useVagvalCache = useVagvalCache;
  }

  public void setBehorighetFileName(String behorighetFileName) {
    this.behorighetFileName = behorighetFileName;
  }

  public void setVagvalFileName(String vagvalFileName) {
    this.vagvalFileName = vagvalFileName;
  }


}
