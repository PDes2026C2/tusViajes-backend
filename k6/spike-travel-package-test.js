import http from 'k6/http';
import { sleep, check } from 'k6';


// Spike: test que simula un alto repentino e inesperado pico de consumo de la app. Ej: venta de entradas para un
// evento masivo

export const options = {
  stages: [
    { duration: '10s', target: 1000 },
    { duration: '45s', target: 1000 },
    { duration: '15s', target: 0     }
  ],
  thresholds: {
    'http_req_duration': ['p(95) < 500'],
    'http_req_failed': ['rate < 0.01'],
  },
};

export default function() {
  let res = http.get('http://localhost:8080/api/travel-packages');
  check(res, { "status is 200": (res) => res.status === 200 });
  sleep(1);
}
