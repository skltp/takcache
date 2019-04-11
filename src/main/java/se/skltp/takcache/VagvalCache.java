package se.skltp.takcache;

import java.util.List;
import se.skltp.takcache.exceptions.RoutingException;

public interface VagvalCache {

  TakCacheStatus refresh(String tjanstegranssnittFilter);

  TakCacheStatus refresh();

  List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress);

  String getRoutingAddress(String tjanstegranssnitt, String receiverAddress,
      String rivProfile) throws RoutingException;
}
