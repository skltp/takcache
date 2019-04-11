package se.skltp.takcache;

import java.util.ArrayList;
import java.util.List;

import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_FAILED;

public class TakCacheLog {
  public static final String MSG_INITIALIZE_TAK_CACHE = "Initialize TAK cache resources...";
  public static final String MSG_SAVE_TO_LOCAL_CACHE_FILE = "Succeeded to get virtualizations and/or permissions from TAK, save to local TAK copy...";
  public static final String MSG_SAVED_TO_LOCAL_CACHE_FILE = "Succesfully saved virtualizations and permissions to local TAK copy: ";
  public static final String MSG_SAVED_BEHORIGHETER_TO_LOCAL_CACHE_FILE = "Succesfully saved permissions to local TAK copy: ";
  public static final String MSG_SAVED_VAGVAL_TO_LOCAL_CACHE_FILE = "Succesfully saved virtualizations to local TAK copy: ";
  public static final String MSG_SAVE_TO_LOCAL_CACHE_FAILED = "Failed to save virtualizations and permissions to local TAK copy: ";
  public static final String MSG_REASON_FOR_FAILURE = "Reason for failure: ";
  public static final String MSG_FAILED_USE_EXISTING_CACHE = "Failed to get virtualizations and/or permissions from TAK, see logfiles for details. Will continue to use already loaded TAK data.";
  public static final String MSG_FAILED_USE_EXISTING_BEHORIGHETS_CACHE = "Failed to get permissions from TAK, see logfiles for details. Will continue to use already loaded TAK data.";
  public static final String MSG_FAILED_RESTORE_FROM_FILE = "Failed to get virtualizations and/or permissions from TAK, see logfiles for details. Restore from local TAK copy...";
  public static final String SUCCESFULLY_RESTORED_FROM_LOCAL_TAK_COPY = "Succesfully restored virtualizations and permissions from local TAK copy: ";
  public static final String MSG_RESTORE_FROM_FILE_FAILED = "Failed to restore virtualizations and permissions from local TAK copy: ";

  public enum RefreshStatus {
        REFRESH_OK,
        REFRESH_FAILED,
        RESTORED_FROM_LOCAL_CACHE,
        REUSING_EXISTING_CACHE;
    }

    private  boolean isRefreshSuccessful = false;
    private  RefreshStatus refreshStatus = REFRESH_FAILED;

    private  int numberBehorigheter;
    private  int numberVagval;


    private  List<String> logBuffer = null;

    public TakCacheLog() {
            logBuffer = new ArrayList<>();
    }

    public void addLog(String log) {
            logBuffer.add(log);
    }

    public List<String> getLog() {
        return logBuffer;
    }

    public boolean isRefreshSuccessful() {
        return isRefreshSuccessful;
    }

    public void setRefreshSuccessful(boolean refreshSuccessful) {
        isRefreshSuccessful = refreshSuccessful;
    }

    public RefreshStatus getRefreshStatus() {
        return refreshStatus;
    }

    public void setRefreshStatus(RefreshStatus refreshStatus) {
        this.refreshStatus = refreshStatus;
    }

    public int getNumberBehorigheter() {
        return numberBehorigheter;
    }

    public void setNumberBehorigheter(int numberBehorigheter) {
        this.numberBehorigheter = numberBehorigheter;
    }

    public int getNumberVagval() {
        return numberVagval;
    }

    public void setNumberVagval(int numberVagval) {
        this.numberVagval = numberVagval;
    }
}


