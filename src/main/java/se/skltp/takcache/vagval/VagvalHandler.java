package se.skltp.takcache.vagval;

import se.skltp.tak.vagvalsinfo.wsdl.v2.VirtualiseringsInfoType;
import se.skltp.takcache.exceptions.RoutingException;
import se.skltp.takcache.exceptions.RoutingFailReason;
import se.skltp.takcache.util.XmlGregorianCalendarUtil;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VagvalHandler {

    VirtualiseringsInfoMap virtualiseringsInfoMap;

    public VagvalHandler(List<VirtualiseringsInfoType> virtualiseringsInfo) {

        if (virtualiseringsInfo == null) {
            throw new RuntimeException("Null is not allowed for the parameter virtualiseringsInfo");
        }
        virtualiseringsInfoMap = new VirtualiseringsInfoMap(virtualiseringsInfo);
    }

    public String getRoutingAddress(String tjanstegranssnitt, String receiverAddress, String rivProfile) throws RoutingException {

          List<VirtualiseringsInfoType> validVirtualiseringar = getValidVagval(tjanstegranssnitt, receiverAddress);
          if( validVirtualiseringar.isEmpty() ){
              throw new RoutingException(RoutingFailReason.NO_MATCH, "Inget matchande vägval hittat");
          }

          List<VirtualiseringsInfoType> virtualiseringarMatchingRivProfile  = validVirtualiseringar.stream()
                  .filter(virt -> rivProfile.equals(virt.getRivProfil()))
                  .collect(Collectors.toList());

          if(virtualiseringarMatchingRivProfile.isEmpty()){
              throw new RoutingException(RoutingFailReason.NO_MATCHING_RIV_PROFILE, "Ingen matchande riv-profil");
          }else if(virtualiseringarMatchingRivProfile.size() > 1){
              throw new RoutingException(RoutingFailReason.MULTIPLE_MATCHES, "Flera matchande vägval");
          }

          return virtualiseringarMatchingRivProfile.get(0).getAdress();
    }

    public List<String> getRoutingRivProfiles(String tjanstegranssnitt, String receiverAddress) {
        List<VirtualiseringsInfoType> validVirtualiseringar = getValidVagval(tjanstegranssnitt, receiverAddress);
        return validVirtualiseringar.stream().map(VirtualiseringsInfoType::getRivProfil).collect(Collectors.toList());
    }

    private List<VirtualiseringsInfoType> getValidVagval(String tjanstegranssnitt, String receiverAddress) {

        List<VirtualiseringsInfoType> validVirtualiseringar = new ArrayList<>();

        List<VirtualiseringsInfoType> matchingVirtualiseringsInfo = virtualiseringsInfoMap.lookupInVirtualiseringsInfoMap(receiverAddress, tjanstegranssnitt);
        if (matchingVirtualiseringsInfo == null) {
            return validVirtualiseringar;
        }

        XMLGregorianCalendar now = XmlGregorianCalendarUtil.getNowAsXMLGregorianCalendar();
        for (VirtualiseringsInfoType vi : matchingVirtualiseringsInfo) {
            if( XmlGregorianCalendarUtil.isTimeWithinInterval(now, vi.getFromTidpunkt(), vi.getTomTidpunkt()) ){
                validVirtualiseringar.add(vi);
            }
        }

        return validVirtualiseringar;
    }


}
