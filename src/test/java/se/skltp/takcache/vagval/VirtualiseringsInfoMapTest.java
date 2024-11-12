package se.skltp.takcache.vagval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.AN_HOUR_AGO;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.IN_ONE_HOUR;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.IN_TEN_YEARS;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.TWO_HOURS_AGO;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.createRouting;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.getRelativeDate;

import java.util.ArrayList;
import org.junit.Test;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

public class VirtualiseringsInfoMapTest {
    private static final String ADDRESS_1 = "address-1";
    private static final String ADDRESS_2 = "address-2";
    private static final String RIV20 = "RIVTABP20";
    private static final String RIV21 = "RIVTABP21";
    private static final String NAMNRYMD_1 = "namnrymd-1";
    private static final String NAMNRYMD_2 = "namnrymd-2";
    private static final String RECEIVER_1 = "receiver-1";
    private static final String RECEIVER_2 = "receiver-2";


    public static final String RECEIVER_3 = "RECEIVER-3";
    public static final String receiver_3 = "receiver-3";
    public static final String RECEIVER_4 = "RECEIVER-4";
    public static final String receiver_4 = "receiver-4";

    @Test
    public void testVirtualiseringsMap() {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));
        routing.add(createRouting(ADDRESS_1, RIV20, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV20, NAMNRYMD_2, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV20, NAMNRYMD_2, RECEIVER_2));
        routing.add(createRouting(ADDRESS_2, RIV20, NAMNRYMD_2, RECEIVER_2));

        VirtualiseringsInfoMap virtualiseringsMap = new VirtualiseringsInfoMap(routing);
        assertEquals(3, virtualiseringsMap.lookupInVirtualiseringsInfoMap(RECEIVER_1, NAMNRYMD_1).size());
        assertEquals(1, virtualiseringsMap.lookupInVirtualiseringsInfoMap(RECEIVER_1, NAMNRYMD_2).size());
        assertEquals(2, virtualiseringsMap.lookupInVirtualiseringsInfoMap(RECEIVER_2, NAMNRYMD_2).size());
        assertNull(virtualiseringsMap.lookupInVirtualiseringsInfoMap(RECEIVER_2, NAMNRYMD_1));
    }


    @Test
    public void testVirtualiseringsMap_ReceiverCaseInsensitivity() {
        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_2, RECEIVER_3));
        routing.add(createRouting(ADDRESS_2, RIV20, NAMNRYMD_2, receiver_4));

        VirtualiseringsInfoMap virtualiseringsMap = new VirtualiseringsInfoMap(routing);
        assertEquals(1, virtualiseringsMap.lookupInVirtualiseringsInfoMap(receiver_3, NAMNRYMD_2).size());
        assertEquals(1, virtualiseringsMap.lookupInVirtualiseringsInfoMap(RECEIVER_4, NAMNRYMD_2).size());
    }

}