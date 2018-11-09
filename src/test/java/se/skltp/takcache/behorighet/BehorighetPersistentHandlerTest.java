package se.skltp.takcache.behorighet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static se.skltp.takcache.util.VagvalSchemasTestUtil.*;

public class BehorighetPersistentHandlerTest {
    public static final String NAMNRYMD_1 = "namnrymd-1";
    public static final String RECEIVER_1 = "receiver-1";
    public static final String SENDER_1 = "sender-1";
    public static final String SENDER_2 = "sender-2";
    public static final String SENDER_3 = "sender-3";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void persistentTest() throws JAXBException {
        ArrayList<AnropsBehorighetsInfoType> authorizations = new ArrayList<AnropsBehorighetsInfoType>();
        authorizations.add(createAuthorization(SENDER_1, NAMNRYMD_1, RECEIVER_1));
        authorizations.add(createAuthorization(SENDER_2, NAMNRYMD_1, RECEIVER_1));
        authorizations.add(createAuthorization(SENDER_3, NAMNRYMD_1, RECEIVER_1, getRelativeDate(TWO_HOURS_AGO), getRelativeDate(AN_HOUR_AGO)));
        authorizations.add(createAuthorization(SENDER_3, NAMNRYMD_1, RECEIVER_1, getRelativeDate(IN_ONE_HOUR), getRelativeDate(IN_TEN_YEARS)));

        String fileName = String.format("%s/behorighetcache-test.xml", testFolder.getRoot().getPath());

        BehorighetPersistentHandler.saveToLocalCache(fileName, authorizations );

        List<AnropsBehorighetsInfoType> authorizationsFromFile = BehorighetPersistentHandler.restoreFromLocalCache(fileName);
        assertEquals(4, authorizationsFromFile.size());
        assertEquals(NAMNRYMD_1, authorizationsFromFile.get(0).getTjansteKontrakt());
    }

  }