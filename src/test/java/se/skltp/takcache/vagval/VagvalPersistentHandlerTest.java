package se.skltp.takcache.vagval;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.*;

public class VagvalPersistentHandlerTest {
    private static final String ADDRESS_1 = "address-1";
    private static final String ADDRESS_2 = "address-2";
    private static final String RIV20 = "RIVTABP20";
    private static final String RIV21 = "RIVTABP21";
    private static final String NAMNRYMD_1 = "namnrymd-1";
    private static final String NAMNRYMD_2 = "namnrymd-2";
    private static final String RECEIVER_1 = "receiver-1";
    private static final String RECEIVER_2 = "receiver-2";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void persistentTest() throws JAXBException {
        ArrayList<VirtualiseringsInfoType> routings = new ArrayList<>();
        routings.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        routings.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));
        routings.add(createRouting(ADDRESS_1, RIV21, NAMNRYMD_1, RECEIVER_2));
        routings.add(createRouting(ADDRESS_1, RIV20, NAMNRYMD_1, RECEIVER_2));
        routings.add(createRouting(ADDRESS_2, RIV21, NAMNRYMD_1, RECEIVER_2));

        String fileName = String.format("%s/vagvalcache-test.xml", testFolder.getRoot().getPath());

        VagvalPersistentHandler.saveToLocalCache(fileName, routings );

        List<VirtualiseringsInfoType> routingsFromFile = VagvalPersistentHandler.restoreFromLocalCache(fileName);
        assertEquals(5, routingsFromFile.size());
        assertEquals(NAMNRYMD_1, routingsFromFile.get(0).getTjansteKontrakt());
    }

}