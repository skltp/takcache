package se.skltp.takcache.util;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.behorighet.BehorighetPersistentHandler;
import se.skltp.takcache.vagval.VagvalPersistentHandler;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static se.skltp.takcache.util.TestTakDataDefines.*;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.*;

public class VagvalSchemasTestListsUtil {
    public static List<AnropsBehorighetsInfoType> getBehorigheterFromXmlResource(String resourceName){
        URL url = VagvalSchemasTestListsUtil.class.getClassLoader().getResource(resourceName);
        return BehorighetPersistentHandler.restoreFromLocalCache(url.getFile());
    }

    public static List<VirtualiseringsInfoType> getVagvalFromXmlResource(String resourceName){
        URL url = VagvalSchemasTestListsUtil.class.getClassLoader().getResource(resourceName);
        return VagvalPersistentHandler.restoreFromLocalCache(url.getFile());
    }

    public static List<AnropsBehorighetsInfoType> getStaticBehorighetList(){
        List<AnropsBehorighetsInfoType> authorization = new ArrayList<AnropsBehorighetsInfoType>();
        authorization.add(createAuthorization(SENDER_1, NAMNRYMD_1, RECEIVER_1));
        authorization.add(createAuthorization(SENDER_2, NAMNRYMD_1, RECEIVER_1));
        authorization.add(createAuthorization(SENDER_3, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        authorization.add(createAuthorization(SENDER_3, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));
        return authorization;
    }
    public static List<VirtualiseringsInfoType> getStaticVagvalList() {
        List<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_2));
        routing.add(createRouting(ADDRESS_1, RIV20, NAMNRYMD_2, RECEIVER_2));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_2, RECEIVER_2));
        return routing;
    }


}
