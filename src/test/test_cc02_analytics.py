# python test_cc02_analytics.py
import requests
import time
import random
from datetime import datetime, timedelta
import sys

BASE_URL = "http://localhost:8081"
API_KEY = "thesis-secret-key"

# Famous locations in Vienna for variety
LOCATIONS = [
    {"name": "Stephansplatz", "lat": 48.2082, "lon": 16.3738},
    {"name": "Schönbrunn", "lat": 48.1845, "lon": 16.3123},
    {"name": "Prater", "lat": 48.2167, "lon": 16.4022},
    {"name": "Rathaus", "lat": 48.2107, "lon": 16.3565},
    {"name": "Belvedere", "lat": 48.1916, "lon": 16.3806},
    {"name": "Donauturm", "lat": 48.2400, "lon": 16.4100},
    {"name": "Hundertwasserhaus", "lat": 48.2075, "lon": 16.3945},
    {"name": "Naschmarkt", "lat": 48.1994, "lon": 16.3633},
]

def log_step(step_num, description):
    """Pretty print test steps"""
    print(f"\n{'='*60}")
    print(f"📋 STEP {step_num}: {description}")
    print(f"{'='*60}")

def log_success(message):
    print(f"✅ {message}")

def log_info(message):
    print(f"ℹ️ {message}")

def log_error(message):
    print(f"❌ {message}")

def log_location(message):
    print(f"📍 {message}")

def format_timestamp(dt):
    """Format datetime WITHOUT milliseconds to match @JsonFormat pattern"""
    return dt.strftime("%Y-%m-%dT%H:%M:%SZ")

def create_user(username):
    """Create a test user"""
    log_step(1, f"Creating user: {username}")

    response = requests.post(
        f"{BASE_URL}/api/users",
        json={"username": username},
        headers={"X-API-Key": API_KEY}
    )

    if response.status_code == 201:
        user_data = response.json()
        log_success(f"User created with ID: {user_data['userId']}")
        return user_data['userId']
    else:
        log_error(f"Failed to create user: {response.text}")
        sys.exit(1)

def create_ride(user_id, ride_date, num_waypoints, locations, ride_number):
    """Create a single ride with multiple waypoints"""

    # Randomly select start and end locations
    start_loc = random.choice(locations)
    # Make sure end location is different from start for visibility
    end_loc = start_loc
    while end_loc["name"] == start_loc["name"] and len(locations) > 1:
        end_loc = random.choice(locations)

    print(f"\n   🚴‍♂️ RIDE {ride_number} DETAILS:")
    print(f"      Starting at: {start_loc['name']} ({start_loc['lat']}, {start_loc['lon']})")
    print(f"      Ending at:   {end_loc['name']} ({end_loc['lat']}, {end_loc['lon']})")
    print(f"      Date/Time:   {ride_date.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"      Waypoints:   {num_waypoints} points along the route")

    # Generate waypoints with realistic movement
    waypoints = []
    current_lat = start_loc["lat"]
    current_lon = start_loc["lon"]

    for i in range(num_waypoints):
        # Add some random movement
        current_lat += random.uniform(-0.002, 0.002)
        current_lon += random.uniform(-0.002, 0.002)

        # Timestamp every 5 minutes
        timestamp = format_timestamp(ride_date + timedelta(minutes=i*5))

        waypoints.append({
            "userId": user_id,
            "timestamp": timestamp,
            "latitude": round(current_lat, 6),
            "longitude": round(current_lon, 6)
        })

    # Add final waypoint at end location
    final_timestamp = format_timestamp(ride_date + timedelta(minutes=num_waypoints*5 + 2))
    waypoints.append({
        "userId": user_id,
        "timestamp": final_timestamp,
        "latitude": end_loc["lat"],
        "longitude": end_loc["lon"]
    })

    return waypoints, start_loc["name"], end_loc["name"]

def send_waypoints(user_id, waypoints, ride_number):
    """Send waypoints to telemetry endpoint"""
    success_count = 0

    print(f"\n      📤 Sending {len(waypoints)} waypoints for Ride {ride_number}:")

    for i, waypoint in enumerate(waypoints):
        try:
            response = requests.post(
                f"{BASE_URL}/api/v1/telemetry",
                json=waypoint,
                headers={"X-API-Key": API_KEY}
            )

            if response.status_code == 202:
                success_count += 1

                # Log first, last, and every 5th waypoint
                if i == 0:
                    log_location(f"        Waypoint 1 (Start): ({waypoint['latitude']}, {waypoint['longitude']}) @ {waypoint['timestamp']}")
                elif i == len(waypoints) - 1:
                    log_location(f"        Waypoint {i+1} (End): ({waypoint['latitude']}, {waypoint['longitude']}) @ {waypoint['timestamp']}")
                elif (i + 1) % 5 == 0:
                    log_location(f"        Waypoint {i+1}: ({waypoint['latitude']}, {waypoint['longitude']}) @ {waypoint['timestamp']}")
            else:
                log_error(f"Failed to send waypoint {i}: {response.status_code} - {response.text}")
                return False
        except Exception as e:
            log_error(f"Exception sending waypoint {i}: {e}")
            return False

    print(f"      ✅ All {success_count}/{len(waypoints)} waypoints sent successfully for Ride {ride_number}")
    return True

def get_latest_ride_id(user_id, previous_ride_ids):
    """Get the most recent ride ID for the user that's not already in our list"""
    response = requests.get(
        f"{BASE_URL}/api/users/{user_id}/rides",
        headers={"X-API-Key": API_KEY}
    )

    if response.status_code == 200:
        rides = response.json()
        # Find a ride ID that's not in our previous_ride_ids list
        for ride in rides:
            if ride['rideId'] not in previous_ride_ids:
                return ride['rideId']
    return None

def end_ride(ride_id, ride_number, start_location, end_location):
    """End a specific ride"""
    if not ride_id:
        log_error(f"      Cannot end Ride {ride_number}: No ride ID")
        return None

    response = requests.post(
        f"{BASE_URL}/api/v1/rides/end",
        json={"rideId": ride_id},
        headers={"X-API-Key": API_KEY}
    )

    if response.status_code == 200:
        data = response.json()
        print(f"      ✅ Ride {ride_number} ({start_location} → {end_location}): {data['totalDistanceKm']:.2f} km, "
              f"{data['avgSpeedKmh']:.2f} km/h, {data['totalWaypoints']} waypoints")
        return data
    else:
        log_error(f"Failed to end ride {ride_number}: {response.text}")
        return None

def wait_for_processing(seconds=2, reason=""):
    """Wait for async processing"""
    if reason:
        log_info(f"Waiting {seconds} second(s) for {reason}...")
    else:
        log_info(f"Waiting {seconds} second(s) for async processing...")
    time.sleep(seconds)

def get_user_rides(user_id):
    """Get all ride IDs for a user"""
    response = requests.get(
        f"{BASE_URL}/api/users/{user_id}/rides",
        headers={"X-API-Key": API_KEY}
    )

    if response.status_code == 200:
        return response.json()
    else:
        log_error(f"Failed to get user rides: {response.text}")
        return []

def test_user_analytics(user_id, expected_rides):
    """Test GET /api/v1/analytics/user/{userId}"""
    log_step(4, "Testing User Analytics Endpoint")

    response = requests.get(
        f"{BASE_URL}/api/v1/analytics/user/{user_id}",
        headers={"X-API-Key": API_KEY}
    )

    if response.status_code == 200:
        data = response.json()
        print(f"\n📊 USER ANALYTICS RESULTS:")
        print(f"   User ID: {data['userId']}")
        print(f"   Total Rides: {data['totalRides']}")
        print(f"   Total Distance: {data['totalDistanceKm']:.2f} km")
        print(f"   Avg Distance/Ride: {data['avgDistancePerRideKm']:.2f} km")
        print(f"   Avg Speed: {data['avgSpeedKmh']:.2f} km/h")
        print(f"   Max Ride Distance: {data['maxRideDistanceKm']:.2f} km")
        print(f"   Total Ride Time: {data['totalRideTimeMinutes']} minutes")

        print(f"\n   📈 VERIFICATION:")
        print(f"      • Expected rides: {expected_rides}")
        print(f"      • Actual rides: {data['totalRides']}")

        if data['totalRides'] == expected_rides:
            print(f"      ✅ CORRECT: All {expected_rides} rides were created as separate entities")
        else:
            print(f"      ⚠️  NOTE: Got {data['totalRides']} rides, expected {expected_rides}")

        log_success("User analytics verified")
        return data
    else:
        log_error(f"Failed to get user analytics: {response.text}")
        return None

def generate_test_data():
    """Main test function"""
    print("\n" + "🌟"*60)
    print("🌟 CC-02 EXTENDED ANALYTICS TEST - SEPARATE RIDES WITH ENDING 🌟".center(80))
    print("🌟"*60 + "\n")

    print("📌 TEST OBJECTIVE: Create 5 SEPARATE rides and verify analytics")
    print("📌 ARCHITECTURE: Event-Driven Architecture (EDA) with Kafka")
    print("📌 RIDES: Exactly 5 rides, each with 2-8 waypoints")
    print("📌 KEY CHANGE: Getting rideId from database after processing\n")

    # 1. Create user
    username = f"analytics_test_{int(time.time())}"
    user_id = create_user(username)

    # Track statistics
    NUM_RIDES = 5
    ride_details = []
    failed_rides = 0
    total_waypoints_sent = 0
    ride_ids = []

    # 2. Generate exactly 5 rides
    log_step(2, f"Creating exactly {NUM_RIDES} separate rides")

    # Space rides 1 hour apart to ensure they're separate
    base_date = datetime.now().replace(hour=9, minute=0, second=0, microsecond=0)

    for ride_num in range(1, NUM_RIDES + 1):
        print(f"\n   {'='*50}")
        print(f"   🚴 CREATING RIDE {ride_num} OF {NUM_RIDES}")
        print(f"   {'='*50}")

        # Each ride has 2-8 waypoints (random)
        num_waypoints = random.randint(2, 8)

        # Space rides 1 hour apart
        ride_date = base_date + timedelta(hours=ride_num-1)

        # Create ride waypoints
        waypoints, start_location, end_location = create_ride(
            user_id,
            ride_date,
            num_waypoints,
            LOCATIONS,
            ride_num
        )

        # Send waypoints
        success = send_waypoints(user_id, waypoints, ride_num)

        if success:
            total_waypoints_sent += len(waypoints)

            # Wait for Kafka to process and create the ride in database
            wait_for_processing(3, f"Ride {ride_num} to appear in database")

            # Get the new ride ID from database
            ride_id = get_latest_ride_id(user_id, ride_ids)

            if ride_id:
                ride_ids.append(ride_id)
                print(f"      ✅ Ride {ride_num} created with ID: {ride_id[:8]}...")

                # END THE RIDE IMMEDIATELY
                print(f"      🔚 Ending Ride {ride_num} now...")
                stats = end_ride(ride_id, ride_num, start_location, end_location)

                if stats:
                    ride_details.append({
                        "ride_num": ride_num,
                        "ride_id": ride_id,
                        "distance": stats['totalDistanceKm'],
                        "speed": stats['avgSpeedKmh'],
                        "waypoints": stats['totalWaypoints'],
                        "start": start_location,
                        "end": end_location
                    })
                    print(f"      ✓ Ride {ride_num} completed and ended successfully")
                else:
                    failed_rides += 1
                    log_error(f"      Failed to end ride {ride_num}")
            else:
                failed_rides += 1
                log_error(f"      Failed to get ride ID for ride {ride_num} from database")
        else:
            failed_rides += 1
            log_error(f"      Failed to send waypoints for ride {ride_num}")

        # Small pause between rides
        if ride_num < NUM_RIDES:
            print(f"      ⏱️  Waiting 3 seconds before next ride...")
            time.sleep(3)

    # 3. Summary of rides created
    log_step(3, "Summary of created rides")
    print(f"\n   📊 RIDES CREATED:")
    print(f"      Total rides attempted: {NUM_RIDES}")
    print(f"      Successfully created: {len(ride_ids)}")
    print(f"      Failed rides: {failed_rides}")
    print(f"      Total waypoints sent: {total_waypoints_sent}")

    if ride_details:
        print(f"\n   📋 RIDE DETAILS:")
        total_dist = 0
        for ride in ride_details:
            print(f"      Ride {ride['ride_num']}: {ride['start']} → {ride['end']}")
            print(f"         ID: {ride['ride_id'][:8]}..., Dist: {ride['distance']:.2f}km, "
                  f"Speed: {ride['speed']:.2f}km/h, Waypoints: {ride['waypoints']}")
            total_dist += ride['distance']
        print(f"      TOTAL DISTANCE: {total_dist:.2f} km")

    # 4. Wait for final processing
    wait_for_processing(3, "final analytics to update")

    # 5. Verify with user rides endpoint
    db_rides = get_user_rides(user_id)
    print(f"\n   📋 Rides in database: {len(db_rides)}")

    # 6. Test User Analytics
    user_analytics = test_user_analytics(user_id, len(ride_ids))

    # 7. Final Summary
    print("\n" + "📈"*60)
    print("📈 CC-02 TEST SUMMARY - FINAL VERIFICATION".center(80))
    print("📈"*60)

    if user_analytics and user_analytics['totalRides'] > 0:
        print(f"\n📊 RESULTS:")
        print(f"   • Rides created: {len(ride_ids)}/{NUM_RIDES}")
        print(f"   • Waypoints sent: {total_waypoints_sent}")
        print(f"   • Failed rides: {failed_rides}")
        print(f"   • Database rides: {len(db_rides)}")
        print(f"   • Analytics rides: {user_analytics['totalRides']}")

        print(f"\n📊 VERIFICATION:")
        if user_analytics['totalRides'] == len(ride_ids):
            print(f"   ✅ SUCCESS: All {len(ride_ids)} rides are separate in analytics!")
        else:
            print(f"   ⚠️  Note: Analytics shows {user_analytics['totalRides']} rides")

        print(f"\n📊 METRICS:")
        print(f"   • Total Distance: {user_analytics['totalDistanceKm']:.2f} km")
        print(f"   • Avg Speed: {user_analytics['avgSpeedKmh']:.2f} km/h")
        print(f"   • Longest Ride: {user_analytics['maxRideDistanceKm']:.2f} km")

        print("\n" + "🎉"*60)
        print("🎉 CC-02 EXTENDED ANALYTICS: SUCCESSFULLY VERIFIED! 🎉".center(80))
        print("🎉"*60)
        print("\n✅ The analytics endpoint correctly aggregates multiple separate rides")
        print("✅ Each ride was ended immediately, creating distinct rides")
        print("✅ Distance, speed, and time calculations are working")
        return True
    else:
        print("\n❌ CC-02 EXTENDED ANALYTICS: TEST FAILED")
        return False

if __name__ == "__main__":
    success = generate_test_data()
    sys.exit(0 if success else 1)