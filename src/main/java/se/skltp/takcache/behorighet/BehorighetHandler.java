package se.skltp.takcache.behorighet;

import se.skltp.tak.vagvalsinfo.wsdl.v2.AnropsBehorighetsInfoType;
import se.skltp.takcache.util.XmlGregorianCalendarUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class BehorighetHandler {

	private PermissionMap permissionMap;

	public BehorighetHandler(List<AnropsBehorighetsInfoType> permissions) {
		if (permissions == null) {
			throw new IllegalArgumentException("Null is not allowed for the parameter permissions");
		}
		this.permissionMap = new PermissionMap(permissions);
	}

	public boolean isAuthorized(String senderId, String tjanstegranssnitt, String receiverAddress) {
        return isAuthorizedInPermissionMap(senderId, tjanstegranssnitt, receiverAddress);
	}

	private boolean isAuthorizedInPermissionMap(String senderId , String tjanstegranssnitt, String receiverId) {

		List<AnropsBehorighetsInfoType> matchingPermissions = permissionMap.lookupInPermissionMap(receiverId, senderId, tjanstegranssnitt);
		if (matchingPermissions == null){
			return false;
		}

		XMLGregorianCalendar now = XmlGregorianCalendarUtil.getNowAsXMLGregorianCalendar();
		for (AnropsBehorighetsInfoType abi : matchingPermissions) {
            if( XmlGregorianCalendarUtil.isTimeWithinInterval(now, abi.getFromTidpunkt(),  abi.getTomTidpunkt()) ){
                return true;
			}
		}
		return false;
	}




}
