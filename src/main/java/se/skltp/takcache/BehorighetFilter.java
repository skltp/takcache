package se.skltp.takcache;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

public interface BehorighetFilter {

  boolean valid(AnropsBehorighetsInfoType anropsBehorighetsInfoType);

}
