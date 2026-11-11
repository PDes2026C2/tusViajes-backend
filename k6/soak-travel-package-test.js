import http from 'k6/http';
import { sleep, check } from 'k6';
import { randomIntBetween, randomItem } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { Trend } from 'k6/metrics'

export const options = {
    stages: [
        { duration: '2m', target: 100 },
        { duration: '2h', target: 100 },
        { duration: '2m', target: 0 },
    ],
    thresholds: {
        'http_req_duration': ['p(95) < 500'],
        'http_req_failed': ['rate < 0.005'],
    },
};

const BASE_URL = 'http://localhost:8080/api';

const mostSearchedCountry = new Trend('searched_country')

export default function () {
    const headers = { 'Content-Type': 'application/json' };

    // obtener paises
    let countriesRes = http.get(`${BASE_URL}/countries`, { headers });

    let countriesCheck = check(countriesRes, {
        "countries status is 200": (r) => r.status === 200,
        "countries body is not empty": (r) => r.body && r.body.length > 2,
    });

    if (!countriesCheck) {
        sleep(1);
        return; // Si falló el GET, salimos de esta iteración
    }

    let countries = [];
    try {
        countries = JSON.parse(countriesRes.body);
    } catch (e) {
        console.error("Error parseando countries: " + e);
        return;
    }

    if (countries.length === 0) return;

    // Seleccionamos un país al azar del array recibido
    let randomCountry = randomItem(countries);
    let selectedIsoCode = randomCountry.isoCode;


    // random para ver si consultamos por ciudad o solo por pais
    let selectedCityId = null;
    // Supongamos que el 70% de los usuarios consultan las ciudades del país elegido
    if (Math.random() < 0.7) {
        let citiesRes = http.get(`${BASE_URL}/cities?countryISO=${selectedIsoCode}`, { headers });

        if (citiesRes.status === 200) {
            try {
                let cities = JSON.parse(citiesRes.body);
                if (cities && cities.length > 0) {
                    let randomCity = randomItem(cities);
                    selectedCityId = randomCity.id || randomCity.cityId;
                }
            } catch (e) {
                // Manejo silencioso si el formato difiere
            }
        }
    }

    if (Math.random() < 0.5) {
        // Armamos los queryparams basados en tu TravelPackageFilterDTO
        let queryParams = [];

        // 50% de probabilidad de filtrar por país de origen o destino
        if (Math.random() < 0.5) {
            queryParams.push(`destinationCountryIso=${selectedIsoCode}`);
            if (selectedCityId) {
                queryParams.push(`destinationCityId=${selectedCityId}`);
            }
        } else {
            queryParams.push(`originCountryIso=${selectedIsoCode}`);
        }

        let queryString = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';

        let packagesRes = http.get(`${BASE_URL}/travel-packages${queryString}`, { headers });

        check(packagesRes, {
            "travel-packages filtered status is 200": (r) => r.status === 200,
        });
    } else {

        let packagesRes = http.get(`${BASE_URL}/travel-packages`, { headers });
        check(packagesRes, {
            "travel-packages general status is 200": (r) => r.status === 200,
        });
    }

    sleep(randomIntBetween(2, 5));
}