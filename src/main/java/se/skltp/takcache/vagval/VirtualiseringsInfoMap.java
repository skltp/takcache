package se.skltp.takcache.vagval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

public class VirtualiseringsInfoMap {

    private Map<String, List<VirtualiseringsInfoType>> virtualiseringarMap;

    public VirtualiseringsInfoMap( List<VirtualiseringsInfoType> virtualiseringsInfo) {
        this.virtualiseringarMap = createVirtualiseringsInfoMap(virtualiseringsInfo);
    }

    public List<VirtualiseringsInfoType> lookupInVirtualiseringsInfoMap(String receiverId, String tjansteKontrakt) {
        String key = createVirtualiseringsInfoMapKey(receiverId, tjansteKontrakt);
        return virtualiseringarMap.get(key);
    }

    private Map<String, List<VirtualiseringsInfoType>> createVirtualiseringsInfoMap(List<VirtualiseringsInfoType> inVirtualiseringsInfo) {

        Map<String, List<VirtualiseringsInfoType>> outVirtualiseringsInfoMap = new HashMap<>();

        for (VirtualiseringsInfoType v : inVirtualiseringsInfo) {
            String key = createVirtualiseringsInfoMapKey(v.getReceiverId(), v.getTjansteKontrakt());

            List<VirtualiseringsInfoType> value = outVirtualiseringsInfoMap.computeIfAbsent(key, val->new ArrayList<>());
            value.add(v);
        }

        return outVirtualiseringsInfoMap;
    }

    private String createVirtualiseringsInfoMapKey(String receiverId, String tjansteKontrakt) {
        return (receiverId + "|" + tjansteKontrakt).toLowerCase();
    }
}
