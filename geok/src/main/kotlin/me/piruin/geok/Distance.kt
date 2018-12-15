/*
 * Copyright (c) 2015 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.piruin.geok

/**
 * From ported from java version at tanrabad/survey
 */
interface DistanceCalculator {

    /**
     * Calculate distance between Latlng
     *
     * @return distance in KM
     */
    fun between(latLng1: LatLng, latlng2: LatLng): Double
}

fun distanceCalculator(): DistanceCalculator = EllipsoidDistance()

private class EllipsoidDistance : DistanceCalculator {

    override fun between(latLng1: LatLng, latlng2: LatLng): Double {
        val lat1 = latLng1.latitude
        val lon1 = latLng1.longitude
        val lat2 = latlng2.latitude
        val lon2 = latlng2.longitude

        val l = Math.toRadians(lon2 - lon1)
        val u1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)))
        val u2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)))
        val sinU1 = Math.sin(u1)
        val cosU1 = Math.cos(u1)
        val sinU2 = Math.sin(u2)
        val cosU2 = Math.cos(u2)

        var cosSqAlpha: Double
        var sinSigma: Double
        var cos2SigmaM: Double
        var cosSigma: Double
        var sigma: Double

        var lambda = l
        var lambdaP: Double
        var iterLimit = 100.0

        do {
            val sinLambda = Math.sin(lambda)
            val cosLambda = Math.cos(lambda)
            sinSigma = Math.sqrt(cosU2 * sinLambda *
                    (cosU2 * sinLambda) +
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) *
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
            )

            if (sinSigma == 0.0)
                return 0.0

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
            sigma = Math.atan2(sinSigma, cosSigma)

            val sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
            cosSqAlpha = 1 - sinAlpha * sinAlpha
            cos2SigmaM = cosSigma - 2.0 * sinU1 * sinU2 / cosSqAlpha

            val c = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha))
            lambdaP = lambda
            lambda = l + ((1 - c) * f * sinAlpha
                    * (sigma + (c * sinSigma
                    * (cos2SigmaM + (c * cosSigma
                    * (-1 + 2.0 * cos2SigmaM * cos2SigmaM))))))
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0)

        if (iterLimit == 0.0) {
            return 0.0
        }

        val uSq = cosSqAlpha * (a * a - b * b) / (b * b)

        val a = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)))
        val b = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)))

        val deltaSigma = (b * sinSigma
                * (cos2SigmaM + b / 4 * (cosSigma * (-1 + 2.0 * cos2SigmaM * cos2SigmaM) - (b / 6 * cos2SigmaM
                * (-3 + 4.0 * sinSigma * sinSigma)
                * (-3 + 4.0 * cos2SigmaM * cos2SigmaM)))))

        return this.b * a * (sigma - deltaSigma)
    }

    private val b = 6356.752314

    companion object {
        private val a = 6378.137
        private val f = 0.003039
    }
}

fun LatLng.distanceTo(latlng: LatLng) = distanceCalculator().between(this, latlng)
