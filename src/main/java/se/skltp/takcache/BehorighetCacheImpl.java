package se.skltp.takcache;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.takcache.behorighet.BehorighetHandler;
import se.skltp.takcache.exceptions.PersistentCacheException;
import se.skltp.takcache.services.TakService;

@Service
public class BehorighetCacheImpl implements BehorighetCache {

  private static final Logger LOGGER = LogManager.getLogger(TakCacheImpl.class);

  TakService takService;

  protected BehorighetHandler behorighetCache;

  @Value("${takcache.persistent.file.name:#{null}}")
  protected String localTakCacheFileName;

  @Value("${takcache.tjanstegranssnitt.filter:#{null}}")
  protected String tjanstegranssnittFilter;


  @Autowired
  public BehorighetCacheImpl(TakService takService) {
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
    takCacheStatus.setLocalFileName(localTakCacheFileName);
    takCacheStatus.setTakAddress(takService.getEndPointAddress());

    takCacheStatus.setRefreshSuccessful(true);

    refreshBehorighetCache(takCacheStatus);

    if (takCacheStatus.isRefreshSuccessful()) {
      takCacheStatus.setRefreshStatus(RefreshStatus.REFRESH_OK);
      saveBehorigheter();
    } else if (behorighetCache != null) {
      takCacheStatus.setRefreshStatus(RefreshStatus.REUSING_EXISTING_CACHE);
    } else {
      restoreFromLocalFileCache(takCacheStatus);
    }
    takCacheStatus.setNumberInCache(behorighetCache == null ? 0 : behorighetCache.count());
    return takCacheStatus;
  }


  @Override
  public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
    if (behorighetCache == null) {
      refresh();
    }
    return behorighetCache != null && behorighetCache.isAuthorized(senderId, tjanstegranssnitt, receiverAddress);
  }

  @Override
  public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos() {
    return behorighetCache == null ? Collections.<AnropsBehorighetsInfoType>emptyList()
        : behorighetCache.getAnropsBehorighetsInfos();
  }

  private void refreshBehorighetCache(TakCacheStatus takCacheStatus) {
    takCacheStatus.setRefreshSuccessful(false);
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
      takCacheStatus.setRefreshSuccessful(true);
    } catch (Exception e) {
      LOGGER.error("Exception from TakService when getting behorigheter:", e);
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

  private void saveBehorigheter() {
    try {
      List<AnropsBehorighetsInfoType> anropsBehorighetsInfoTypes = behorighetCache.getAnropsBehorighetsInfos();
      TakCachePersistentHandler.saveBehorigheter(localTakCacheFileName, anropsBehorighetsInfoTypes);
    } catch (PersistentCacheException e) {
      LOGGER.error("Failed save to local cache file: " + localTakCacheFileName, e);
    }
  }

  private void restoreFromLocalFileCache(TakCacheStatus takCacheStatus) {

    try {
      behorighetCache = new BehorighetHandler(TakCachePersistentHandler.restoreBehorigheter(localTakCacheFileName));
      LOGGER.warn("Restored from local cache file: " + localTakCacheFileName);
      takCacheStatus.setRefreshStatus(RefreshStatus.RESTORED_FROM_LOCAL_CACHE);

    } catch (PersistentCacheException e) {
      LOGGER.error("Failed restore from local cache file: " + localTakCacheFileName, e);
    }
  }

  public void setLocalTakCacheFileName(String fileName) {
    this.localTakCacheFileName = fileName;
  }

}
