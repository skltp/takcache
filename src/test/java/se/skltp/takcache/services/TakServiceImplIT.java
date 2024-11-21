package se.skltp.takcache.services;

import static org.junit.Assert.*;
import static se.skltp.takcache.services.TakServiceImpl.ENDPOINT_ADDRESS_PROPERTY_NAME;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.TestSocketUtils;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.exceptions.TakServiceException;
import se.skltp.takcache.util.SokVagvalsInfoMockWebService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TakServiceImplIT {

  public static final String TAK_SOKVALSINFO_ADDRESS = "http://localhost:%d/sokvagvalsinfo";

  private static SokVagvalsInfoMockWebService mockWebService;

  @Autowired
  TakService takService;

  @BeforeClass
  public static void before() {
    String address = String.format(TAK_SOKVALSINFO_ADDRESS, TestSocketUtils.findAvailableTcpPort());
    System.setProperty(ENDPOINT_ADDRESS_PROPERTY_NAME, address);
    mockWebService = new SokVagvalsInfoMockWebService(address);
  }

   @Test
  public void getVirtualiseringarTest() throws Exception {
    mockWebService.start();
    List<VirtualiseringsInfoType> virtualiseringar = takService.getVirtualiseringar();
    assertEquals(5, virtualiseringar.size());
    mockWebService.stop();
  }

  @Test
  public void getBehorigheterTest() throws Exception {
    mockWebService.start();
    List<AnropsBehorighetsInfoType> behorigheter = takService.getBehorigheter();
    assertEquals(5, behorigheter.size());
    mockWebService.stop();
  }

  @Test(expected = TakServiceException.class)
  public void getBehorigheterNoContactWithServerShouldThrowException() throws Exception {
    takService.getBehorigheter();
  }

  @Test(expected = TakServiceException.class)
  public void getVirtualiseringarNoContactWithServerShouldThrowException() throws Exception {
    takService.getVirtualiseringar();
  }
}