import http from 'k6/http';
import { sleep, check } from 'k6';

// Stress: test que simula un alto consumo de la app, con duracion mas alta que un spike test y con un pico menor


/*

Simula una entrada a la pagina, registro y agrego de favoritos (para no depender de vender o no un paquete

 */
export const options = {
    stages: [
        { duration: '3m', target: 200 },
        { duration: '10m', target: 200 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        'http_req_duration': ['p(95) < 600'],
        'http_req_failed': ['rate < 0.01'],   // Menos del 1% de errores globales
    }
};

export default () => {
    const headers = { 'Content-Type': 'application/json' };

    let packagesRes = http.get('http://localhost:8080/api/travel-packages', { headers });

    let getPackagesCheck = check(packagesRes, {
        "status is 200": (r) => r.status === 200
    });

    let selectedPackageId = 1;
    if (getPackagesCheck && packagesRes.body) {
        try {
            let packages = JSON.parse(packagesRes.body);
            if (Array.isArray(packages) && packages.length > 0) {
                // paquete random
                let randomPackage = packages[Math.floor(Math.random() * packages.length)];
                selectedPackageId = randomPackage.id;
            }
        } catch (e) {
            // si falla busca el 1
        }
    }

    // registro
    const uniqueId = `${__VU}_${__ITER}_${Date.now()}`;
    const email = `test_${uniqueId}@example.com`;

    const registerPayload = JSON.stringify({
        firstName: "User",
        lastName: `number ${__VU}`,
        email: email,
        password: "password",
        phoneNumber: 1123456789,
        nationalId: 30000000 + (__VU * 100000) + __ITER,
    });

    let registerRes = http.post('http://localhost:8080/api/auth/register/buyer', registerPayload, { headers });

    let authToken = null;
    check(registerRes, {
        "status is 201": (r) => r.status === 201,
        "response has id and tokens": (r) => {
            try {
                let body = JSON.parse(r.body);
                if (body.token) {
                    authToken = body.token;
                }
                return body.id !== undefined && body.token !== undefined;
            } catch (e) {
                return false;
            }
        }
    });

    if (authToken) {
        let authHeaders = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`
        };

        let favoriteRes = http.post(
            `http://localhost:8080/api/buyers/me/favorites/${selectedPackageId}`,
            null, // El endpoint no pide body, solo el path variable
            { headers: authHeaders }
        );


        let favCheck = check(favoriteRes, {
            "favorite added status is 200": (r) => r.status === 200,
        });

        if (!favCheck) {
            console.log(`FALLÓ FAVORITO -> Status: ${favoriteRes.status} | Body: ${favoriteRes.body} | PackageId: ${selectedPackageId}`);
        }
    }

    sleep(1);
};