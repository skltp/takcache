package se.skltp.takcache.services;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

import java.util.List;

public interface TakService {

    public List<AnropsBehorighetsInfoType> getBehorigheter() throws Exception;

    public List<VirtualiseringsInfoType> getVirtualiseringar() throws Exception;

}
