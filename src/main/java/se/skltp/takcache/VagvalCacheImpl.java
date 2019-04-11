package se.skltp.takcache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.TakCachePersistentHandler.PersistentCache;
import se.skltp.takcache.exceptions.PersistentCacheException;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.vagval.VagvalHandler;

@Service
public class VagvalCacheImpl implements VagvalCache {

  private static final Logger LOGGER = LogManager.getLogger(TakCacheImpl.class);

  TakService takService;

  protected VagvalHandler vagvalCache;

  @Value("${takcache.persistent.file.name:#{null}}")
  protected String localTakCacheFileName;

  @Value("${takcache.tjanstegranssnitt.filter:#{null}}")
  protected String tjanstegranssnittFilter;

  @Autowired
  public VagvalCacheImpl(TakService takService) {
    this.takService = takService;
  }

  @Override
  public TakCacheStatus refresh(String tjanstegranssnittFilter) {
    this.tjanstegranssnittFilter = tjanstegranssnittFilter;
    return refresh();
  }

  @Override
  public TakCacheStatus refresh() {
    TakCacheStatus takCacheStatus = new TakCacheStatus();
    takCacheStatus.setRefreshSuccessful(true);
    takCacheStatus.setLocalFileName(localTakCacheFileName);
    takCacheStatus.setTakAddress(takService.getEndPointAddress());

    refreshVagvalCache(takCacheStatus);

    if (takCacheStatus.isRefreshSuccessful()) {
      takCacheStatus.setRefreshStatus(RefreshStatus.REFRESH_OK);
      saveToLocalFileCache(takCacheStatus);
    } else if (vagvalCache != null) {
      takCacheStatus.setRefreshStatus(RefreshStatus.REUSING_EXISTING_CACHE);
    } else {
      restoreFromLocalFileCache(takCacheStatus);
    }

    takCacheStatus.setNumberInCache(vagvalCache==null ? 0 : vagvalCache.count());
    return takCacheStatus;
  }

  @Override
  public List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress) {
    if (vagvalCache == null) {
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
  public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress, String rivProfile)
      throws RoutingException {
    if (vagvalCache == null) {
      refresh();
    }
    return vagvalCache == null ? null : vagvalCache.getRoutingAddress(tjanstegranssnitt, receiverAddress, rivProfile);
  }

  private void saveToLocalFileCache(TakCacheStatus takCacheStatus) {
    try {
      List<VirtualiseringsInfoType> virtualiseringsInfoTypes = vagvalCache.getVirtualiseringsInfos();
      TakCachePersistentHandler.saveVagval(localTakCacheFileName, virtualiseringsInfoTypes);
    } catch (PersistentCacheException e) {
      LOGGER.error("Failed save to local cache: " + localTakCacheFileName, e);
    }
  }

  private void restoreFromLocalFileCache(TakCacheStatus takCacheStatus) {

    try {
      PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(localTakCacheFileName);
      vagvalCache = new VagvalHandler(persistentCache.virtualiseringsInfo);

      LOGGER.warn("Restored from local cache file: " + localTakCacheFileName);
      takCacheStatus.setRefreshStatus(RefreshStatus.RESTORED_FROM_LOCAL_CACHE);

    } catch (PersistentCacheException e) {
      LOGGER.error("Failed restore from local cache: " + localTakCacheFileName, e);
    }

  }

  private void refreshVagvalCache(TakCacheStatus takCacheStatus) {
    takCacheStatus.setRefreshSuccessful(false);
    try {
      List<VirtualiseringsInfoType> virtualiseringar = takService.getVirtualiseringar();
      if (virtualiseringar == null || virtualiseringar.isEmpty()) {
        LOGGER.warn("Got empty result of virtualisations from TAK service");
        return;
      }

      LOGGER.info("Init number of virtualizations: {}", virtualiseringar.size());
      List<VirtualiseringsInfoType> filteredVirtualiseringar = filterVirtualiseringar(virtualiseringar);
      if (filteredVirtualiseringar == null || filteredVirtualiseringar.isEmpty()) {
        LOGGER.error("No VirtualiseringsInfo after filtering on: " + tjanstegranssnittFilter);
        return;
      }

      LOGGER.info("Number of filtered virtualizations: {}", filteredVirtualiseringar.size());
      takCacheStatus.setRefreshStatus(RefreshStatus.REFRESH_OK);
      vagvalCache = new VagvalHandler(filteredVirtualiseringar);
      takCacheStatus.setRefreshSuccessful(true);
    } catch (Exception e) {
      LOGGER.error("Exception from TakService when getting virtualizations:", e);
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

  public void setLocalTakCacheFileName(String fileName) {
    this.localTakCacheFileName = fileName;
  }
}
