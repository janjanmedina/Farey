package com.medina.falcutila.farey;

public class RideShare {
    public int baseFare;
    public float perKM;
    public int perMin;
    public String pkg;

    public RideShare() {

    }

    public RideShare(int baseFare, float perKM, int perMin, String pkg) {
        this.baseFare = baseFare;
        this.perKM = perKM;
        this.perMin = perMin;
        this.pkg = pkg;
    }

    public int getBaseFare() {
        return baseFare;
    }

    public float getPerKM() {
        return perKM;
    }

    public int getPerMin() {
        return perMin;
    }

    public String getPkg() {
        return pkg;
    }
}
