package com.women.safety.features.liveLocationTracking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenStreetMap Service - 100% FREE Location Services
 * No API key required, No credit card needed
 *
 * Features:
 * - Reverse Geocoding (coordinates → address)
 * - Forward Geocoding (address → coordinates)
 * - Distance Calculation
 * - Nearby Places Search (police, hospitals, etc.)
 * - Built-in caching to reduce API calls
 */
@Service
public class OpenStreetMapService {

    private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapService.class);

    // FREE APIs - No authentication required
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org";
    private static final String OVERPASS_API = "https://overpass-api.de/api/interpreter";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Simple in-memory cache (1 hour expiration)
    private final Map<String, CachedResult> geocodeCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 3600000; // 1 hour

    public OpenStreetMapService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ==================== Geocoding - 100% FREE ====================

    /**
     * Get address from coordinates (Reverse Geocoding)
     * FREE - No API key needed
     */
    public GeocodeResult getAddressFromCoordinates(double latitude, double longitude) {
        // Round to 4 decimal places (~11 meters accuracy) for caching
        String cacheKey = String.format("rev_%.4f_%.4f", latitude, longitude);

        // Check cache first
        GeocodeResult cached = getCachedResult(cacheKey);
        if (cached != null) {
            logger.debug("Returning cached address for: {}, {}", latitude, longitude);
            return cached;
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(NOMINATIM_API + "/reverse")
                    .queryParam("format", "json")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("addressdetails", 1)
                    .queryParam("zoom", 18)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "WomenSafetyApp/1.0 (Emergency Safety Application)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody();
            JsonNode root = objectMapper.readTree(response);

            GeocodeResult result = new GeocodeResult();
            result.setLatitude(latitude);
            result.setLongitude(longitude);
            result.setFormattedAddress(root.get("display_name").asText());

            // Parse detailed address components
            if (root.has("address")) {
                JsonNode address = root.get("address");
                if (address.has("road")) result.setStreet(address.get("road").asText());
                if (address.has("house_number")) result.setStreetNumber(address.get("house_number").asText());
                if (address.has("city")) result.setCity(address.get("city").asText());
                if (address.has("town")) result.setCity(address.get("town").asText());
                if (address.has("village")) result.setCity(address.get("village").asText());
                if (address.has("state")) result.setState(address.get("state").asText());
                if (address.has("country")) result.setCountry(address.get("country").asText());
                if (address.has("country_code")) result.setCountryCode(address.get("country_code").asText().toUpperCase());
                if (address.has("postcode")) result.setPostalCode(address.get("postcode").asText());
            }

            logger.info("OpenStreetMap reverse geocoding successful: {}, {} → {}",
                    latitude, longitude, result.getFormattedAddress());

            // Cache the result
            cacheResult(cacheKey, result);

            // Rate limiting - wait 1 second (OpenStreetMap requirement)
            Thread.sleep(1000);

            return result;

        } catch (Exception e) {
            logger.error("Error in reverse geocoding: {}", e.getMessage());
            // Return basic result with just coordinates
            GeocodeResult fallback = new GeocodeResult();
            fallback.setLatitude(latitude);
            fallback.setLongitude(longitude);
            fallback.setFormattedAddress(String.format("Location: %.4f, %.4f", latitude, longitude));
            return fallback;
        }
    }

    /**
     * Get coordinates from address (Forward Geocoding)
     * FREE - No API key needed
     */
    public GeocodeResult getCoordinatesFromAddress(String address) {
        String cacheKey = "fwd_" + address.toLowerCase().replaceAll("\\s+", "_");

        // Check cache first
        GeocodeResult cached = getCachedResult(cacheKey);
        if (cached != null) {
            logger.debug("Returning cached coordinates for: {}", address);
            return cached;
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(NOMINATIM_API + "/search")
                    .queryParam("format", "json")
                    .queryParam("q", address)
                    .queryParam("addressdetails", 1)
                    .queryParam("limit", 1)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "WomenSafetyApp/1.0 (Emergency Safety Application)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody();
            JsonNode root = objectMapper.readTree(response);

            if (root.isArray() && root.size() > 0) {
                JsonNode firstResult = root.get(0);

                GeocodeResult result = new GeocodeResult();
                result.setLatitude(Double.parseDouble(firstResult.get("lat").asText()));
                result.setLongitude(Double.parseDouble(firstResult.get("lon").asText()));
                result.setFormattedAddress(firstResult.get("display_name").asText());

                // Parse address details
                if (firstResult.has("address")) {
                    JsonNode addressNode = firstResult.get("address");
                    if (addressNode.has("road")) result.setStreet(addressNode.get("road").asText());
                    if (addressNode.has("city")) result.setCity(addressNode.get("city").asText());
                    if (addressNode.has("state")) result.setState(addressNode.get("state").asText());
                    if (addressNode.has("country")) result.setCountry(addressNode.get("country").asText());
                    if (addressNode.has("postcode")) result.setPostalCode(addressNode.get("postcode").asText());
                }

                logger.info("OpenStreetMap forward geocoding successful: {} → {}, {}",
                        address, result.getLatitude(), result.getLongitude());

                // Cache the result
                cacheResult(cacheKey, result);

                Thread.sleep(1000); // Rate limiting
                return result;
            }

            logger.warn("⚠No results found for address: {}", address);
            return null;

        } catch (Exception e) {
            logger.error("Error in forward geocoding: {}", e.getMessage());
            return null;
        }
    }

    // ==================== Distance & Navigation - FREE ====================

    /**
     * Calculate distance between two points using Haversine formula
     * 100% FREE - No API needed, pure math
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth's radius in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in meters
    }

    /**
     * Calculate distance with human-readable text
     */
    public DistanceResult getDistanceInfo(double lat1, double lon1, double lat2, double lon2) {
        double distanceMeters = calculateDistance(lat1, lon1, lat2, lon2);

        DistanceResult result = new DistanceResult();
        result.setDistanceMeters(distanceMeters);

        // Format distance text
        if (distanceMeters < 1000) {
            result.setDistanceText(String.format("%.0f m", distanceMeters));
        } else {
            result.setDistanceText(String.format("%.1f km", distanceMeters / 1000));
        }

        // Estimate walking time (average 5 km/h = 1.4 m/s)
        long walkingSeconds = (long) (distanceMeters / 1.4);
        result.setWalkingTimeSeconds(walkingSeconds);
        result.setWalkingTimeText(formatDuration(walkingSeconds));

        // Estimate driving time (average 40 km/h in city = 11.1 m/s)
        long drivingSeconds = (long) (distanceMeters / 11.1);
        result.setDrivingTimeSeconds(drivingSeconds);
        result.setDrivingTimeText(formatDuration(drivingSeconds));

        return result;
    }

    // ==================== Nearby Places - FREE ====================

    /**
     * Find nearby places using Overpass API (OpenStreetMap data)
     * FREE - No API key needed
     *
     * Common amenity types: police, hospital, pharmacy, fire_station,
     * clinic, doctors, bank, atm, fuel, parking
     */
    public List<NearbyPlace> findNearbyPlaces(double latitude, double longitude,
                                              String amenity, int radiusMeters) {
        try {
            // Overpass QL query
            String query = String.format(
                    "[out:json][timeout:25];(node[\"amenity\"=\"%s\"](around:%d,%.6f,%.6f););out body;",
                    amenity, radiusMeters, latitude, longitude
            );

            URI uri = UriComponentsBuilder.fromHttpUrl(OVERPASS_API)
                    .queryParam("data", query)
                    .build()
                    .encode()
                    .toUri();

            logger.debug("Searching for nearby {} within {}m of {}, {}", amenity, radiusMeters, latitude, longitude);

            String response = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(response);

            List<NearbyPlace> places = new ArrayList<>();

            if (root.has("elements")) {
                for (JsonNode element : root.get("elements")) {
                    NearbyPlace place = new NearbyPlace();
                    place.setLatitude(element.get("lat").asDouble());
                    place.setLongitude(element.get("lon").asDouble());
                    place.setAmenityType(amenity);

                    // Parse tags
                    if (element.has("tags")) {
                        JsonNode tags = element.get("tags");
                        if (tags.has("name")) {
                            place.setName(tags.get("name").asText());
                        } else {
                            place.setName(amenity.substring(0, 1).toUpperCase() + amenity.substring(1));
                        }

                        // Build address from available tags
                        StringBuilder address = new StringBuilder();
                        if (tags.has("addr:street")) address.append(tags.get("addr:street").asText()).append(", ");
                        if (tags.has("addr:city")) address.append(tags.get("addr:city").asText());
                        place.setAddress(address.toString());

                        if (tags.has("phone")) place.setPhone(tags.get("phone").asText());
                        if (tags.has("opening_hours")) place.setOpeningHours(tags.get("opening_hours").asText());
                    }

                    // Calculate distance from user
                    double distance = calculateDistance(latitude, longitude,
                            place.getLatitude(), place.getLongitude());
                    place.setDistanceMeters(distance);
                    place.setDistanceText(distance < 1000 ?
                            String.format("%.0f m", distance) :
                            String.format("%.1f km", distance / 1000));

                    places.add(place);
                }
            }

            // Sort by distance (closest first)
            places.sort((a, b) -> Double.compare(a.getDistanceMeters(), b.getDistanceMeters()));

            logger.info("Found {} nearby {} within {}m", places.size(), amenity, radiusMeters);
            return places;

        } catch (Exception e) {
            logger.error("Error finding nearby {}: {}", amenity, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Find nearest police station - FREE
     */
    public NearbyPlace findNearestPoliceStation(double latitude, double longitude) {
        List<NearbyPlace> stations = findNearbyPlaces(latitude, longitude, "police", 5000);
        if (stations.isEmpty()) {
            logger.warn("No police station found within 5km of {}, {}", latitude, longitude);
            return null;
        }
        logger.info("Nearest police station: {} ({} away)",
                stations.get(0).getName(), stations.get(0).getDistanceText());
        return stations.get(0);
    }

    /**
     * Find nearest hospital - FREE
     */
    public NearbyPlace findNearestHospital(double latitude, double longitude) {
        List<NearbyPlace> hospitals = findNearbyPlaces(latitude, longitude, "hospital", 5000);
        if (hospitals.isEmpty()) {
            // Try clinics if no hospitals found
            hospitals = findNearbyPlaces(latitude, longitude, "clinic", 5000);
        }
        if (hospitals.isEmpty()) {
            logger.warn("No hospital found within 5km of {}, {}", latitude, longitude);
            return null;
        }
        logger.info("Nearest hospital: {} ({} away)",
                hospitals.get(0).getName(), hospitals.get(0).getDistanceText());
        return hospitals.get(0);
    }

    /**
     * Find nearest pharmacy - FREE
     */
    public NearbyPlace findNearestPharmacy(double latitude, double longitude) {
        List<NearbyPlace> pharmacies = findNearbyPlaces(latitude, longitude, "pharmacy", 3000);
        return pharmacies.isEmpty() ? null : pharmacies.get(0);
    }

    // ==================== Map URLs - FREE ====================

    /**
     * Generate OpenStreetMap URL for viewing location
     */
    public String generateMapUrl(double latitude, double longitude) {
        return String.format("https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=18/%.6f/%.6f",
                latitude, longitude, latitude, longitude);
    }

    /**
     * Generate directions URL
     */
    public String generateDirectionsUrl(double fromLat, double fromLon, double toLat, double toLon) {
        return String.format("https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=%.6f%%2C%.6f%%3B%.6f%%2C%.6f",
                fromLat, fromLon, toLat, toLon);
    }

    // ==================== Utility Methods ====================

    /**
     * Format duration in human-readable format
     */
    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " sec";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " min";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }

    /**
     * Cache management
     */
    private void cacheResult(String key, GeocodeResult result) {
        geocodeCache.put(key, new CachedResult(result, System.currentTimeMillis()));

        // Clean cache asynchronously after expiration
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(CACHE_DURATION_MS);
                geocodeCache.remove(key);
                logger.debug("Cache expired for key: {}", key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private GeocodeResult getCachedResult(String key) {
        CachedResult cached = geocodeCache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.result;
        }
        return null;
    }

    // ==================== Result Classes ====================

    public static class GeocodeResult {
        private Double latitude;
        private Double longitude;
        private String formattedAddress;
        private String streetNumber;
        private String street;
        private String city;
        private String state;
        private String country;
        private String countryCode;
        private String postalCode;

        // Getters and setters
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }

        public String getStreetNumber() { return streetNumber; }
        public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    }

    public static class DistanceResult {
        private Double distanceMeters;
        private String distanceText;
        private Long walkingTimeSeconds;
        private String walkingTimeText;
        private Long drivingTimeSeconds;
        private String drivingTimeText;

        // Getters and setters
        public Double getDistanceMeters() { return distanceMeters; }
        public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

        public String getDistanceText() { return distanceText; }
        public void setDistanceText(String distanceText) { this.distanceText = distanceText; }

        public Long getWalkingTimeSeconds() { return walkingTimeSeconds; }
        public void setWalkingTimeSeconds(Long walkingTimeSeconds) { this.walkingTimeSeconds = walkingTimeSeconds; }

        public String getWalkingTimeText() { return walkingTimeText; }
        public void setWalkingTimeText(String walkingTimeText) { this.walkingTimeText = walkingTimeText; }

        public Long getDrivingTimeSeconds() { return drivingTimeSeconds; }
        public void setDrivingTimeSeconds(Long drivingTimeSeconds) { this.drivingTimeSeconds = drivingTimeSeconds; }

        public String getDrivingTimeText() { return drivingTimeText; }
        public void setDrivingTimeText(String drivingTimeText) { this.drivingTimeText = drivingTimeText; }
    }

    public static class NearbyPlace {
        private String name;
        private String amenityType;
        private String address;
        private String phone;
        private String openingHours;
        private Double latitude;
        private Double longitude;
        private Double distanceMeters;
        private String distanceText;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAmenityType() { return amenityType; }
        public void setAmenityType(String amenityType) { this.amenityType = amenityType; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getOpeningHours() { return openingHours; }
        public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public Double getDistanceMeters() { return distanceMeters; }
        public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

        public String getDistanceText() { return distanceText; }
        public void setDistanceText(String distanceText) { this.distanceText = distanceText; }
    }

    // Cache wrapper class
    private static class CachedResult {
        final GeocodeResult result;
        final long timestamp;

        CachedResult(GeocodeResult result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}
