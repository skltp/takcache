package se.skltp.takcache;

import java.util.ArrayList;
import java.util.List;

import static se.skltp.takcache.TakCacheLog.RefreshStatus.REFRESH_FAILED;

public class TakCacheLog {

    public enum RefreshStatus {
        REFRESH_OK,
        REFRESH_FAILED,
        RESTORED_FROM_LOCAL_CACHE;
    }

    private  boolean isRefreshSuccessful = false;
    private  RefreshStatus vagvalRefreshStatus = REFRESH_FAILED;
    private  RefreshStatus behorigheterRefreshStatus = REFRESH_FAILED;

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

    public RefreshStatus getVagvalRefreshStatus() {
        return vagvalRefreshStatus;
    }

    public void setVagvalRefreshStatus(RefreshStatus vagvalRefreshStatus) {
        this.vagvalRefreshStatus = vagvalRefreshStatus;
    }

    public RefreshStatus getBehorigheterRefreshStatus() {
        return behorigheterRefreshStatus;
    }

    public void setBehorigheterRefreshStatus(RefreshStatus behorigheterRefreshStatus) {
        this.behorigheterRefreshStatus = behorigheterRefreshStatus;
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


