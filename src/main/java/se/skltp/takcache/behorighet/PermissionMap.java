package se.skltp.takcache.behorighet;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionMap {
    Map<String, List<AnropsBehorighetsInfoType>> permissionMap;

    public PermissionMap(List<AnropsBehorighetsInfoType> permissions) {
        permissionMap = createPermissionMap(permissions);
    }

    public List<AnropsBehorighetsInfoType> lookupInPermissionMap(String receiverId, String senderId, String tjansteKontrakt) {
        String key = createPermissionsMapKey(receiverId, senderId, tjansteKontrakt);
        return permissionMap.get(key);
    }

    private Map<String, List<AnropsBehorighetsInfoType>> createPermissionMap(List<AnropsBehorighetsInfoType> inPermissions) {

       permissionMap = new HashMap<>();

        for (AnropsBehorighetsInfoType p : inPermissions) {
            String key = createPermissionsMapKey(p.getReceiverId(), p.getSenderId(), p.getTjansteKontrakt());

            List<AnropsBehorighetsInfoType> value = permissionMap.get(key);
            if (value == null) {
                value = new ArrayList<>();
                permissionMap.put(key, value);
            }
            value.add(p);
        }

        return permissionMap;
    }

    private String createPermissionsMapKey(String receiverId, String senderId, String tjansteKontrakt) {
        return receiverId + "|" + senderId + "|" + tjansteKontrakt;
    }
}
