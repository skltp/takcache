package se.skltp.takcache;

public class RoutingInfo {
    private String address;
    private String rivProfile;

    public RoutingInfo() {
        
    }

    public RoutingInfo(String address, String rivProfile) {
        this.address = address;
        this.rivProfile = rivProfile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRivProfile() {
        return rivProfile;
    }

    public void setRivProfile(String rivProfile) {
        this.rivProfile = rivProfile;
    }
}
