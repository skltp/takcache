package se.skltp.takcache.behorighet;

import org.junit.Test;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.*;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.IN_TEN_YEARS;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.getRelativeDate;

public class PermissionMapTest {
    public static final String NAMNRYMD_1 = "namnrymd-1";
    public static final String RECEIVER_1 = "receiver-1";
     public static final String SENDER_1 = "sender-1";
    public static final String SENDER_2 = "sender-2";
    public static final String SENDER_3 = "sender-3";
    public static final String SENDER_4 = "sender-4";

    @Test
    public void testMapCreation() throws Exception {

        ArrayList<AnropsBehorighetsInfoType> authorization = new ArrayList<AnropsBehorighetsInfoType>();
        authorization.add(createAuthorization(SENDER_1, NAMNRYMD_1, RECEIVER_1));
        authorization.add(createAuthorization(SENDER_2, NAMNRYMD_1, RECEIVER_1));
        authorization.add(createAuthorization(SENDER_3, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        authorization.add(createAuthorization(SENDER_3, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));

        PermissionMap permissionMap = new PermissionMap(authorization);

        assertEquals(1, permissionMap.lookupInPermissionMap(RECEIVER_1, SENDER_1, NAMNRYMD_1).size());
        assertEquals(1, permissionMap.lookupInPermissionMap(RECEIVER_1, SENDER_2, NAMNRYMD_1).size());
        assertEquals(2, permissionMap.lookupInPermissionMap(RECEIVER_1, SENDER_3, NAMNRYMD_1).size());
        assertNull(permissionMap.lookupInPermissionMap(RECEIVER_1, SENDER_4, NAMNRYMD_1));
    }


}