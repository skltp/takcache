package se.skltp.takcache;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.takcache.exceptions.RoutingException;

import java.util.List;

public interface TakCache {

    TakCacheLog refresh();

    TakCacheLog refresh(List<String> tjanstegranssnittFilter);

    TakCacheLog refresh(VagvalFilter vagvalFilter, BehorighetFilter behorighetFilter);

    @Deprecated
    boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress);

    @Deprecated
    List<RoutingInfo> getRoutingInfo(String tjanstegranssnitt, String receiverAddress);

    @Deprecated
    List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos();

    @Deprecated
    String getRoutingAddress(String tjanstegranssnitt, String receiverAddress, String rivProfile) throws RoutingException;

    BehorigheterCache getBehorigeterCache();

    VagvalCache getVagvalCache();
}
