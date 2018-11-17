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

    public float TaxiCommon(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 13.50f;

        // System.out.println("Time Fare: " + timefare);
        // System.out.println("New Distance: " + newdist);
        // System.out.println("Distance Fare: " + distancefare);

        return (float) 40 + timefare + distancefare;
    }

    public float TaxiAirport(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 13.50f;

        // System.out.println("Time Fare: " + timefare);
        // System.out.println("New Distance: " + newdist);
        // System.out.println("Distance Fare: " + distancefare);

        return (float) 70 + timefare + distancefare;
    }

    /*
     * Car Sharing
     * Apps: Grab, uHop, micab, OWTO, Hype
     * Resources: https://assets.rappler.com/612F469A6EA84F6BAE882D2B94A4B421/img/7978DAAB0F7D45159EA8791FE2508DE2/ride-hailing-apps-2_7978DAAB0F7D45159EA8791FE2508DE2.jpg
     */

    /********************* GRAB FARES *********************/

    public float GrabCarFare(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 12f; // ₱10 - ₱14

        return (float) 40 + timefare + distancefare;
    }

    public float Grab6SeaterFare(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 16f; // ₱13 - ₱19

        return (float) 60 + timefare + distancefare;
    }

    public float GrabCarPlusFare(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 19.5f; // ₱16 - ₱23

        return (float) 70 + timefare + distancefare;
    }

    /********************* UHOP FARES *********************/

    public float uHopSedanFare(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 15.25f; // ₱14.75 - ₱15.75

        return (float) 40 + timefare + distancefare;
    }

    public float uHopSUVFare(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 15.25f; // ₱14.75 - ₱15.75

        return (float) 70 + timefare + distancefare;
    }

    public float uHopVanFare(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 15.25f; // ₱14.75 - ₱15.75

        return (float) 100 + timefare + distancefare;
    }

    /********************* MICAB FARES *********************/

    public float micabFare(int dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = dist / 300; // Fare is per 300m
        float distancefare = newdist * 13.50f; // ₱13.50 per 300m

        return (float) 40 + timefare + distancefare;
    }

    /********************* OWTO FARES *********************/

    public float owtoFare(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 12f;

        return (float) 40 + timefare + distancefare;
    }

    /********************* HYPE FARES *********************/

    public float hypeFare(float dist, int time) {
        int timefare = time * 2; // ₱2 per minute
        int newdist = (int) dist; // remove decimal places
        float distancefare = newdist * 14f;

        return (float) 40 + timefare + distancefare;
    }
}