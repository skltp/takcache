package se.skltp.takcache;

import java.util.ArrayList;
import java.util.List;

public class TakCacheLog {

    private  boolean isRefreshSuccessful = false;
    private  RefreshStatus refreshStatus = RefreshStatus.REFRESH_FAILED;

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


