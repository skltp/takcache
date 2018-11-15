package se.skltp.takcache.behorighet;

import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.takcache.util.XmlGregorianCalendarUtil;

public class BehorighetHandler {

  private PermissionMap permissionMap;
  private List<AnropsBehorighetsInfoType> anropsBehorighetsInfos;

  public BehorighetHandler(List<AnropsBehorighetsInfoType> anropsBehorighetsInfos) {
    if (anropsBehorighetsInfos == null) {
      throw new IllegalArgumentException("Null is not allowed for the parameter anropsBehorighetsInfos");
    }
    this.anropsBehorighetsInfos = anropsBehorighetsInfos;
    this.permissionMap = new PermissionMap(anropsBehorighetsInfos);
  }

  public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
    return isAuthorizedInPermissionMap(senderId, tjanstegranssnitt, receiverAddress);
  }

  public List<AnropsBehorighetsInfoType> getAnropsBehorighetsInfos() {
    return anropsBehorighetsInfos;
  }

  private boolean isAuthorizedInPermissionMap(String senderId, String tjanstegranssnitt,
      String receiverId) {

    List<AnropsBehorighetsInfoType> matchingPermissions = permissionMap
        .lookupInPermissionMap(receiverId, senderId, tjanstegranssnitt);
    if (matchingPermissions == null) {
      return false;
    }

    XMLGregorianCalendar now = XmlGregorianCalendarUtil.getNowAsXMLGregorianCalendar();
    for (AnropsBehorighetsInfoType abi : matchingPermissions) {
      if (XmlGregorianCalendarUtil.isTimeWithinInterval(now, abi.getFromTidpunkt(), abi.getTomTidpunkt())) {
        return true;
      }
    }
    return false;
  }

  public int count() {
    return anropsBehorighetsInfos.size();
  }
}
