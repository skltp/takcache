package se.skltp.takcache.vagval;

import org.junit.jupiter.api.Test;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.exceptions.RoutingFailReason;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.*;

class VagvalHandlerTest {
    private static final String ADDRESS_1 = "address-1";
    private static final String ADDRESS_2 = "address-2";
    private static final String RIV20 = "RIVTABP20";
    private static final String RIV21 = "RIVTABP21";
    private static final String NAMNRYMD_1 = "namnrymd-1";
    private static final String NAMNRYMD_2 = "namnrymd-2";
    private static final String RECEIVER_1 = "receiver-1";
    private static final String RECEIVER_2 = "receiver-2";


    @Test
    void testRoutingOnRivVersions() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV20, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        assertEquals(ADDRESS_1, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV20));
        assertEquals(ADDRESS_2, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21));
    }


    @Test
    void testRoutingOnNameSpace() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_2, RECEIVER_1));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        assertEquals(ADDRESS_1, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21));
        assertEquals(ADDRESS_2, vagvalHandler.getRoutingAddress(NAMNRYMD_2, RECEIVER_1, RIV21));
    }

    @Test
    void testRoutingOnReceiver() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_2));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        assertEquals(ADDRESS_1, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21));
        assertEquals(ADDRESS_2, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
    }

    @Test
    void testNoMatch() {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        RoutingException exception = assertThrows(RoutingException.class, () ->
            vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21)
        );
        assertEquals(RoutingFailReason.NO_MATCH, exception.getFailReason());
    }

    @Test
    void testNoMatchingRivVersion() {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_2));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        RoutingException exception = assertThrows(RoutingException.class, () ->
            vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV20)
        );
        assertTrue(exception.getMessage().contains("riv-profil"));
        assertEquals(RoutingFailReason.NO_MATCHING_RIV_PROFILE, exception.getFailReason());
    }

    @Test
    void testMultipleMatches() {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        RoutingException exception = assertThrows(RoutingException.class, () ->
            vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21)
        );
        assertEquals(RoutingFailReason.MULTIPLE_MATCHES, exception.getFailReason());
    }

    @Test
    void testNoMatchOnValidDates() {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        RoutingException exception = assertThrows(RoutingException.class, () ->
            vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21)
        );
        assertEquals(RoutingFailReason.NO_MATCH, exception.getFailReason());
    }

    @Test
    void testGetRoutingProfiles() {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_2));
        routing.add(createRouting(ADDRESS_1, RIV20, NAMNRYMD_2, RECEIVER_2));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_2, RECEIVER_2));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        assertEquals( 0, vagvalHandler.getRoutingRivProfiles(NAMNRYMD_1, RECEIVER_1).size());
        assertEquals( 1, vagvalHandler.getRoutingRivProfiles(NAMNRYMD_1, RECEIVER_2).size());
        assertEquals( RIV21, vagvalHandler.getRoutingRivProfiles(NAMNRYMD_1, RECEIVER_2).get(0));
        assertEquals( 2, vagvalHandler.getRoutingRivProfiles(NAMNRYMD_2, RECEIVER_2).size());
    }

}