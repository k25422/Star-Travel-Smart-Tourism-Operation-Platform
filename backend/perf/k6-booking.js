import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  scenarios: {
    booking_spike: {
      executor: "ramping-vus",
      startVUs: 1,
      stages: [
        { duration: "15s", target: 10 },
        { duration: "20s", target: 30 },
        { duration: "15s", target: 0 }
      ]
    }
  },
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<1200"]
  }
};

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";
const username = __ENV.USERNAME || "traveler";
const password = __ENV.PASSWORD || "user123";
const routeId = Number(__ENV.ROUTE_ID || "1");

function login() {
  const response = http.post(`${baseUrl}/api/auth/login`, JSON.stringify({
    username,
    password
  }), {
    headers: { "Content-Type": "application/json" }
  });

  check(response, {
    "login success": (r) => r.status === 200
  });

  return response.json("data.accessToken");
}

export default function () {
  const token = login();
  const response = http.post(`${baseUrl}/api/bookings`, JSON.stringify({
    routeId,
    travelerName: `压测用户-${__VU}-${__ITER}`,
    phone: `139${String(__VU).padStart(4, "0")}${String(__ITER).padStart(4, "0")}`.slice(0, 11),
    travelers: 1,
    requestId: `perf-${__VU}-${__ITER}`
  }), {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json"
    }
  });

  check(response, {
    "booking success or sold out": (r) => r.status === 201 || r.status === 400
  });

  sleep(1);
}
