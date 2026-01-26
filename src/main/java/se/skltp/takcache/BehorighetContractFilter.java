package se.skltp.takcache;

import java.util.List;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;

public class BehorighetContractFilter implements BehorighetFilter {

  private final List<String> tjanstegranssnittFilter;

  public BehorighetContractFilter(List<String> tjanstegranssnittFilter) {
    this.tjanstegranssnittFilter = tjanstegranssnittFilter;
  }

  @Override
  public boolean valid(AnropsBehorighetsInfoType anropsBehorighetsInfoType) {
    return tjanstegranssnittFilter.contains(anropsBehorighetsInfoType.getTjansteKontrakt());
  }
}
