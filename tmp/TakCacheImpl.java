package se.skltp.takcache;

import static se.skltp.takcache.TakCacheLog.MSG_FAILED_RESTORE_FROM_FILE;
import static se.skltp.takcache.TakCacheLog.MSG_FAILED_USE_EXISTING_CACHE;
import static se.skltp.takcache.TakCacheLog.MSG_INITIALIZE_TAK_CACHE;
import static se.skltp.takcache.TakCacheLog.MSG_RESTORE_FROM_FILE_FAILED;
import static se.skltp.takcache.TakCacheLog.MSG_SAVED_TO_LOCAL_CACHE_FILE;
import static se.skltp.takcache.TakCacheLog.MSG_SAVE_TO_LOCAL_CACHE_FAILED;
import static se.skltp.takcache.TakCacheLog.MSG_SAVE_TO_LOCAL_CACHE_FILE;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_FAILED;
import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;
import static se.skltp.takcache.TakCacheLog.SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.takcache.TakCacheLog.RefreshStatus;
import se.skltp.takcache.TakCacheStatus.Status;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.services.TakService;


@Service
public class TakCacheImpl implements TakCache {

  private static final Logger LOGGER = LogManager.getLogger(TakCacheImpl.class);

  TakService takService;


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
  VagvalCacheImpl vagvalCache;

  @Autowired
  BehorighetCacheImpl behorighetCache;

  @Autowired
  public TakCacheImpl(TakService takService) {
    this.takService = takService;
//    behorighetCache = new BehorighetCacheImpl(takService);
//    vagvalCache = new VagvalCacheImpl(takService);
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
      TakCacheStatus vagvalStatus = vagvalCache.refresh();
      takCacheLog.setNumberVagval(vagvalStatus.getNumberInCache());

      if( !vagvalStatus.isRefreshSuccessful() ){
        setTakCacheLog(takCacheLog, vagvalStatus);
        return takCacheLog;
      }
    }

    if (useBehorighetCache ) {
      TakCacheStatus behorighetStatus = behorighetCache.refresh();
      takCacheLog.setNumberBehorigheter(behorighetStatus.getNumberInCache());

      if( !behorighetStatus.isRefreshSuccessful() ){
        setTakCacheLog(takCacheLog, behorighetStatus);
        return takCacheLog;
      }
    }


    takCacheLog.setRefreshSuccessful(true);
    logEndInitializeTakCache(takCacheLog);
    return takCacheLog;
  }

  private void setTakCacheLog(TakCacheLog takCacheLog, TakCacheStatus vagvalStatus) {
    takCacheLog.setRefreshSuccessful(false);
    if(vagvalStatus.getRefreshStatus() == Status.FAILED){
      takCacheLog.setRefreshStatus(REFRESH_FAILED);

    } else if(vagvalStatus.getRefreshStatus() == Status.FAILED_RESTORE_FROM_LOCAL_CACHE){
      takCacheLog.addLog(MSG_RESTORE_FROM_FILE_FAILED + localTakCacheFileName);
//      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());

      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FILE);
      takCacheLog.addLog(MSG_SAVED_TO_LOCAL_CACHE_FILE + localTakCacheFileName);
      takCacheLog.addLog(MSG_FAILED_RESTORE_FROM_FILE);

      takCacheLog.addLog(MSG_RESTORE_FROM_FILE_FAILED + localTakCacheFileName);
      //      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage());
      takCacheLog.setRefreshStatus(RESTORED_FROM_LOCAL_CACHE);

    }else if(vagvalStatus.getRefreshStatus() == Status.RESTORED_FROM_LOCAL_CACHE){
      takCacheLog.addLog(SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY + localTakCacheFileName);
      takCacheLog.setRefreshStatus(RESTORED_FROM_LOCAL_CACHE);

    }else if(vagvalStatus.getRefreshStatus() == Status.REUSING_EXISTING_CACHE){
      takCacheLog.addLog(MSG_FAILED_USE_EXISTING_CACHE);
      takCacheLog.setRefreshStatus(RefreshStatus.REUSING_EXISTING_CACHE);


    }else if(vagvalStatus.getRefreshStatus() == Status.OK_BUT_FAILED_SAVE_TO_LOCAL_CACHE){
      takCacheLog.addLog(MSG_SAVE_TO_LOCAL_CACHE_FAILED + localTakCacheFileName);
      takCacheLog.addLog(MSG_FAILED_RESTORE_FROM_FILE);
//      takCacheLog.addLog(MSG_REASON_FOR_FAILURE + e.getMessage())
      takCacheLog.setRefreshStatus(REFRESH_FAILED);
    }

  }

  @Override
  public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
    if (behorighetCache != null && useBehorighetCache) {
      behorighetCache.isAuthorized(senderId, tjanstegranssnitt, receiverAddress);
    }
    return false;
  }

  @Override
  public List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress) {
    if (vagvalCache != null && useVagvalCache) {
      return vagvalCache.getRoutingInfo(tjanstegranssnitt, receiverAddress);
    }
    return Collections.<RoutingInfo>emptyList();
  }


  @Override
  public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos() {
    return useBehorighetCache ? behorighetCache.getAnropsBehorighetsInfos() : Collections.<AnropsBehorighetsInfoType>emptyList();
  }

  @Override
  public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress,
      String rivProfile) throws RoutingException {
    if (vagvalCache != null && useVagvalCache) {
      return vagvalCache.getRoutingAddress(tjanstegranssnitt, receiverAddress, rivProfile);
    }
    return null;
  }




   private void logStartInitializeTakCache(TakCacheLog takCacheLog) {
    takCacheLog.addLog("Host: " + getHostName());
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    takCacheLog.addLog("Time: " + df.format(calendar.getTime()));
    takCacheLog.addLog(MSG_INITIALIZE_TAK_CACHE);
  }

  private void logEndInitializeTakCache(TakCacheLog takCacheLog) {


    takCacheLog.addLog("Init TAK cache loaded number of permissions: " + takCacheLog.getNumberBehorigheter());
    takCacheLog.addLog("Init TAK cache loaded number of virtualizations: " + takCacheLog.getNumberVagval());
    takCacheLog.addLog("Init done, was successful: " + (takCacheLog.getRefreshStatus() != REFRESH_FAILED));
  }

  private String getHostName() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      return "UNKNOWN";
    }
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
