package se.skltp.takcache.behorighet;

import org.junit.Test;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.*;

public class BehorighetHandlerTest {
    public static final String NAMNRYMD_1 = "namnrymd-1";
    public static final String RECEIVER_1 = "receiver-1";
    public static final String RECEIVER_2 = "receiver-2";
    public static final String SENDER_1 = "sender-1";
    public static final String SENDER_2 = "sender-2";
    public static final String SENDER_3 = "sender-3";

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

}