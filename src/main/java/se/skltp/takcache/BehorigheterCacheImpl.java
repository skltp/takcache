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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.takcache.TakCacheLog.RefreshStatus;
import se.skltp.takcache.TakCachePersistentHandler.PersistentCache;
import se.skltp.takcache.behorighet.BehorighetHandler;
import se.skltp.takcache.exceptions.PersistentCacheException;
import se.skltp.takcache.services.TakService;

@Service
public class BehorigheterCacheImpl implements BehorigheterCache {

  private static final Logger LOGGER = LogManager.getLogger(TakCacheImpl.class);

  TakService takService;

  protected BehorighetHandler behorighetHandler;

  protected String localTakCacheFileName;

  @Autowired
  public BehorigheterCacheImpl(TakService takService,  @Value("${takcache.persistent.file.name:#{null}}")String localTakCacheFileName) {
    this.localTakCacheFileName = localTakCacheFileName;
    this.takService = takService;
  }

  @Override
  public int count() {
    return behorighetHandler != null ? behorighetHandler.count() : 0;
  }

  @Override
  public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
    if (behorighetHandler == null) {
      refreshBehorighetCache(new TakCacheLog(), null);
    }
    return behorighetHandler != null && behorighetHandler.isAuthorized(senderId, tjanstegranssnitt, receiverAddress);
  }

  @Override
  public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos() {
    return behorighetHandler == null ? Collections.<AnropsBehorighetsInfoType>emptyList()
        : behorighetHandler.getAnropsBehorighetsInfos();
  }

  @Override
  public TakCacheLog refresh() {
    return refresh(null);
  }

  @Override
  public TakCacheLog refresh(BehorighetFilter behorighetFilter) {
    TakCacheLog takCacheLog = new TakCacheLog();
    takCacheLog.logStartInitialize();
    takCacheLog.setRefreshSuccessful(true);

    refreshBehorighetCache(takCacheLog, behorighetFilter);

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

    takCacheLog.logEndInitialize(-1, count());
    return takCacheLog;
  }

  @Override
  public void setLocalTakCacheFileName(String fileName) {
    this.localTakCacheFileName = fileName;
  }

  public void restoreCache(List<AnropsBehorighetsInfoType> behorigheter) {
    if (behorigheter == null) {
      behorighetHandler = null;
    } else {

      behorighetHandler = new BehorighetHandler(behorigheter);
    }
  }

  public void refreshBehorighetCache(TakCacheLog takCacheLog, BehorighetFilter behorighetFilter) {
    takCacheLog.setRefreshSuccessful(false);
    try {
      List<AnropsBehorighetsInfoType> behorigheter = takService.getBehorigheter();
      if (behorigheter == null || behorigheter.isEmpty()) {
        LOGGER.warn(" Got empty result of behorigheter from TAK service");
        return;
      }
      LOGGER.info("Init number of permissions: {}", behorigheter.size());
      List<AnropsBehorighetsInfoType> filteredBehorigheter = filterBehorigheter(behorigheter, behorighetFilter);
      if (filteredBehorigheter == null || filteredBehorigheter.isEmpty()) {
        LOGGER.warn("No behorigheter after filtering");
        return;
      }
      LOGGER.info("Number of filtered permissions: {}", filteredBehorigheter.size());
      restoreCache(filteredBehorigheter);
      takCacheLog.setRefreshSuccessful(true);
    } catch (Exception e) {
      LOGGER.error("Exception from TakService when getting behorigheter:", e);
    }
  }


  private List<AnropsBehorighetsInfoType> filterBehorigheter(
      List<AnropsBehorighetsInfoType> behorigheter, BehorighetFilter behorighetFilter) {
    if (behorighetFilter != null) {
      return behorigheter
          .stream()
          .filter(behorighet -> behorighetFilter.valid(behorighet))
          .collect(Collectors.toList());
    }
    return behorigheter;
  }


  public boolean isExistingCacheAvailable() {
    return behorighetHandler != null;
  }

  private void restoreFromLocalFileCache(TakCacheLog takCacheLog) {
    try {
      PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(localTakCacheFileName);

      restoreCache(persistentCache.anropsBehorighetsInfo);

      LOGGER.warn("Restored permissions from local cache file: " + localTakCacheFileName);
      takCacheLog.addLog(SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY + localTakCacheFileName);
      takCacheLog.setRefreshStatus(RESTORED_FROM_LOCAL_CACHE);

    } catch (PersistentCacheException e) {
      takCacheLog.addLog(MSG_RESTORE_FROM_FILE_FAILED + localTakCacheFileName);
      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());
    }
  }

  private void saveToLocalFileCache(TakCacheLog takCacheLog) {
    try {
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FILE);
      TakCachePersistentHandler
          .saveBehorigheterToLocalCache(localTakCacheFileName, behorighetHandler.getAnropsBehorighetsInfos());
      takCacheLog.addLog(MSG_SAVED_TO_LOCAL_CACHE_FILE + localTakCacheFileName);
    } catch (PersistentCacheException e) {
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FAILED + localTakCacheFileName);
      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());
    }
  }

}
