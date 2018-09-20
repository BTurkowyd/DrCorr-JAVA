package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

class ROIManager extends JFrame {

    final List<Rectangle> rectangles = new ArrayList<>();
    private List<Fiducial> fiducialList = new ArrayList<>();
    List<NeNA> nenaList = new ArrayList<>();
    ArrayList<List<NeNA>> subNenaList;
    ArrayList<List<NUNeNA>> subNuNenaList;
    private int x, y, x2, y2;
    private List<ROIs> rois = new ArrayList<>();
    private List<Integer> roisCoords = new ArrayList<>();
    private static float[][] smoothDrift;
    List<NUNeNA> nunenaList = new ArrayList<>();

    /*
     * Opens an image and sets up the mouse clicks listener
     */
    ROIManager(ImageIcon img) {

        x = y = x2 = y2 = 0;
        MouseListener listener = new MouseListener();
        addMouseListener(listener);
        addMouseMotionListener(listener);

        this.setSize(img.getIconWidth(), img.getIconHeight());
        this.setTitle("Super-resolution reconstruction image");
        this.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());
        this.setMaximizedBounds(new Rectangle(0, 0, img.getIconWidth(), img.getIconHeight()));
        this.setUndecorated(true);
        System.out.println(img.getIconWidth() + " x " + img.getIconHeight());
        JLabel bkgdImage = new JLabel(img);
        this.add(bkgdImage);

        System.out.println(this.getWidth() + " x " + this.getHeight());
        this.setVisible(true);
        subNenaList = new ArrayList<>();
        subNuNenaList = new ArrayList<>();
    }

    private void setStartPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private void setEndPoint(int x, int y) {
        this.x2 = x;
        this.y2 = y;
    }

    private void drawPerfectRect(Graphics g, int x, int y, int x2, int y2) {
        int px = Math.min(x, x2);
        int py = Math.min(y, y2);
        int pw = Math.abs(x - x2);
        int ph = Math.abs(y - y2);
        g.drawRect(px, py, pw, ph);
        for (Rectangle r : rectangles) {
            g.setColor(Color.RED);
            g.drawRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());

        }
        g.dispose();
    }

    List<ROIs> getRois() {
        return rois;
    }

    void generateFiducials(List<ROIs> rois, List<Particle> listOfLocalizations, float threshold) {
        int counter = 0;
        for (ROIs r : rois) {
            fiducialList.add(new Fiducial(r, listOfLocalizations, threshold));
            counter++;
            System.out.println("Fiducial " + counter + " created!");
        }

        calculateAverageDrift();
        for (float[] aSmoothDrift : smoothDrift) {
            System.out.printf("X: %s   Y: %s   T: %s   Int: %s%n", aSmoothDrift[0], aSmoothDrift[1], aSmoothDrift[2], aSmoothDrift[3]);
        }
    }

    void generateNeNAs(List<ROIs> rois, List<Particle> listOfLocalizations) {
        int counter = 0;
        for (ROIs r : rois) {
            this.nenaList.add(new NeNA(r, listOfLocalizations));
            counter++;
            System.out.println("NeNA area no.:  " + counter + " created!");
        }

        for (NeNA n : nenaList) {
            n.populateSubROI();

        }

        for (NeNA n : nenaList) {
            n.nextFramePartners();
        }

        for (NeNA n : nenaList) {
            n.findNNs();
        }

        for (NeNA n : nenaList) {
            n.makeHistograms();
        }
    }

    void generateNUNeNas(List<ROIs> rois, List<Particle> listofLocalizations) {
        int counter = 0;
        for (ROIs r : rois) {
            this.nunenaList.add(new NUNeNA(r, listofLocalizations));
            counter++;
            System.out.println("NUNeNA area no.:  " + counter + " created!");
        }

        for (NUNeNA n : nunenaList){
            n.populateSubROI();
        }

        for (NUNeNA n : nunenaList){
            n.nextFramePartners();
            n.next100thFramePartners();
        }

        for (NUNeNA n : nunenaList) {
            n.findNNs();
            n.find100thNNs();
        }

        for (NUNeNA n : nunenaList) {
            n.makeHist();
        }
    }

    void generateTempNeNAs(List<ROIs> rois, List<Particle> particles, JTextArea initFrame, JTextArea lastFrame, JTextArea jumpFrame, JTextArea windowSize) {

        if (!DrCorrGUI.nenaCorrectionTerms) {
            if (Integer.parseInt(lastFrame.getText()) > particles.get(particles.size()-1).getTime()){
                lastFrame.setText(Integer.toString((int) particles.get(particles.size()-1).getTime()));
            }

            int counter = 0;
            for (ROIs r : rois) {
                this.nenaList.add(new NeNA(r, particles));
                counter++;
                System.out.println("NeNA area no.:  " + counter + " created!");
            }

            for (NeNA n : nenaList) {
                n.populateSubROI();
            }

            for (int i=0; i < nenaList.size(); i++) {
                List<NeNA> tempRegionArray = new ArrayList<>();
                for (int j = Integer.parseInt(initFrame.getText()); j <= Integer.parseInt(lastFrame.getText()); j += Integer.parseInt(jumpFrame.getText())){
                    List<Particle> tempParticleArray = new ArrayList<>();
                    for (Particle p : nenaList.get(i).subROI){
                        if (p.getTime() >= j && p.getTime() < j + Integer.parseInt(windowSize.getText())){
                            tempParticleArray.add(p);
                        }

                    }
                    tempRegionArray.add(new NeNA(rois.get(i), tempParticleArray));
                    System.out.println("Temporal particle array " + tempParticleArray.size());
                }
                System.out.println("Temporal region array " + tempRegionArray.size());
                subNenaList.add(tempRegionArray);
                System.out.println("Sub Nena List " + subNenaList.size());
            }

            System.out.println("Temporal nena executed");

            // Here you have to access subNenaList lists to calculate nena. First layer stores N-lists of regions, second stores lists of particles

            for (List<NeNA> aSubNenaList1 : subNenaList) {
                for (NeNA anASubNenaList1 : aSubNenaList1) {

                    anASubNenaList1.populateSubROI();
                }
            }

            for (List<NeNA> aSubNenaList : subNenaList) {
                for (NeNA anASubNenaList : aSubNenaList) {
                    anASubNenaList.nextFramePartners();
                }
            }

            for (List<NeNA> aSubNenaList : subNenaList) {
                for (NeNA anASubNenaList : aSubNenaList) {
                    anASubNenaList.findNNs();
                }
            }

            for (List<NeNA> aSubNenaList : subNenaList) {
                for (NeNA anASubNenaList : aSubNenaList) {
                    anASubNenaList.makeHistograms();
                }
            }
        } else {
            System.out.println(":)");
            if (Integer.parseInt(lastFrame.getText()) > particles.get(particles.size()-1).getTime()){
                lastFrame.setText(Integer.toString((int) particles.get(particles.size()-1).getTime()));
            }

            int counter = 0;
            for (ROIs r : rois) {
                this.nunenaList.add(new NUNeNA(r, particles));
                counter++;
                System.out.println("NeNA area no.:  " + counter + " created!");
            }

            for (NUNeNA n : nunenaList) {
                n.populateSubROI();
            }

            for (int i=0; i < nunenaList.size(); i++) {
                List<NUNeNA> tempRegionArray = new ArrayList<>();
                for (int j = Integer.parseInt(initFrame.getText()); j <= Integer.parseInt(lastFrame.getText()); j += Integer.parseInt(jumpFrame.getText())){
                    List<Particle> tempParticleArray = new ArrayList<>();
                    for (Particle p : nunenaList.get(i).subROI){
                        if (p.getTime() >= j && p.getTime() < j + Integer.parseInt(windowSize.getText())){
                            tempParticleArray.add(p);
                        }

                    }
                    tempRegionArray.add(new NUNeNA(rois.get(i), tempParticleArray));
                    System.out.println("Temporal particle array " + tempParticleArray.size());
                }
                System.out.println("Temporal region array " + tempRegionArray.size());
                subNuNenaList.add(tempRegionArray);
                System.out.println("Sub Nena List " + subNuNenaList.size());
            }

            System.out.println("Temporal nena executed");

            // Here you have to access subNenaList lists to calculate nena. First layer stores N-lists of regions, second stores lists of particles

            for (List<NUNeNA> aSubNuNenaList1 : subNuNenaList) {
                for (NUNeNA anASubNuNenaList1 : aSubNuNenaList1) {

                    anASubNuNenaList1.populateSubROI();
                }
            }

            for (List<NUNeNA> aSubNuNenaList : subNuNenaList) {
                for (NUNeNA anASubNuNenaList : aSubNuNenaList) {
                    anASubNuNenaList.nextFramePartners();
                }
            }

            for (List<NUNeNA> aSubNuNenaList : subNuNenaList) {
                for (NUNeNA anASubNuNenaList : aSubNuNenaList) {
                    anASubNuNenaList.findNNs();
                }
            }

            for (List<NUNeNA> aSubNuNenaList : subNuNenaList) {
                for (NUNeNA anASubNuNenaList : aSubNuNenaList) {
                    anASubNuNenaList.makeHist();
                }
            }
        }

//        if (Integer.parseInt(lastFrame.getText()) > particles.get(particles.size()-1).getTime()){
//            lastFrame.setText(Integer.toString((int) particles.get(particles.size()-1).getTime()));
//        }
//
//        int counter = 0;
//        for (ROIs r : rois) {
//            this.nenaList.add(new NeNA(r, particles));
//            counter++;
//            System.out.println("NeNA area no.:  " + counter + " created!");
//        }
//
//        for (NeNA n : nenaList) {
//            n.populateSubROI();
//        }
//
//        for (int i=0; i < nenaList.size(); i++) {
//            List<NeNA> tempRegionArray = new ArrayList<>();
//            for (int j = Integer.parseInt(initFrame.getText()); j <= Integer.parseInt(lastFrame.getText()); j += Integer.parseInt(jumpFrame.getText())){
//                List<Particle> tempParticleArray = new ArrayList<>();
//                for (Particle p : nenaList.get(i).subROI){
//                    if (p.getTime() >= j && p.getTime() < j + Integer.parseInt(windowSize.getText())){
//                        tempParticleArray.add(p);
//                    }
//
//                }
//                tempRegionArray.add(new NeNA(rois.get(i), tempParticleArray));
//                System.out.println("Temporal particle array " + tempParticleArray.size());
//            }
//            System.out.println("Temporal region array " + tempRegionArray.size());
//            subNenaList.add(tempRegionArray);
//            System.out.println("Sub Nena List " + subNenaList.size());
//        }
//
//        System.out.println("Temporal nena executed");
//
//        // Here you have to access subNenaList lists to calculate nena. First layer stores N-lists of regions, second stores lists of particles
//
//        for (List<NeNA> aSubNenaList1 : subNenaList) {
//            for (NeNA anASubNenaList1 : aSubNenaList1) {
//
//                anASubNenaList1.populateSubROI();
//            }
//        }
//
//        for (List<NeNA> aSubNenaList : subNenaList) {
//            for (NeNA anASubNenaList : aSubNenaList) {
//                anASubNenaList.nextFramePartners();
//            }
//        }
//
//        for (List<NeNA> aSubNenaList : subNenaList) {
//            for (NeNA anASubNenaList : aSubNenaList) {
//                anASubNenaList.findNNs();
//            }
//        }
//
//        for (List<NeNA> aSubNenaList : subNenaList) {
//            for (NeNA anASubNenaList : aSubNenaList) {
//                anASubNenaList.makeHistograms();
//            }
//        }
    }

    private void calculateAverageDrift() {
        // This part has to be corrected. It is sensitive to gaps in fiducials.
        float[][] smoothDrift = new float[fiducialList.get(0).drift.length][4];
        for (int i = 0; i < fiducialList.get(0).drift.length; i++) {
            for (int j = 0; j < 2; j++) {
                for (Fiducial aFiducialList : fiducialList) {
                    smoothDrift[i][j] += aFiducialList.drift[i][j];
                }
                smoothDrift[i][j] /= fiducialList.size();
            }
        }

        for (int i = 0; i < smoothDrift.length; i++) {
            smoothDrift[i][2] = i;
            smoothDrift[i][3] = i;
        }
        ROIManager.smoothDrift = smoothDrift;

    }

    static float[][] getSmoothDrift() {
        return smoothDrift;
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.GREEN);
        drawPerfectRect(g, x, y, x2, y2);
    }



    class MouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            setStartPoint(e.getX(), e.getY());
            roisCoords.add(e.getX());
            roisCoords.add(e.getY());

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            setEndPoint(e.getX(), e.getY());
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            setEndPoint(e.getX(), e.getY());
            repaint();
            roisCoords.add(e.getX());
            roisCoords.add(e.getY());
            rectangles.add(new Rectangle(Math.min(roisCoords.get(0), roisCoords.get(2)), Math.min(roisCoords.get(1), roisCoords.get(3)),
                    Math.abs(roisCoords.get(0) - roisCoords.get(2)), Math.abs(roisCoords.get(1) - roisCoords.get(3))));

            rois.add(new ROIs(Math.min(roisCoords.get(0), roisCoords.get(2)), Math.min(roisCoords.get(1), roisCoords.get(3)),
                    Math.max(roisCoords.get(0), roisCoords.get(2)), Math.max(roisCoords.get(1), roisCoords.get(3))));
            roisCoords.clear();
        }
    }

    class ROIs {
        float x;
        float y;
        float x2;
        float y2;

        ROIs(float x, float y, float x2, float y2) {
            this.x = x;
            this.y = y;
            this.x2 = x2;
            this.y2 = y2;
        }

    }
}
