package se.skltp.takcache;

import java.util.List;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

public interface BehorigheterCache {

  TakCacheLog refresh();

  TakCacheLog refresh(BehorighetFilter behorighetFilter);

  boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress);

  List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos();

  int count();

  void setLocalTakCacheFileName(String fileName);
}
