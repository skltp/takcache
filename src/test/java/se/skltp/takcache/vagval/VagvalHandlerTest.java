package se.skltp.takcache.vagval;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.exceptions.RoutingFailReason;

import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.*;

public class VagvalHandlerTest {
    private static final String ADDRESS_1 = "address-1";
    private static final String ADDRESS_2 = "address-2";
    private static final String RIV20 = "RIVTABP20";
    private static final String RIV21 = "RIVTABP21";
    private static final String NAMNRYMD_1 = "namnrymd-1";
    private static final String NAMNRYMD_2 = "namnrymd-2";
    private static final String RECEIVER_1 = "receiver-1";
    private static final String RECEIVER_2 = "receiver-2";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testRoutingOnRivVersions() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV20, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        assertEquals(ADDRESS_1, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV20));
        assertEquals(ADDRESS_2, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21));
    }


    @Test
    public void testRoutingOnNameSpace() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_2, RECEIVER_1));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        assertEquals(ADDRESS_1, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21));
        assertEquals(ADDRESS_2, vagvalHandler.getRoutingAddress(NAMNRYMD_2, RECEIVER_1, RIV21));
    }

    @Test
    public void testRoutingOnReceiver() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_2));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        assertEquals(ADDRESS_1, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21));
        assertEquals(ADDRESS_2, vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21));
    }

    @Test
    public void testNoMatch() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));

        thrown.expect(RoutingException.class);
        thrown.expect(hasProperty("failReason", is(RoutingFailReason.NO_MATCH)));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_2, RIV21);
    }

    @Test
    public void testNoMatchingRivVersion() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_2));

        thrown.expect(RoutingException.class);
        thrown.expectMessage(containsString("riv-profil"));
        thrown.expect(hasProperty("failReason", is(RoutingFailReason.NO_MATCHING_RIV_PROFILE)));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV20);
    }

    @Test
    public void testMultipleMatches() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1));

        thrown.expect(RoutingException.class);
        thrown.expect(hasProperty("failReason", is(RoutingFailReason.MULTIPLE_MATCHES)));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21);
    }

    @Test
    public void testNoMatchOnValidDates() throws Exception {

        ArrayList<VirtualiseringsInfoType> routing = new ArrayList<>();
        routing.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        routing.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));

        thrown.expect(RoutingException.class);
        thrown.expect(hasProperty("failReason", is(RoutingFailReason.NO_MATCH)));

        VagvalHandler vagvalHandler = new VagvalHandler(routing);
        vagvalHandler.getRoutingAddress(NAMNRYMD_1, RECEIVER_1, RIV21);
    }

    @Test
    public void testGetRoutingProfiles() throws Exception {

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