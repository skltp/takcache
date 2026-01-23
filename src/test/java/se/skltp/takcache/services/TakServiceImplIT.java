package se.skltp.takcache.services;

import static org.junit.jupiter.api.Assertions.*;
import static se.skltp.takcache.services.TakServiceImpl.ENDPOINT_ADDRESS_PROPERTY_NAME;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.TestSocketUtils;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.TakServiceException;
import se.skltp.takcache.util.SokVagvalsInfoMockWebService;

@SpringJUnitConfig(locations = "classpath*:spring-context.xml")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class TakServiceImplIT {

  public static final String TAK_SOKVALSINFO_ADDRESS = "http://localhost:%d/sokvagvalsinfo";

  private static SokVagvalsInfoMockWebService mockWebService;
  private static String originalEndpointProperty;

  @Autowired
  TakService takService;

  @BeforeAll
  static void before() {
    // Save original property value
    originalEndpointProperty = System.getProperty(ENDPOINT_ADDRESS_PROPERTY_NAME);

    String address = String.format(TAK_SOKVALSINFO_ADDRESS, TestSocketUtils.findAvailableTcpPort());
    System.setProperty(ENDPOINT_ADDRESS_PROPERTY_NAME, address);
    mockWebService = new SokVagvalsInfoMockWebService(address);
  }

  @AfterAll
  static void afterAll() {
    // Clean up system property
    if (originalEndpointProperty == null) {
      System.clearProperty(ENDPOINT_ADDRESS_PROPERTY_NAME);
    } else {
      System.setProperty(ENDPOINT_ADDRESS_PROPERTY_NAME, originalEndpointProperty);
    }

    // Ensure mock web service is stopped
    if (mockWebService != null) {
      try {
        mockWebService.stop();
      } catch (Exception e) {
        // Ignore if already stopped
      }
    }
  }

  @AfterEach
  void afterEach() {
    // Ensure the mock web service is stopped after each test to prevent interference
    try {
      mockWebService.stop();
    } catch (Exception e) {
      // Ignore if already stopped
    }
  }

   @Test
   void getVirtualiseringarTest() throws Exception {
    mockWebService.start();
    List<VirtualiseringsInfoType> virtualiseringar = takService.getVirtualiseringar();
    assertEquals(5, virtualiseringar.size());
  }

  @Test
  void getBehorigheterTest() throws Exception {
    mockWebService.start();
    List<AnropsBehorighetsInfoType> behorigheter = takService.getBehorigheter();
    assertEquals(5, behorigheter.size());
  }

  @Test
  void getBehorigheterNoContactWithServerShouldThrowException() {
    assertThrows(TakServiceException.class, () -> takService.getBehorigheter());
  }

  @Test
  void getVirtualiseringarNoContactWithServerShouldThrowException() {
    assertThrows(TakServiceException.class, () -> takService.getVirtualiseringar());
  }
}