package se.skltp.takcache.behorighet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.AN_HOUR_AGO;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.IN_ONE_HOUR;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.IN_TEN_YEARS;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.TWO_HOURS_AGO;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.createAuthorization;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.getRelativeDate;

import java.util.ArrayList;
import org.junit.Test;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

public class BehorighetHandlerTest {
    public static final String NAMNRYMD_1 = "namnrymd-1";
    public static final String RECEIVER_1 = "receiver-1";
    public static final String RECEIVER_2 = "receiver-2";
    public static final String SENDER_1 = "sender-1";
    public static final String SENDER_2 = "sender-2";
    public static final String SENDER_3 = "sender-3";

    public static final String RECEIVER_3 = "RECEIVER-3";
    public static final String receiver_3 = "receiver-3";
    public static final String RECEIVER_4 = "RECEIVER-4";
    public static final String receiver_4 = "receiver-4";

    @Test
    public void testIsAuthorized() {

        ArrayList<AnropsBehorighetsInfoType> authorization = new ArrayList<AnropsBehorighetsInfoType>();
        authorization.add(createAuthorization(SENDER_1, NAMNRYMD_1, RECEIVER_1));
        authorization.add(createAuthorization(SENDER_2, NAMNRYMD_1, RECEIVER_1));
        authorization.add(createAuthorization(SENDER_3, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        authorization.add(createAuthorization(SENDER_3, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));

        BehorighetHandler behorighetHandler = new BehorighetHandler(authorization);
        assertTrue( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
        assertTrue( behorighetHandler.isAuthorized(SENDER_2, NAMNRYMD_1, RECEIVER_1));
        assertFalse( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_2));
        assertFalse( behorighetHandler.isAuthorized(SENDER_3, NAMNRYMD_1, RECEIVER_1));
    }

    @Test
    public void testIsAuthorized_ReceiverCaseInsensitivity() {
        ArrayList<AnropsBehorighetsInfoType> authorization = new ArrayList<AnropsBehorighetsInfoType>();
        authorization.add(createAuthorization(SENDER_1, NAMNRYMD_1, RECEIVER_3));
        authorization.add(createAuthorization(SENDER_1, NAMNRYMD_1, receiver_4));

        BehorighetHandler behorighetHandler = new BehorighetHandler(authorization);
        assertTrue( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, receiver_3));
        assertTrue( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_4));
    }

}