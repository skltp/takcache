package se.skltp.takcache;

import java.util.List;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

public interface BehorighetCache {

  TakCacheStatus refresh(String tjanstegranssnittFilter);

  TakCacheStatus refresh();

  boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress);

  List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos();
}
