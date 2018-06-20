package se.skltp.takcache;

import se.skltp.takcache.exceptions.RoutingException;

import java.util.List;

public interface TakCache {
    public boolean refresh();
    public boolean refresh(TakCacheLog takCacheLog);

    public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress);

    public List<String> getRoutingRivProfiles(String tjanstegranssnitt, String receiverAddress);

    public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress, String rivProfile) throws RoutingException;
}
