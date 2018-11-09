package se.skltp.takcache.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.SokVagvalsInfoInterface;
import se.skltp.tak.vagvalsinfo.wsdl.v2.SokVagvalsServiceSoap11LitDocService;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.TakServiceException;

@Service
public class TakServiceImpl implements TakService{
    private static final Logger LOGGER = LogManager.getLogger(TakServiceImpl.class);

    public static final String ENDPOINT_ADDRESS_PROPERTY_NAME = "takcache.endpoint.address";

    @Value("${takcache.endpoint.address:null}")
    private String endpointAddressTjanstekatalog;

    @Value("${takcache.header.user.agent:SKLTP VP/3.1}" )
    private String userAgentHeader;

    private SokVagvalsInfoInterface port = null;

    public List<VirtualiseringsInfoType> getVirtualiseringar() throws TakServiceException {
        List<VirtualiseringsInfoType> virtualiseringsInfoTypes = null;
        try {
            LOGGER.info("Fetch all virtualizations from TAK...");
            HamtaAllaVirtualiseringarResponseType t = getPort().hamtaAllaVirtualiseringar(null);
            virtualiseringsInfoTypes = t.getVirtualiseringsInfo();
            LOGGER.info("Retrieved {} virtualizations from TAK.", virtualiseringsInfoTypes.size());
        } catch (Exception e) {
            LOGGER.error("Unable to get virtualizations from TAK", e);
            throw new TakServiceException(e);
        }
        return virtualiseringsInfoTypes;
    }

    public List<AnropsBehorighetsInfoType> getBehorigheter() throws TakServiceException {
        List<AnropsBehorighetsInfoType> anropsBehorighetsInfoTypes = null;
        try {
            LOGGER.info("Fetch all permissions from TAK...");
            HamtaAllaAnropsBehorigheterResponseType t = getPort().hamtaAllaAnropsBehorigheter(null);
            anropsBehorighetsInfoTypes = t.getAnropsBehorighetsInfo();
            LOGGER.info("Retrieved {} permissions from TAK.", anropsBehorighetsInfoTypes.size());
        } catch (Exception e) {
            LOGGER.error("Unable to get permissions from TAK", e);
            throw new TakServiceException(e);
        }
        return anropsBehorighetsInfoTypes;
    }

    private static URL createEndpointUrlFromWsdl(String adressOfWsdl) {
        try {
            return new URL(adressOfWsdl);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL to TAK {}",adressOfWsdl);
            throw new RuntimeException(e);
        }
    }

    private static URL createEndpointUrlFromServiceAddress(String serviceAddress) {
        return createEndpointUrlFromWsdl(serviceAddress + "?wsdl");
    }

    private SokVagvalsInfoInterface getPort() {

        if(port == null){
            LOGGER.info("Use TAK endpoint adress: {}", endpointAddressTjanstekatalog);
            if(endpointAddressTjanstekatalog== null || endpointAddressTjanstekatalog.isEmpty()){
                LOGGER.error("No endpoint address for TAK set. Please configure property {}.", ENDPOINT_ADDRESS_PROPERTY_NAME);
            }
            LOGGER.info("User agent header: {}", userAgentHeader);

            SokVagvalsServiceSoap11LitDocService service = new SokVagvalsServiceSoap11LitDocService(
                    createEndpointUrlFromServiceAddress(endpointAddressTjanstekatalog));
            port = service.getSokVagvalsSoap11LitDocPort();

            Map<String, Object> reqCtx = ((BindingProvider)port).getRequestContext();
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("User-Agent", Collections.singletonList(userAgentHeader));
            reqCtx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        }
        return port;
    }

}
