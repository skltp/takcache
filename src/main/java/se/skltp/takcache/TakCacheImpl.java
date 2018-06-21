package se.skltp.takcache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.behorighet.BehorighetHandler;
import se.skltp.takcache.behorighet.BehorighetPersistentHandler;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.services.TakService;
import se.skltp.takcache.vagval.VagvalHandler;
import se.skltp.takcache.vagval.VagvalPersistentHandler;

import java.util.Collections;
import java.util.List;

import static se.skltp.takcache.TakCacheLog.RefreshStatus.RESTORED_FROM_LOCAL_CACHE;


@Service
public class TakCacheImpl implements TakCache {
    private final static Logger LOGGER = LogManager.getLogger(TakCacheImpl.class);

    public static final String USE_BEHORIGHET_PROPERTY_NAME = "takcache.use.behorighet.cache";
    public static final String USE_VAGVAL_PROPERTY_NAME     = "takcache.use.vagval.cache";
    public static final String VAGVAL_PERSISTENT_FILE_PROPERTY_NAME     = "takcache.vagval.persistent.file.name";
    public static final String BEHORIGHET_PERSISTENT_FILE_PROPERTY_NAME     = "takcache.behorighet.persistent.file.name";


    TakService takService;

    protected BehorighetHandler behorighetCache;
    protected VagvalHandler vagvalCache;

    protected boolean useBehorighetCache;
    protected boolean useVagvalCache;

    protected String behorighetFileName;
    protected String vagvalFileName;

    @Autowired
    public TakCacheImpl(TakService takService, Environment env){
        this.takService = takService;

        String useBehorighetFromProperty = env.getProperty(USE_BEHORIGHET_PROPERTY_NAME);
        String useVagvalFromProperty = env.getProperty(USE_VAGVAL_PROPERTY_NAME);
        this.useBehorighetCache = useBehorighetFromProperty==null ? true : Boolean.parseBoolean(useBehorighetFromProperty);
        this.useVagvalCache     = useVagvalFromProperty==null ? true: Boolean.parseBoolean(useVagvalFromProperty);

        String behorighetPersistentFileName = env.getProperty(BEHORIGHET_PERSISTENT_FILE_PROPERTY_NAME);
        String vagvalPersistentFileName = env.getProperty(VAGVAL_PERSISTENT_FILE_PROPERTY_NAME);
        setLocalCacheFileNames(behorighetPersistentFileName, vagvalPersistentFileName);
    }

    @Override
    public TakCacheLog refresh() {
        TakCacheLog takCacheLog = new TakCacheLog();
        takCacheLog.setRefreshSuccessful(true);

        if(useVagvalCache) {
            refreshVagvalCache(takCacheLog);
            if( vagvalCache == null){
                takCacheLog.setRefreshSuccessful(false);
                vagvalCache = restoreVagvalFromLocalFileCache(takCacheLog);
            }
        }

        if(useBehorighetCache) {
            refreshBehorighetCache(takCacheLog);
            if( behorighetCache == null){
                takCacheLog.setRefreshSuccessful(false);
                behorighetCache = restoreBehorigheterFromLocalFileCache(takCacheLog);
            }
        }
        return takCacheLog;
    }

    @Override
    public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
        if( behorighetCache == null && useBehorighetCache){
            refresh();
        }
        return behorighetCache == null ? false : behorighetCache.isAuthorized(senderId, tjanstegranssnitt, receiverAddress);
    }

    @Override
    public List<String> getRoutingRivProfiles(String tjanstegranssnitt, String receiverAddress) {
        if( vagvalCache == null && useVagvalCache){
            refresh();
        }
        return vagvalCache == null ? Collections.<String>emptyList() : vagvalCache.getRoutingRivProfiles(tjanstegranssnitt, receiverAddress);
    }

    @Override
    public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress, String rivProfile) throws RoutingException {
        if( vagvalCache == null && useVagvalCache){
            refresh();
        }
        return vagvalCache == null ? null : vagvalCache.getRoutingAddress(tjanstegranssnitt, receiverAddress, rivProfile);
    }

    protected void setLocalCacheFileNames(String behorighetFileName, String vagvalFileName){
        this.behorighetFileName = behorighetFileName;
        this.vagvalFileName     = vagvalFileName;
    }

    private BehorighetHandler restoreBehorigheterFromLocalFileCache(TakCacheLog takCacheLog) {
        List<AnropsBehorighetsInfoType> anropsBehorighetsInfoTypes = BehorighetPersistentHandler.restoreFromLocalCache(behorighetFileName);
        if(anropsBehorighetsInfoTypes==null){
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
        List<VirtualiseringsInfoType> virtualiseringsInfoTypes = VagvalPersistentHandler.restoreFromLocalCache(vagvalFileName);
        if(virtualiseringsInfoTypes==null){
            takCacheLog.addLog("Failed restore vagval from local cache.");
            takCacheLog.setNumberVagval(0);
            return null;
        }
        LOGGER.warn("Virtualisations restored from local cache");
        takCacheLog.addLog("Restored vagval from local cache.");
        takCacheLog.setVagvalRefreshStatus(RESTORED_FROM_LOCAL_CACHE);
        takCacheLog.setNumberVagval( virtualiseringsInfoTypes.size());
        return  new VagvalHandler(virtualiseringsInfoTypes);
    }

    private void refreshVagvalCache(TakCacheLog takCacheLog) {
        try {
            List<VirtualiseringsInfoType> virtualiseringar = takService.getVirtualiseringar();
            if (virtualiseringar != null && !virtualiseringar.isEmpty()) {
                LOGGER.info("Init number of virtualizations: {}", virtualiseringar.size());
                vagvalCache = new VagvalHandler(virtualiseringar);
                takCacheLog.setVagvalRefreshStatus(TakCacheLog.RefreshStatus.REFRESH_OK);
                takCacheLog.setNumberVagval(virtualiseringar.size());
                VagvalPersistentHandler.saveToLocalCache(vagvalFileName, virtualiseringar);
            } else {
                takCacheLog.addLog("Failed to refresh TAK data. Got empty result of virtualisations from TAK service");
                LOGGER.warn("Failed to refresh TAK data. No VirtualiseringsInfo found, TAK returned empty list.");
            }
        } catch(Exception e){
            takCacheLog.addLog("Failed to refresh TAK virtualisations. Exception from TakService: "+e.getMessage());
            LOGGER.error("Failed to refresh TAK virtualisations. Exception from TakService:", e);
        }
    }

    private void refreshBehorighetCache(TakCacheLog takCacheLog) {
         try {
            List<AnropsBehorighetsInfoType> behorigheter = takService.getBehorigheter();
             if(behorigheter != null && !behorigheter.isEmpty()) {
                 LOGGER.info("Init number of permissions: {}", behorigheter.size());
                 behorighetCache = new BehorighetHandler(behorigheter);
                 takCacheLog.setBehorigheterRefreshStatus(TakCacheLog.RefreshStatus.REFRESH_OK);
                 takCacheLog.setNumberBehorigheter(behorigheter.size());
                 BehorighetPersistentHandler.saveToLocalCache(behorighetFileName, behorigheter);
            } else {
                 takCacheLog.addLog("Failed to refresh TAK behorigheter. Got empty result of behorigheter from TAK service");
                 LOGGER.warn("Failed to refresh TAK data. No AnropsBehorighetsInfo found, TAK returned empty list.");
            }
        } catch(Exception e){
             takCacheLog.addLog("Failed to refresh TAK behorigheter. Exception from TakService: "+e.getMessage());
             LOGGER.error("Failed to refresh TAK behorigheter. Exception from TakService:", e);
        }
    }

}
