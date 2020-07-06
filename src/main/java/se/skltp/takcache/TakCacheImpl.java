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
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.TakCacheLog.RefreshStatus;
import se.skltp.takcache.TakCachePersistentHandler.PersistentCache;
import se.skltp.takcache.exceptions.PersistentCacheException;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.services.TakService;


@Service
public class TakCacheImpl implements TakCache {

  private static final Logger LOGGER = LogManager.getLogger(TakCacheImpl.class);

  protected BehorigheterCacheImpl behorigheterCache;

  protected VagvalCacheImpl vagvalCache;

  protected String localTakCacheFileName;

  public TakCacheImpl(TakService takService) {
    this(takService, null);
  }

  @Autowired
  public TakCacheImpl(TakService takService, @Value("${takcache.persistent.file.name:#{null}}") String localTakCacheFileName) {
    this.localTakCacheFileName = localTakCacheFileName;
    behorigheterCache = new BehorigheterCacheImpl(takService, localTakCacheFileName);
    vagvalCache = new VagvalCacheImpl(takService, localTakCacheFileName);
  }

  @Override
  public TakCacheLog refresh() {
    return refresh(null, null);
  }

  @Override
  public TakCacheLog refresh(List<String> tjanstegranssnittFilter) {
    BehorighetFilter behorighetFilter = new BehorighetContractFilter(tjanstegranssnittFilter);
    VagvalFilter vagvalFilter = new VagvalContractFilter(tjanstegranssnittFilter);
    return refresh(vagvalFilter, behorighetFilter);
  }

  @Override
  public TakCacheLog refresh(VagvalFilter vagvalFilter, BehorighetFilter behorighetFilter) {
    TakCacheLog takCacheLog = new TakCacheLog();
    takCacheLog.logStartInitialize();
    takCacheLog.setRefreshSuccessful(true);

    vagvalCache.refreshVagvalCache(takCacheLog, vagvalFilter);

    if (takCacheLog.isRefreshSuccessful()) {
      behorigheterCache.refreshBehorighetCache(takCacheLog, behorighetFilter);
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

    takCacheLog.logEndInitialize(vagvalCache.count(), behorigheterCache.count());
    return takCacheLog;
  }

  @Override
  public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
    return behorigheterCache.isAuthorized(senderId, tjanstegranssnitt, receiverAddress);
  }

  @Override
  public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos() {
    return behorigheterCache.getAnropsBehorighetsInfos();
  }

  @Override
  public List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress) {
    return vagvalCache.getRoutingInfo(tjanstegranssnitt, receiverAddress);
  }

  @Override
  public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress,
      String rivProfile) throws RoutingException {
    return vagvalCache.getRoutingAddress(tjanstegranssnitt, receiverAddress, rivProfile);
  }

  @Override
  public BehorigheterCache getBehorigeterCache() {
    return behorigheterCache;
  }

  @Override
  public VagvalCache getVagvalCache() {
    return vagvalCache;
  }

  private void saveToLocalFileCache(TakCacheLog takCacheLog) {
    try {
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FILE);
      List<VirtualiseringsInfoType> virtualiseringsInfoTypes = vagvalCache.getVirtualiseringsInfos();
      List<AnropsBehorighetsInfoType> anropsBehorighetsInfoTypes = behorigheterCache.getAnropsBehorighetsInfos();
      TakCachePersistentHandler.saveToLocalCache(localTakCacheFileName, virtualiseringsInfoTypes, anropsBehorighetsInfoTypes);
      takCacheLog.addLog(MSG_SAVED_TO_LOCAL_CACHE_FILE + localTakCacheFileName);
    } catch (PersistentCacheException e) {
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FAILED + localTakCacheFileName);
      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());
    }
  }

  private void restoreFromLocalFileCache(TakCacheLog takCacheLog) {

    try {
      PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(localTakCacheFileName);
      vagvalCache.restoreCache(persistentCache.virtualiseringsInfo);
      behorigheterCache.restoreCache(persistentCache.anropsBehorighetsInfo);

      LOGGER.warn("Restored from local cache file: " + localTakCacheFileName);
      takCacheLog.addLog(SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY + localTakCacheFileName);
      takCacheLog.setRefreshStatus(RESTORED_FROM_LOCAL_CACHE);

    } catch (PersistentCacheException e) {
      takCacheLog.addLog(MSG_RESTORE_FROM_FILE_FAILED + localTakCacheFileName);
      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());
    }

  }


  private boolean isExistingCacheAvailable() {
    return vagvalCache != null && behorigheterCache.isExistingCacheAvailable();
  }

  // Methods to increase testability

  public void setLocalTakCacheFileName(String fileName) {
    this.localTakCacheFileName = fileName;
  }

  public void resetCache() {
    vagvalCache.restoreCache(null);
    behorigheterCache.restoreCache(null);
  }

}
