package se.skltp.takcache;

import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

public interface VagvalFilter {

  boolean valid(VirtualiseringsInfoType virtualiseringsInfoType);

}
