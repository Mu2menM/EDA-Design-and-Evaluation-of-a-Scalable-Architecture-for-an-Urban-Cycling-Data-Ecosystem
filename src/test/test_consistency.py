import requests
import time
import sys

BASE_URL = "http://localhost:8080"
API_KEY = "thesis-secret-key"

def test_consistency():
    # 1. Create a new user
    username = f"testuser-{int(time.time())}"
    user_resp = requests.post(
        f"{BASE_URL}/api/users",
        json={"username": username},
        headers={"X-API-Key": API_KEY}
    )
    assert user_resp.status_code == 201, f"Failed to create user: {user_resp.text}"
    user_id = user_resp.json()["userId"]
    print(f"Created user: {user_id}")

    # 2. Send a telemetry point
    telemetry = {
        "userId": user_id,
        "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "latitude": 48.2082,
        "longitude": 16.3738
    }
    start_time = time.time()
    telemetry_resp = requests.post(
        f"{BASE_URL}/api/v1/telemetry",
        json=telemetry,
        headers={"X-API-Key": API_KEY}
    )
    assert telemetry_resp.status_code == 202, f"Telemetry not accepted: {telemetry_resp.text}"
    print("Telemetry accepted, waiting for consistency...")

    # 3. Poll the user's rides until a ride appears (or timeout)
    timeout = 3  # seconds
    poll_interval = 0.5
    elapsed = 0
    ride_id = None

    while elapsed < timeout:
        time.sleep(poll_interval)
        rides_resp = requests.get(
            f"{BASE_URL}/api/users/{user_id}/rides",
            headers={"X-API-Key": API_KEY}
        )
        if rides_resp.status_code != 200:
            print(f"⚠️  Rides endpoint error: {rides_resp.status_code}")
            continue

        rides = rides_resp.json()
        print(f"   Poll: found {len(rides)} rides")
        if rides:
            # Get the most recent ride (assuming sorted by startTime desc)
            ride_id = rides[0]["rideId"]
            break
        elapsed += poll_interval

    if ride_id is None:
        print("No ride appeared within timeout")
        # Optional: check directly if any ride exists for this user in DB (via Adminer)
        sys.exit(1)

    # 4. Fetch the ride details to confirm waypoint
    ride_resp = requests.get(
        f"{BASE_URL}/api/v1/rides/{ride_id}",
        headers={"X-API-Key": API_KEY}
    )
    if ride_resp.status_code == 200:
        ride_data = ride_resp.json()
        waypoints = ride_data.get("waypoints", [])
        if waypoints:
            end_time = time.time()
            delay = (end_time - start_time) * 1000
            print(f"Ride available. Delay = {delay:.2f} ms")
            if delay < 2000:
                print("SC-04 satisfied (< 2 seconds)")
            else:
                print("SC-04 violated (>= 2 seconds)")
        else:
            print("Ride has no waypoints – something wrong")
    else:
        print(f"Could not fetch ride details: {ride_resp.status_code}")

if __name__ == "__main__":
    test_consistency()