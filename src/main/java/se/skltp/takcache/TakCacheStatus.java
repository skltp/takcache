package se.skltp.takcache;

import lombok.Data;

@Data
public class TakCacheStatus {
  private  boolean isRefreshSuccessful = false;
  private RefreshStatus refreshStatus = RefreshStatus.REFRESH_FAILED;

  private int numberInCache;

  private String localFileName;
  private String takAddress;
}
