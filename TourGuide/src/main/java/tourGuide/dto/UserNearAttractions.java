package tourGuide.dto;


import gpsUtil.location.Location;

import java.util.List;

public class UserNearAttractions {

    private Location userLocation;
    private List<NearAttraction> listAttractions;

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public List<NearAttraction> getListAttractions() {
        return listAttractions;
    }

    public void setListAttractions(List<NearAttraction> listAttractions) {
        this.listAttractions = listAttractions;
    }
}
