package at.hs.campus.wien.sde.urban_cycling_core.util;

public class GeoUtils {
  private static final double EARTH_RADIUS_KM = 6371.0;

  /**
   * Calculates the great-circle distance between two points on Earth
   * using the Haversine formula.
   *
   * <p>Mathematical formula:</p>
   *
   * <pre>
   * Δφ = rad(lat2 - lat1)
   * Δλ = rad(lon2 - lon1)
   *
   * a = sin²(Δφ / 2)
   *     + cos(rad(lat1)) · cos(rad(lat2))
   *     · sin²(Δλ / 2)
   *
   * c = 2 · atan2( √a , √(1 − a) )
   *
   * distance = R · c
   *
   * where:
   * R = 6371 km (Earth radius)
   * </pre>
   *
   * @param lat1 latitude of first point (degrees)
   * @param lon1 longitude of first point (degrees)
   * @param lat2 latitude of second point (degrees)
   * @param lon2 longitude of second point (degrees)
   * @return distance between points in kilometers
   */
  public static double haversine(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }
}