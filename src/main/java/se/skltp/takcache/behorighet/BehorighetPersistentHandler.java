package se.skltp.takcache.behorighet;

import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

public class BehorighetPersistentHandler {
    private static final Logger LOGGER = LogManager.getLogger(BehorighetPersistentHandler.class);

    private BehorighetPersistentHandler() {
        // Private Utility class
    }

    @XmlRootElement
    static class PersistentCache {
        @XmlElement
        private List<AnropsBehorighetsInfoType> anropsBehorighetsInfo;
    }

    public static void saveToLocalCache(String fileName, List<AnropsBehorighetsInfoType> virtualiseringar) {
        if(fileName==null || fileName.isEmpty()){
            LOGGER.warn("No filename defined for local cache");
            return;
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PersistentCache.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            PersistentCache pc = new PersistentCache();
            pc.anropsBehorighetsInfo = virtualiseringar;

            jaxbMarshaller.marshal(pc, new File(fileName));
        } catch(Exception e){
            LOGGER.error("Failed to save permissions to local TAK copy: "+ fileName, e);
        }
    }

    public static List<AnropsBehorighetsInfoType> restoreFromLocalCache(String fileName) {
        if(fileName==null || fileName.isEmpty()){
            LOGGER.warn("No filename defined for local cache");
            return null;
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PersistentCache.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            PersistentCache persistentCache = (PersistentCache) jaxbUnmarshaller.unmarshal(new File(fileName));
            return persistentCache.anropsBehorighetsInfo;
        }  catch(Exception e){
            LOGGER.error("Failed to restore virtualizations and permissions from local TAK copy: " + fileName, e);
        }
        return null;
    }

}

