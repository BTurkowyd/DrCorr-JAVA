package com.company;

import java.util.ArrayList;
import java.util.List;

class Fiducial {

    private List<Particle> fiducial = new ArrayList<>();
    private ROIManager.ROIs roi;
    private List<Particle> listOfLocalizations;
    float[][] drift;
    private float intensityThres;

    Fiducial(ROIManager.ROIs roi, List<Particle> listOfLocalizations, float threshold) {
        this.roi = roi;
        this.listOfLocalizations = listOfLocalizations;
        this.intensityThres = threshold;
        System.out.println(roi.x + "   " + roi.x2 + "   " + roi.y + "   " + roi.y2 + "   ");
        populateFiducials();
        calculateRelativeDrift();
    }

    private void populateFiducials() {
        for (Particle p : listOfLocalizations) {
            if (p.getIntensity() > intensityThres && (p.getRescaledX()) > roi.x && (p.getRescaledX()) < roi.x2 &&
                    (p.getRescaledY()) > roi.y && (p.getRescaledY()) < roi.y2) {
                fiducial.add(p);

            }
        }
        System.out.println(fiducial.size());
    }

    private void calculateRelativeDrift() {
        float[][] drift = new float[(int) listOfLocalizations.get(listOfLocalizations.size() - 1).getTime() + 1][4];

        for (Particle aFiducial : fiducial) {
            drift[(int) aFiducial.getTime()][0] = aFiducial.getX() - fiducial.get(0).getX();
            drift[(int) aFiducial.getTime()][1] = aFiducial.getY() - fiducial.get(0).getY();
            drift[(int) aFiducial.getTime()][2] = aFiducial.getTime();
            drift[(int) aFiducial.getTime()][3] = aFiducial.getIntensity();
        }

        System.out.println("Relative drift computed successfully!");

        for (int i = drift.length - 20; i < drift.length; i++) {
            System.out.println(drift[i][0] + "   " + drift[i][1] + "   " + drift[i][2] + "   " + drift[i][3] + "   ");
        }
        this.drift = drift;
    }
}
