package se.skltp.takcache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import se.skltp.takcache.TakCachePersistentHandler.PersistentCache;
import se.skltp.takcache.exceptions.PersistentCacheException;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;

public class TakCachePersistentHandlerTest {

  @TempDir
  Path testFolder;

  @Test
  public void persistentTest() throws JAXBException, PersistentCacheException {

    String fileName = testFolder.resolve("takcache-test.xml").toString();

    TakCachePersistentHandler.saveToLocalCache(fileName, VagvalSchemasTestListsUtil.getStaticVagvalList(),
        VagvalSchemasTestListsUtil.getStaticBehorighetList());

    PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(fileName);
    assertEquals(5, persistentCache.anropsBehorighetsInfo.size());
    assertEquals(5, persistentCache.virtualiseringsInfo.size());

  }

  @Test
  public void persistentTestOnOnlyVagval() throws JAXBException, PersistentCacheException {

    String fileName = testFolder.resolve("vagvalcache-test.xml").toString();

    TakCachePersistentHandler.saveToLocalCache(fileName, VagvalSchemasTestListsUtil.getStaticVagvalList(),
        null);

    PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(fileName);
    assertEquals(5, persistentCache.virtualiseringsInfo.size());
    assertNull( persistentCache.anropsBehorighetsInfo);

  }

  @Test
  public void persistentTestOnOnlyBehorigheter() throws JAXBException, PersistentCacheException {

    String fileName = testFolder.resolve("behorighetcache-test.xml").toString();

    TakCachePersistentHandler.saveToLocalCache(fileName, null, VagvalSchemasTestListsUtil.getStaticBehorighetList());

    PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(fileName);
    assertEquals(5, persistentCache.anropsBehorighetsInfo.size());
    assertNull( persistentCache.virtualiseringsInfo);

  }
}