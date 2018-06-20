package se.skltp.takcache.vagval;

import org.junit.Test;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.*;

public class VirtualiseringsInfoMapTest {
    private static final String ADDRESS_1 = "address-1";
    private static final String ADDRESS_2 = "address-2";
    private static final String RIV20 = "RIVTABP20";
    private static final String RIV21 = "RIVTABP21";
    private static final String NAMNRYMD_1 = "namnrymd-1";
    private static final String NAMNRYMD_2 = "namnrymd-2";
    private static final String RECEIVER_1 = "receiver-1";
    private static final String RECEIVER_2 = "receiver-2";


    @Test
    public void testMapCreation() {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));
        routing.add(createRouting(ADDRESS_1, RIV20, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV20, NAMNRYMD_2, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV20, NAMNRYMD_2, RECEIVER_2));
        routing.add(createRouting(ADDRESS_2, RIV20, NAMNRYMD_2, RECEIVER_2));

        VirtualiseringsInfoMap permissionMap = new VirtualiseringsInfoMap(routing);
        assertEquals(3, permissionMap.lookupInVirtualiseringsInfoMap(RECEIVER_1, NAMNRYMD_1).size());
        assertEquals(1, permissionMap.lookupInVirtualiseringsInfoMap(RECEIVER_1, NAMNRYMD_2).size());
        assertEquals(2, permissionMap.lookupInVirtualiseringsInfoMap(RECEIVER_2, NAMNRYMD_2).size());
        assertNull(permissionMap.lookupInVirtualiseringsInfoMap(RECEIVER_2, NAMNRYMD_1));
    }


}