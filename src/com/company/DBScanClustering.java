package com.company;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DBScanClustering extends JFrame implements ActionListener {

    private List<Particle> particles;
    private List<ROIManager.ROIs> rois;
    private File currentDir;
    private ArrayList<List<Particle>> subRois = new ArrayList<>();
    private ArrayList<DoublePoint[]> doublePointsSubRois = new ArrayList<>();
    private JTextArea eps;
    private JTextArea minPts;
    private List<List<Cluster<DoublePoint>>> roiClusters = new ArrayList<>();
    private JComboBox<String> fileType;
    private static JProgressBar dbscanProgress = new JProgressBar();

    DBScanClustering(List<Particle> particles, List<ROIManager.ROIs> rois, File currentDir, JComboBox<String> fileType) throws HeadlessException {
        this.particles = particles;
        this.rois = rois;
        this.currentDir = currentDir;
        this.fileType = fileType;

        JFrame dbScanFrame = new JFrame("DBScan clustering");
        JPanel panel = new JPanel();
        DrCorrGUI.MyLayoutManager mgr = new DrCorrGUI.MyLayoutManager();
        panel.setLayout(mgr);
        panel.setPreferredSize(new Dimension(440, 320));

        eps = new JTextArea();
        minPts = new JTextArea();

        JLabel epsLabel = new JLabel("Epsilon (nm)");
        JLabel minPtsLabel = new JLabel("MinPts (min. 2)");

        eps.setText("50");
        minPts.setText("10");

        Font font = new Font("Arial", Font.PLAIN, 20);
        eps.setFont(font);
        minPts.setFont(font);

        dbscanProgress.setValue(0);
        dbscanProgress.setStringPainted(true);
        dbscanProgress.setString("Progress: " + (int) (dbscanProgress.getPercentComplete()*100) + "%");

        JButton executeDBScan = new JButton("Run DBScan");

        executeDBScan.addActionListener(this);
        executeDBScan.setActionCommand("dbscan");

        panel.add(eps, new Rectangle(20, 60, 150, 40));
        panel.add(minPts, new Rectangle(20, 170, 150, 40));

        panel.add(epsLabel, new Rectangle(20, 10, 170, 40));
        panel.add(minPtsLabel, new Rectangle(20, 120, 170, 40));

        panel.add(dbscanProgress, new Rectangle(20,250,400, 60));

        panel.add(executeDBScan, new Rectangle(200, 60, 200, 150));

        dbScanFrame.add(panel);
        dbScanFrame.pack();
        SwingUtilities.updateComponentTreeUI(dbScanFrame);
        dbScanFrame.setVisible(true);
    }

    private void populateSubROI(ROIManager.ROIs roi, ArrayList<Particle> subListOfParticles) {
        for (Particle p : particles) {
            if ((p.getRescaledX()) > roi.x && (p.getRescaledX()) < roi.x2 &&
                    (p.getRescaledY()) > roi.y && (p.getRescaledY()) < roi.y2) {
                subListOfParticles.add(p);

            }
        }
        System.out.println(subListOfParticles.size());
        System.out.println("DBScan area populated");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        switch (action) {
            case "dbscan":
                Thread runDBScan = new Thread(() -> {
                    this.particles = DrCorrGUI.getParticles();
                    this.rois = DrCorrGUI.getRois();
                    this.currentDir = DrCorrGUI.getCurrentDir();

                    this.subRois.clear();
                    this.doublePointsSubRois.clear();
                    this.roiClusters.clear();

                    DoublePoint[] points = new DoublePoint[particles.size()];

                    for (int i = 0; i < particles.size(); i++) {
                        points[i] = new DoublePoint(new double[]{particles.get(i).getX(), particles.get(i).getY()});
                    }

                    System.out.println(points.length);

                    for (int i = 0; i < 10; i++) {
                        System.out.println(points[i].toString());
                    }

                    System.out.println("xDDDDDDDDBScaaaaaaaaaaaan, bitches!");
                    for (ROIManager.ROIs r : this.rois) {
                        System.out.println(r.x + " " + r.y);
                    }

                    for (ROIManager.ROIs r : rois) {
                        ArrayList<Particle> subListOfParticles = new ArrayList<>();
                        populateSubROI(r, subListOfParticles);
                        DoublePoint[] doublePointsSubListOfParticles = new DoublePoint[subListOfParticles.size()];
                        for (int i = 0; i < subListOfParticles.size(); i++) {
                            doublePointsSubListOfParticles[i] = new DoublePoint(new double[]{subListOfParticles.get(i).getX(), subListOfParticles.get(i).getY()});
                        }
                        this.subRois.add(subListOfParticles);
                        this.doublePointsSubRois.add(doublePointsSubListOfParticles);
                        System.out.println("double point list size: " + this.doublePointsSubRois.size());
                        System.out.println("subROI size: " + this.subRois.size());

                    }

                    final DBSCANClusterer<DoublePoint> clustering = new DBSCANClusterer<>(Integer.parseInt(this.eps.getText()), Integer.parseInt(this.minPts.getText()));

                    for (DoublePoint[] p : doublePointsSubRois) {
                        System.out.println(doublePointsSubRois.size());
                        final List<Cluster<DoublePoint>> clusters = clustering.cluster(Arrays.asList(p));
                        this.roiClusters.add(clusters);
                        System.out.println("Clustered!");
                        System.out.println("Size of the roiClusters: " + this.roiClusters.size());
                    }

                    int totalClusters = 0;
                    for (List<Cluster<DoublePoint>> c : this.roiClusters) {
                        System.out.println("Number of clusters: " + c.size());
                        totalClusters += c.size();
                    }

                    System.out.println("Size of the subrois: " + this.subRois.size());
                    System.out.println("Size of the doublepointsubrois: " + this.doublePointsSubRois.size());
                    System.out.println("Size of the roiclusters: " + this.roiClusters.size());

                    int clusterNumber = 1;

//                DoublePoint x = roiClusters.get(0).get(0).getPoints().get(0); first get(0)- get the roi; second get(0)- get the cluster; third get(0)- get the point
                    dbscanProgress.setMaximum(totalClusters);

                    for (int i = 0; i < roiClusters.size(); i++) {
                        for (int j = 0; j < roiClusters.get(i).size(); j++) {

                            for (int k = 0; k < roiClusters.get(i).get(j).getPoints().size(); k++) {
                                for (int l = 0; l < subRois.get(i).size(); l++) {

                                    if (roiClusters.get(i).get(j).getPoints().get(k).equals(new DoublePoint(new double[]{subRois.get(i).get(l).getX(), subRois.get(i).get(l).getY()}))) {
                                        subRois.get(i).get(l).setDbScanCluster(clusterNumber);

                                    }

                                }

                            }
                            clusterNumber++;
                            dbscanProgress.setValue(clusterNumber);
                            dbscanProgress.setString("Processing ROI " + (i+1) + ": " + (int) (dbscanProgress.getPercentComplete()*100) + "%");
                        }

                    }

                    dbscanProgress.setString("Done!");


                    System.out.println("Total number of clusters: " + (clusterNumber - 1));

// Export contains five columns for both, RapidStorm and ThunderStorm formats. Fix it for Thunderstorm, it should return ALL possible parameters.
// Simple "if" statement, which will check which format is processed, will be enough. but do it from home. :))

                    if (fileType.getSelectedItem() == "RapidStorm") {
                        try (PrintWriter writer = new PrintWriter(currentDir.getParentFile() + "\\DBScan_clusters.txt")) {
                            for (Particle p : particles) {
                                if (p.getDbScanCluster() > -2) {
                                    writer.println(String.format(Locale.US, "%.1f %.1f %.0f %.0f %.0f", p.getX(), p.getY(), p.getTime(), p.getIntensity(), (float) p.getDbScanCluster()));
                                }

                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        try (PrintWriter writer = new PrintWriter(currentDir.getParentFile() + "\\DBScan_clusters.csv")) {
                            for (Particle particle : particles) {
                                if (particle.getDbScanCluster() > -2) {
                                    writer.println(String.format(Locale.US, "%.0f,%.0f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.5f,%.0f",
                                            particle.getId(), particle.getTime(), particle.getX(), particle.getY(),
                                            particle.getSigma(), particle.getIntensity(), particle.getOffset(), particle.getBkgstd(),
                                            particle.getChi2(), particle.getUncertainity_xy(), (float) particle.getDbScanCluster()));
                                }

                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                runDBScan.start();


        }

    }


}
