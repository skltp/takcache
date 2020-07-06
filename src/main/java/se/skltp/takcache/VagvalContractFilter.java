package se.skltp.takcache;

import java.util.List;
import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;

public class VagvalContractFilter implements VagvalFilter {

  public VagvalContractFilter(List<String> tjanstegranssnittFilter) {
    this.tjanstegranssnittFilter = tjanstegranssnittFilter;
  }

  public List<String> tjanstegranssnittFilter;

  @Override
  public boolean valid(VirtualiseringsInfoType virtualiseringsInfoType) {
    return tjanstegranssnittFilter.contains(virtualiseringsInfoType.getTjansteKontrakt());
  }

}
