package se.skltp.takcache.services;

import java.util.List;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.TakServiceException;

public interface TakService {

    public List<AnropsBehorighetsInfoType> getBehorigheter() throws TakServiceException;

    public List<VirtualiseringsInfoType> getVirtualiseringar() throws TakServiceException;

}
