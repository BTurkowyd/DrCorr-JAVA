package com.company;

import java.util.ArrayList;
import java.util.List;

class Particle {
    private final float x;
    private final float y;
    private float time;
    private float intensity;
    private float rescaledX;
    private float rescaledY;
    private float newX, newY;
    private static float[][] drift;
    List<Particle> nextFramePartners = new ArrayList<>();
    List<Particle> next100thFramePartners = new ArrayList<>();
    Particle nearestNeighbor;
    Particle nearest100thNeighbor;
    double nnDist = -1;
    double nn100thDist = -1;
    int nnDistToHistogram = -1;
    int nn100thDistToHistogram = -1;
    private int dbScanCluster = -1;
    double opticsRD = -1;
    double opticsCD = -1;
    boolean opticsProcessed = false;


    Particle(float x, float y, float time, float intensity) {
        this.x = x;
        this.y = y;
        this.time = time;
        this.intensity = intensity;


    }

    void rescale() {

        float rescaledX = this.x / (RescalingFactor.rescalingFactorX * 10);
        float rescaledY = this.y / (RescalingFactor.rescalingFactorY * 10);
        this.rescaledX = rescaledX;
        this.rescaledY = rescaledY;
    }

    static void loadDrift() {
        drift = ROIManager.getSmoothDrift();
        System.out.println("Drift loaded successfully!");

    }

    void driftCorrection() {
        for (float[] aDrift : drift) {
            if (aDrift[2] == this.time) {
                this.newX = this.x - aDrift[0];
                this.newY = this.y - aDrift[1];
            }
        }
    }

    float getX() {
        return x;
    }

    float getY() {
        return y;
    }

    float getTime() {
        return time;
    }

    float getIntensity() {
        return intensity;
    }

    float getRescaledX() {
        return rescaledX;
    }

    float getRescaledY() {
        return rescaledY;
    }

    float getNewX() {
        return newX;
    }

    float getNewY() {
        return newY;
    }

    int getDbScanCluster() {
        return dbScanCluster;
    }

    void setDbScanCluster(int dbScanCluster) {
        this.dbScanCluster = dbScanCluster;
    }

}
