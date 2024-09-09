package se.skltp.takcache;

import static se.skltp.takcache.TakCacheLog.MSG_FAILED_RESTORE_FROM_FILE;
import static se.skltp.takcache.TakCacheLog.MSG_FAILED_USE_EXISTING_CACHE;
import static se.skltp.takcache.TakCacheLog.MSG_REASON_FOR_FAILURE;
import static se.skltp.takcache.TakCacheLog.MSG_RESTORE_FROM_FILE_FAILED;
import static se.skltp.takcache.TakCacheLog.MSG_SAVED_TO_LOCAL_CACHE_FILE;
import static se.skltp.takcache.TakCacheLog.MSG_SAVE_TO_LOCAL_CACHE_FAILED;
import static se.skltp.takcache.TakCacheLog.MSG_SAVE_TO_LOCAL_CACHE_FILE;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;
import static se.skltp.takcache.TakCacheLog.SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY;

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
import se.skltp.takcache.TakCacheLog.RefreshStatus;
import se.skltp.takcache.TakCachePersistentHandler.PersistentCache;
import se.skltp.takcache.exceptions.PersistentCacheException;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.vagval.VagvalHandler;

@Service
public class VagvalCacheImpl implements VagvalCache {

  private static final Logger LOGGER = LogManager.getLogger(VagvalCacheImpl.class);

  TakService takService;

  protected VagvalHandler vagvalHandler;

  protected String localTakCacheFileName;

  @Autowired
  public VagvalCacheImpl(TakService takService, @Value("${takcache.persistent.file.name:#{null}}")String localTakCacheFileName ) {
    this.localTakCacheFileName = localTakCacheFileName;
    this.takService = takService;
  }

  @Override
  public int count(){
    return vagvalHandler!=null ? vagvalHandler.count() : 0;
  }

  @Override
  public TakCacheLog refresh(){
    return refresh(null);
  }

  @Override
  public TakCacheLog refresh(VagvalFilter vagvalFilter) {
    TakCacheLog takCacheLog = new TakCacheLog();
    takCacheLog.logStartInitialize();
    takCacheLog.setRefreshSuccessful(true);

    refreshVagvalCache(takCacheLog, vagvalFilter);

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

    takCacheLog.logEndInitialize(count(), -1);
    return takCacheLog;
  }

  @Override
  public List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress) {
    if (vagvalHandler == null) {
      refreshVagvalCache(new TakCacheLog(), null);
    }
    if (vagvalHandler == null) {
      return Collections.<RoutingInfo>emptyList();
    }
    return toRoutingInfo(vagvalHandler.geVirtualiseringar(tjanstegranssnitt, receiverAddress));
  }

  @Override
  public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress,
      String rivProfile) throws RoutingException {
    if (vagvalHandler == null) {
      refreshVagvalCache(new TakCacheLog(), null);
    }
    return vagvalHandler == null ? null : vagvalHandler.getRoutingAddress(tjanstegranssnitt, receiverAddress, rivProfile);
  }

  public void restoreCache(List<VirtualiseringsInfoType> virtualiseringsInfoTypes) {
    if (virtualiseringsInfoTypes == null) {
      vagvalHandler = null;
    } else {
      vagvalHandler = new VagvalHandler(virtualiseringsInfoTypes);
    }
  }

  @Override
  public List<VirtualiseringsInfoType> getVirtualiseringsInfos() {
    return vagvalHandler == null ? null : vagvalHandler.getVirtualiseringsInfos();
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
  public void setLocalTakCacheFileName(String fileName) {
    this.localTakCacheFileName = fileName;
  }

  public boolean isExistingCacheAvailable() {
    return vagvalHandler != null;
  }


  protected void refreshVagvalCache(TakCacheLog takCacheLog, VagvalFilter vagvalFilter) {
    takCacheLog.setRefreshSuccessful(false);
    try {
      List<VirtualiseringsInfoType> virtualiseringar = takService.getVirtualiseringar();
      if (virtualiseringar == null || virtualiseringar.isEmpty()) {
        LOGGER.warn("Got empty result of virtualisations from TAK service");
        return;
      }
      LOGGER.info("Init number of virtualizations: {}", virtualiseringar.size());
      List<VirtualiseringsInfoType> filteredVirtualiseringar = filterVirtualiseringar(virtualiseringar, vagvalFilter);
      if (filteredVirtualiseringar == null || filteredVirtualiseringar.isEmpty()) {
        LOGGER.error("No VirtualiseringsInfo after filtering");
        return;
      }

      LOGGER.info("Number of filtered virtualizations: {}", filteredVirtualiseringar.size());
      restoreCache(filteredVirtualiseringar);

      takCacheLog.setRefreshStatus(TakCacheLog.RefreshStatus.REFRESH_OK);
      takCacheLog.setRefreshSuccessful(true);
    } catch (Exception e) {
      LOGGER.error("Exception from TakService when getting virtualizations:", e);
    }
  }

  private List<VirtualiseringsInfoType> filterVirtualiseringar(
      List<VirtualiseringsInfoType> virtualiseringar, VagvalFilter vagvalFilter) {
    if (vagvalFilter != null) {
      return virtualiseringar
          .stream()
          .filter(virt -> vagvalFilter.valid(virt))
          .collect(Collectors.toList());
    }
    return virtualiseringar;
  }

  private void saveToLocalFileCache(TakCacheLog takCacheLog) {
    try {
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FILE);
      List<VirtualiseringsInfoType> virtualiseringsInfoTypes = getVirtualiseringsInfos();
      TakCachePersistentHandler.saveVagvalToLocalCache(localTakCacheFileName, virtualiseringsInfoTypes);
      takCacheLog.addLog(MSG_SAVED_TO_LOCAL_CACHE_FILE + localTakCacheFileName);
    } catch (PersistentCacheException e) {
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FAILED + localTakCacheFileName);
      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());
    }
  }

  private void restoreFromLocalFileCache(TakCacheLog takCacheLog) {

    try {
      PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(localTakCacheFileName);
      restoreCache(persistentCache.virtualiseringsInfo);

      LOGGER.warn("Restored from local cache file: " + localTakCacheFileName);
      takCacheLog.addLog(SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY + localTakCacheFileName);
      takCacheLog.setRefreshStatus(RESTORED_FROM_LOCAL_CACHE);

    } catch (PersistentCacheException e) {
      takCacheLog.addLog(MSG_RESTORE_FROM_FILE_FAILED + localTakCacheFileName);
      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());
    }

  }



}
