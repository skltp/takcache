package se.skltp.takcache.behorighet;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionMap {
    Map<String, List<AnropsBehorighetsInfoType>> behorighetMap;

    public PermissionMap(List<AnropsBehorighetsInfoType> permissions) {
        behorighetMap = createPermissionMap(permissions);
    }

    public List<AnropsBehorighetsInfoType> lookupInPermissionMap(String receiverId, String senderId, String tjansteKontrakt) {
        String key = createPermissionsMapKey(receiverId, senderId, tjansteKontrakt);
        return behorighetMap.get(key);
    }

    private Map<String, List<AnropsBehorighetsInfoType>> createPermissionMap(List<AnropsBehorighetsInfoType> inPermissions) {

       behorighetMap = new HashMap<>();

        for (AnropsBehorighetsInfoType p : inPermissions) {
            String key = createPermissionsMapKey(p.getReceiverId(), p.getSenderId(), p.getTjansteKontrakt());
            List<AnropsBehorighetsInfoType> value = behorighetMap.computeIfAbsent(key, k -> new ArrayList<>());
            value.add(p);
        }

        return behorighetMap;
    }

    private String createPermissionsMapKey(String receiverId, String senderId, String tjansteKontrakt) {
        return receiverId + "|" + senderId + "|" + tjansteKontrakt;
    }
}
