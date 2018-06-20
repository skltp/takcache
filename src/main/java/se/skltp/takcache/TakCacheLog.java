package se.skltp.takcache;

import java.util.ArrayList;
import java.util.List;

public class TakCacheLog {
    private  boolean isRefreshSuccessful = false;
    private  boolean isVagvalRefreshSuccessful = false;
    private  boolean isBehorigheterRefreshSuccessful = false;

    private  int numberBehorigheter;
    private  int numberVagval;

    private  List<String> logBuffer = null;

    public TakCacheLog() {
            logBuffer = new ArrayList<String>();
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

    public boolean isVagvalRefreshSuccessful() {
        return isVagvalRefreshSuccessful;
    }

    public void setVagvalRefreshSuccessful(boolean vagvalRefreshSuccessful) {
        isVagvalRefreshSuccessful = vagvalRefreshSuccessful;
    }

    public boolean isBehorigheterRefreshSuccessful() {
        return isBehorigheterRefreshSuccessful;
    }

    public void setBehorigheterRefreshSuccessful(boolean behorigheterRefreshSuccessful) {
        isBehorigheterRefreshSuccessful = behorigheterRefreshSuccessful;
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
