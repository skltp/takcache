package se.skltp.takcache.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skltp.tak.vagvalsinfo.wsdl.v2.*;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TakServiceImpl implements TakService{
    private final static Logger LOGGER = LogManager.getLogger(TakServiceImpl.class);

    public static final String ENDPOINT_ADDRESS_PROPERTY_NAME = "takcache.endpoint.address";
    public static final String ENDPOINT_USER_AGENT_PROPERTY_NAME = "takcache.header.user.agent";

    public static final String VP_HEADER_USER_AGENT_DEFAULT = "SKLTP VP/3.1";


    @Value("${takcache.endpoint.address:null}")
    private String endpointAddressTjanstekatalog;

    @Value("${takcache.header.user.agent:SKLTP VP/3.1}" )
    private String userAgentHeader;

    private SokVagvalsInfoInterface port = null;

    public TakServiceImpl() {
    }

    public List<VirtualiseringsInfoType> getVirtualiseringar() throws Exception {
        List<VirtualiseringsInfoType> virtualiseringsInfoTypes = null;
        try {
            LOGGER.info("Fetch all virtualizations from TAK...");
            HamtaAllaVirtualiseringarResponseType t = getPort().hamtaAllaVirtualiseringar(null);
            virtualiseringsInfoTypes = t.getVirtualiseringsInfo();
            LOGGER.info("Retrieved {} virtualizations from TAK.", virtualiseringsInfoTypes.size());
        } catch (Exception e) {
            LOGGER.error("Unable to get virtualizations from TAK", e);
            throw e;
        }
        return virtualiseringsInfoTypes;
    }

    public List<AnropsBehorighetsInfoType> getBehorigheter() throws Exception {
        List<AnropsBehorighetsInfoType> anropsBehorighetsInfoTypes = null;
        try {
            LOGGER.info("Fetch all permissions from TAK...");
            HamtaAllaAnropsBehorigheterResponseType t = getPort().hamtaAllaAnropsBehorigheter(null);
            anropsBehorighetsInfoTypes = t.getAnropsBehorighetsInfo();
            LOGGER.info("Retrieved {} permissions from TAK.", anropsBehorighetsInfoTypes.size());
        } catch (Exception e) {
            LOGGER.error("Unable to get permissions from TAK", e);
            throw e;
        }
        return anropsBehorighetsInfoTypes;
    }

    private static URL createEndpointUrlFromWsdl(String adressOfWsdl) {
        try {
            return new URL(adressOfWsdl);
        } catch (MalformedURLException e) {
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

            Map<String, Object> req_ctx = ((BindingProvider)port).getRequestContext();
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("User-Agent", Collections.singletonList(userAgentHeader));
            req_ctx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        }
        return port;
    }

    public void setEndpointAddress(String endpointAddressTjanstekatalog) {
        this.endpointAddressTjanstekatalog = endpointAddressTjanstekatalog;
    }

}
