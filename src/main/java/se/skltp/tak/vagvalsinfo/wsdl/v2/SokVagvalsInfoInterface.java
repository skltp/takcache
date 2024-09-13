package se.skltp.tak.vagvalsinfo.wsdl.v2;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaAnropsBehorigheterResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaTjanstekomponenterResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaTjanstekontraktResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.HamtaAllaVirtualiseringarResponseType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.ObjectFactory;

/**
 * This class was generated by Apache CXF 4.0.5
 * 2024-09-13T10:04:51.469+02:00
 * Generated source version: 4.0.5
 *
 */
@WebService(targetNamespace = "urn:skl:tp:vagvalsinfo:v2", name = "SokVagvalsInfoInterface")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface SokVagvalsInfoInterface {

    @WebMethod
    @WebResult(name = "hamtaAllaTjanstekontraktResponse", targetNamespace = "urn:skl:tp:vagvalsinfo:v2", partName = "response")
    public HamtaAllaTjanstekontraktResponseType hamtaAllaTjanstekontrakt(

        @WebParam(partName = "parameters", name = "hamtaAllaTjanstekontrakt", targetNamespace = "urn:skl:tp:vagvalsinfo:v2")
        Object parameters
    );

    @WebMethod
    @WebResult(name = "hamtaAllaVirtualiseringarResponse", targetNamespace = "urn:skl:tp:vagvalsinfo:v2", partName = "response")
    public HamtaAllaVirtualiseringarResponseType hamtaAllaVirtualiseringar(

        @WebParam(partName = "parameters", name = "hamtaAllaVirtualiseringar", targetNamespace = "urn:skl:tp:vagvalsinfo:v2")
        Object parameters
    );

    @WebMethod
    @WebResult(name = "hamtaAllaTjanstekomponenterResponse", targetNamespace = "urn:skl:tp:vagvalsinfo:v2", partName = "response")
    public HamtaAllaTjanstekomponenterResponseType hamtaAllaTjanstekomponenter(

        @WebParam(partName = "parameters", name = "hamtaAllaTjanstekomponenter", targetNamespace = "urn:skl:tp:vagvalsinfo:v2")
        Object parameters
    );

    @WebMethod
    @WebResult(name = "hamtaAllaAnropsBehorigheterResponse", targetNamespace = "urn:skl:tp:vagvalsinfo:v2", partName = "response")
    public HamtaAllaAnropsBehorigheterResponseType hamtaAllaAnropsBehorigheter(

        @WebParam(partName = "parameters", name = "hamtaAllaAnropsBehorigheter", targetNamespace = "urn:skl:tp:vagvalsinfo:v2")
        Object parameters
    );
}