package tourGuide.service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateRewards(User user) {
		//List<VisitedLocation> userLocations = user.getVisitedLocations();
		//List<Attraction> attractions = gpsUtil.getAttractions();

		ExecutorService executorService = Executors.newFixedThreadPool(200);
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
				List<Attraction> attractions = new CopyOnWriteArrayList<>(gpsUtil.getAttractions());




				for(VisitedLocation visitedLocation : userLocations) {
					for(Attraction attraction : attractions) {
						if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
							if(nearAttraction(visitedLocation, attraction)) {
								user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
							}
						}
					}
				}

			}
		});

		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}


	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

	public List<Attraction> near5Attractions(VisitedLocation visitedLocation) {
		List<Attraction> list5Attractions = new ArrayList<>();
		//Attraction[] list5Attractions = new Attraction[]{gpsUtil.getAttractions().get(0), gpsUtil.getAttractions().get(1), gpsUtil.getAttractions().get(2), gpsUtil.getAttractions().get(3), gpsUtil.getAttractions().get(4)};

		/*for(Attraction attraction : gpsUtil.getAttractions()) {
			for(int j = 0; j < 5;j++){


			}
		}*/

		list5Attractions = gpsUtil.getAttractions();

		list5Attractions = list5Attractions.stream().sorted(Comparator.comparing(attraction -> getDistance(visitedLocation.location,attraction))).collect(Collectors.toList());


		for(int i = 0;list5Attractions.size() > 5;i++){
			list5Attractions.remove(list5Attractions.size()-1);
		}

		//List<Attraction> goodList = Arrays.asList(list5Attractions);

		return list5Attractions;
	}
}
