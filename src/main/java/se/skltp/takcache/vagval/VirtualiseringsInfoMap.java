package se.skltp.takcache.vagval;

import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualiseringsInfoMap {

    private Map<String, List<VirtualiseringsInfoType>> virtualiseringsInfoMap;

    public VirtualiseringsInfoMap( List<VirtualiseringsInfoType> virtualiseringsInfo) {
        this.virtualiseringsInfoMap = createVirtualiseringsInfoMap(virtualiseringsInfo);
    }

    public List<VirtualiseringsInfoType> lookupInVirtualiseringsInfoMap(String receiverId, String tjansteKontrakt) {
        String key = createVirtualiseringsInfoMapKey(receiverId, tjansteKontrakt);
        return virtualiseringsInfoMap.get(key);
    }

    private Map<String, List<VirtualiseringsInfoType>> createVirtualiseringsInfoMap(List<VirtualiseringsInfoType> inVirtualiseringsInfo) {

        Map<String, List<VirtualiseringsInfoType>> outVirtualiseringsInfoMap = new HashMap<>();

        for (VirtualiseringsInfoType v : inVirtualiseringsInfo) {
            String key = createVirtualiseringsInfoMapKey(v.getReceiverId(), v.getTjansteKontrakt());

            List<VirtualiseringsInfoType> value = outVirtualiseringsInfoMap.get(key);
            if (value == null) {
                value = new ArrayList<>();
                outVirtualiseringsInfoMap.put(key, value);
            }

            value.add(v);
        }

        return outVirtualiseringsInfoMap;
    }

    private String createVirtualiseringsInfoMapKey(String receiverId, String tjansteKontrakt) {
        return receiverId + "|" + tjansteKontrakt;
    }
}
