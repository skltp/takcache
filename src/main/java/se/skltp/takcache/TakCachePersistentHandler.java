package se.skltp.takcache;

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
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.PersistentCacheException;

public class TakCachePersistentHandler {

  private static final Logger LOGGER = LogManager.getLogger(TakCachePersistentHandler.class);
  public static final String MSG_NO_FILE_NAME_DEFINED = "No filename defined for local cache";
  public static final String MSG_FAILED_TO_RESTORE_FROM_LOCAL_TAK = "Failed to restore from local TAK: ";
  public static final String MSG_FAILED_TO_SAVE_TO_LOCAL_TAK = "Failed to save to local TAK: ";

   private TakCachePersistentHandler() {
    // Private Utility class
  }

  @XmlRootElement
  static class PersistentCache {

    @XmlElement
    protected List<VirtualiseringsInfoType> virtualiseringsInfo;
    @XmlElement
    protected List<AnropsBehorighetsInfoType> anropsBehorighetsInfo;
  }

  public static void saveToLocalCache(String fileName, List<VirtualiseringsInfoType> vagval, List<AnropsBehorighetsInfoType> behorigheter)
      throws PersistentCacheException {
    if (fileName == null || fileName.isEmpty()) {
      LOGGER.warn(MSG_NO_FILE_NAME_DEFINED);
      throw (new PersistentCacheException(MSG_NO_FILE_NAME_DEFINED));
    }

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(PersistentCache.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

      LOGGER.info("Save virtualizations and permissions to local TAK copy: {}", fileName);

      PersistentCache pc = new PersistentCache();
      pc.virtualiseringsInfo = vagval;
      pc.anropsBehorighetsInfo = behorigheter;
      jaxbMarshaller.marshal(pc, new File(fileName));
    } catch (Exception e) {
      LOGGER.error(MSG_FAILED_TO_SAVE_TO_LOCAL_TAK + fileName, e);
      throw (new PersistentCacheException(e));
    }
  }

  public static PersistentCache restoreFromLocalCache(String fileName)
      throws PersistentCacheException {
    return unmarshallPersistenCache(fileName);
  }

  public static PersistentCache unmarshallPersistenCache(String fileName)
      throws PersistentCacheException {
    if (fileName == null || fileName.isEmpty()) {
      LOGGER.warn(MSG_NO_FILE_NAME_DEFINED);
      throw (new PersistentCacheException(MSG_NO_FILE_NAME_DEFINED ));
    }

    try {
      LOGGER.info("Restore virtualizations and permissions from local TAK copy: {}", fileName);
      JAXBContext jaxbContext = JAXBContext.newInstance(PersistentCache.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (PersistentCache) jaxbUnmarshaller.unmarshal(new File(fileName));
    } catch (Exception e) {
      LOGGER.error(MSG_FAILED_TO_RESTORE_FROM_LOCAL_TAK + fileName, e);
      throw (new PersistentCacheException(e));
    }
  }

}
