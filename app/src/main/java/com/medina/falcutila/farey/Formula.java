package com.medina.falcutila.farey;

public class Formula {

    // Google's Distance Matrix API returns travel time in Seconds

    public int toMinutes(int seconds) {
        return seconds / 60;
    }

    // Google's Distance Matrix API returns distance in Meters

    public float toKilometers(int meters) {
        float newmeters = meters;

        return newmeters / 1000;
    }

    /*
     * Taxi Fares
     * Types: Common and Airport
     * Resources: https://www.rappler.com/nation/208620-new-taxi-fares-per-kilometer-minute-rates
     */

    public float Formula(float dist, int time, int base, int per_min, float per_km) {
        int timefare = time * per_min; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * per_km;

        return (float) base + timefare + distancefare;
    }
}