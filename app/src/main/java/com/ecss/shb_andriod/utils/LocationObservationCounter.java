package com.ecss.shb_andriod.utils;

import java.util.HashMap;
import java.util.Map;

public class LocationObservationCounter {
    public static Map<String, Integer> countObservationsByLocation(java.util.List<com.ecss.shb_andriod.model.Survey> surveys) {
        Map<String, Integer> locationCounts = new HashMap<>();
        for (com.ecss.shb_andriod.model.Survey survey : surveys) {
            String location = survey.getLocation(); // Assumes Survey has getLocation()
            if (location == null) location = "Unknown";
            locationCounts.put(location, locationCounts.getOrDefault(location, 0) + 1);
        }
        return locationCounts;
    }
}
