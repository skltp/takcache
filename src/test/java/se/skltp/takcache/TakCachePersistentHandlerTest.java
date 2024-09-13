package se.skltp.takcache;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

import jakarta.xml.bind.JAXBException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import se.skltp.takcache.TakCachePersistentHandler.PersistentCache;
import se.skltp.takcache.exceptions.PersistentCacheException;
import se.skltp.takcache.util.VagvalSchemasTestListsUtil;

public class TakCachePersistentHandlerTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void persistentTest() throws JAXBException, PersistentCacheException {

    String fileName = String.format("%s/takcache-test.xml", testFolder.getRoot().getPath());

    TakCachePersistentHandler.saveToLocalCache(fileName, VagvalSchemasTestListsUtil.getStaticVagvalList(),
        VagvalSchemasTestListsUtil.getStaticBehorighetList());

    PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(fileName);
    assertEquals(5, persistentCache.anropsBehorighetsInfo.size());
    assertEquals(5, persistentCache.virtualiseringsInfo.size());

  }

  @Test
  public void persistentTestOnOnlyVagval() throws JAXBException, PersistentCacheException {

    String fileName = String.format("%s/vagvalcache-test.xml", testFolder.getRoot().getPath());

    TakCachePersistentHandler.saveToLocalCache(fileName, VagvalSchemasTestListsUtil.getStaticVagvalList(),
        null);

    PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(fileName);
    assertEquals(5, persistentCache.virtualiseringsInfo.size());
    assertNull( persistentCache.anropsBehorighetsInfo);

  }

  @Test
  public void persistentTestOnOnlyBehorigheter() throws JAXBException, PersistentCacheException {

    String fileName = String.format("%s/behorighetcache-test.xml", testFolder.getRoot().getPath());

    TakCachePersistentHandler.saveToLocalCache(fileName, null, VagvalSchemasTestListsUtil.getStaticBehorighetList());

    PersistentCache persistentCache = TakCachePersistentHandler.restoreFromLocalCache(fileName);
    assertEquals(5, persistentCache.anropsBehorighetsInfo.size());
    assertNull( persistentCache.virtualiseringsInfo);

  }
}