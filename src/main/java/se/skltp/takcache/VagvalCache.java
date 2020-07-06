package se.skltp.takcache;

import java.util.List;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.RoutingException;

public interface VagvalCache {

  TakCacheLog refresh();

  TakCacheLog refresh(VagvalFilter vagvalFilter);

  List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress);

  String getRoutingAddress(String tjanstegranssnitt, String receiverAddress,
      String rivProfile) throws RoutingException;

  List<VirtualiseringsInfoType> getVirtualiseringsInfos();

  int count();

  void setLocalTakCacheFileName(String fileName);
}
