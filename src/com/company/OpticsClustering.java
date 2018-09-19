package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

public class OpticsClustering extends JFrame implements ActionListener {

    private List<Particle> particles;
    private int minPoints;
    private double maxRadius;
    private File currentDir;
    private JComboBox<String> fileType;
    private ArrayList<List<Particle>> subRois = new ArrayList<>();
    private ArrayList<List<Particle>> unprocessed = new ArrayList<>();
    private ArrayList<List<Particle>> ordered = new ArrayList<>();
    private JTextArea eps = new JTextArea();
    private JTextArea minPts = new JTextArea();
    private static JProgressBar opticsProgress = new JProgressBar();

    OpticsClustering() throws HeadlessException {

        JFrame opticsFrame = new JFrame("OPTICS clustering");
        JPanel panel = new JPanel();
        DrCorrGUI.MyLayoutManager mgr = new DrCorrGUI.MyLayoutManager();
        panel.setLayout(mgr);
        panel.setPreferredSize(new Dimension(440, 320));

        JLabel epsLabel = new JLabel("Upper limit (nm)");

        JLabel minPtsLabel = new JLabel("MinPts (min. 2)");

        eps.setText("1000");
        minPts.setText("10");

        Font font = new Font("Arial", Font.PLAIN, 20);
        eps.setFont(font);
        minPts.setFont(font);

        opticsProgress.setValue(0);
        opticsProgress.setStringPainted(true);
        opticsProgress.setString("Progress: " + (int) (opticsProgress.getPercentComplete()*100) + "%");

        JButton executeOPTICS = new JButton("Run OPTICS");

        executeOPTICS.addActionListener(this);
        executeOPTICS.setActionCommand("optics");

        panel.add(eps, new Rectangle(20, 60, 150, 40));
        panel.add(epsLabel, new Rectangle(20, 10, 170, 40));
        panel.add(minPts, new Rectangle(20, 180, 150,40));
        panel.add(minPtsLabel, new Rectangle(20, 130, 150,40));
        panel.add(executeOPTICS, new Rectangle(200, 60, 200, 150));
        panel.add(opticsProgress, new Rectangle(20,250,400, 60));

        opticsFrame.add(panel);
        opticsFrame.pack();
        SwingUtilities.updateComponentTreeUI(opticsFrame);
        opticsFrame.setVisible(true);

    }

    private void populateSubROI(ROIManager.ROIs roi, ArrayList<Particle> subListOfParticles) {
        for (Particle p : particles) {
            if ((p.getRescaledX()) > roi.x && (p.getRescaledX()) < roi.x2 &&
                    (p.getRescaledY()) > roi.y && (p.getRescaledY()) < roi.y2) {
                subListOfParticles.add(p);

            }
        }
        System.out.println(subListOfParticles.size());
        System.out.println("OPTICS area populated");
    }

    private double getHypotenuse(Particle particleOfInterest, Particle otherParticles) {
        return Math.hypot(particleOfInterest.getX() - otherParticles.getX(),
                particleOfInterest.getY() - otherParticles.getY());

    }

    private void setup(List<Particle> subRoi){
        System.out.println("Size of the subRoi: " + subRoi.size());
        List<Particle> unprocessedRoi = new ArrayList<>();
        List<Particle> orderedRoi = new ArrayList<>();
        unprocessedRoi.addAll(subRoi);
        this.unprocessed.add(unprocessedRoi);
        this.ordered.add(orderedRoi);
    }

    private double coreDistance(Particle point, List<Particle> neighbors){
        if (point.opticsCD != -1) {
            return point.opticsCD;
        }

        if (neighbors.size() < this.minPoints - 1) {
            point.opticsCD = this.maxRadius;
            return point.opticsCD;
        }

        if (neighbors.size() >= this.minPoints - 1) {
            double[] sortedNeighbors = new double[neighbors.size()];
            for (int i = 0; i < sortedNeighbors.length; i++) {
                sortedNeighbors[i] = getHypotenuse(point, neighbors.get(i));
            }

            Arrays.sort(sortedNeighbors);
            point.opticsCD = sortedNeighbors[this.minPoints - 2];
            return point.opticsCD;
        }
        return point.opticsCD;
    }

    private List<Particle> neighbors(Particle point, List<Particle> subRoi){
        List<Particle> pointNeighbors = new ArrayList<>();
        for (Particle p : subRoi){
            if (getHypotenuse(point, p) <= this.maxRadius && point != p) {
                pointNeighbors.add(p);
            }
        }

        return pointNeighbors;
    }

    private void processed(Particle point, List<Particle> unprocessed, List<Particle> ordered){
        point.opticsProcessed = true;
        unprocessed.remove(point);
        ordered.add(point);
    }

    private void update(List<Particle> neighbors, Particle point, List<Particle> seeds){
        for (Particle n : neighbors){
            if(!n.opticsProcessed){
                double newRD = Math.max(point.opticsCD, getHypotenuse(point, n));
                if (n.opticsRD == -1) {
                    n.opticsRD = newRD;
                    seeds.add(n);
                } else if (newRD < n.opticsRD){
                    n.opticsRD = newRD;
                }
            }
        }

    }

    private void run(){
        for (int i= 0; i < this.subRois.size(); i++){

            setup(this.subRois.get(i));
            System.out.println("Size of the unprocessed array: " + this.unprocessed.size());
            System.out.println("Size of the unprocessed subarray: " + this.unprocessed.get(i).size());
            System.out.println("Size of the subROIs: " + this.subRois.size());
            System.out.println("Size of the subRoi array: " + this.subRois.get(i).size());
            }

            Thread runOptics = new Thread(() -> {
                for (int i= 0; i < this.unprocessed.size(); i++){
                    opticsProgress.setMaximum(this.unprocessed.get(i).size());
                    int counts = 0;
                    while(this.unprocessed.get(i).size() > 0){
                        Particle point = this.unprocessed.get(i).get(0);
                        processed(point, this.unprocessed.get(i), this.ordered.get(i));

                        List<Particle> pointNeighbors = neighbors(point, this.subRois.get(i));

                        if (pointNeighbors.size() > 0) {
//                    System.out.println("Neighbor size. " + pointNeighbors.size());
                            counts += pointNeighbors.size();
                        }



                        if (coreDistance(point, pointNeighbors) > 0){
                            List<Particle> seeds = new ArrayList<>();
                            update(pointNeighbors, point, seeds);

                            while(seeds.size() > 0) {
                                Collections.sort(seeds, new RDComparator());
                                try{
                                    Particle n = seeds.get(0);
                                    seeds.remove(0);

                                    processed(n, this.unprocessed.get(i), this.ordered.get(i));
                                    List<Particle> nNeighbors = neighbors(n, this.subRois.get(i));

                                    if (coreDistance(n, nNeighbors) > 0){
                                        update(nNeighbors, n, seeds);
                                    }
                                }

                                catch (IndexOutOfBoundsException ignored) {
                                }

                                opticsProgress.setValue(this.ordered.get(i).size());
                                opticsProgress.setString("Processing ROI " + (i+1) + ": " + (int) (opticsProgress.getPercentComplete()*100) + "%");
                                opticsProgress.repaint();
                            }

                        }

                    }
                    System.out.println("Counts: " + counts);
                    opticsProgress.setString("Done!");
                    DrCorrGUI.status.setText("OPTICS, so slow");

                    try(PrintWriter write = new PrintWriter((this.currentDir.getParentFile() + "\\OPTICS_plot_" + i + ".txt"))){
                        for (int j = 0; j < this.ordered.get(i).size(); j++){
                            write.println(String.format(Locale.US, "%.2f", this.ordered.get(i).get(j).opticsRD));
                        }
                    } catch (FileNotFoundException e1){
                        e1.printStackTrace();
                    }

                }
            });

            runOptics.start();


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        switch (action){
            case "optics":



                this.particles = DrCorrGUI.getParticles();
                List<ROIManager.ROIs> rois = DrCorrGUI.getRois();
                this.currentDir = DrCorrGUI.getCurrentDir();

                this.subRois.clear();
                this.unprocessed.clear();
                this.ordered.clear();

                for (Particle p : this.particles) {
                    p.opticsCD = -1;
                    p.opticsRD = -1;
                    p.opticsProcessed = false;
                }

                if (rois.size() == 0) {
                    ArrayList<Particle> subListOfParticles = new ArrayList<>();
                    subListOfParticles.addAll(particles);
                    this.subRois.add(subListOfParticles);
                    System.out.println("No ROIs selected, all localizations are taken to the analysis\n");
                    System.out.println("Number of events " + this.subRois.get(0).size());
                } else {
                    for (ROIManager.ROIs r : rois) {
                        ArrayList<Particle> subListOfParticles = new ArrayList<>();
                        populateSubROI(r, subListOfParticles);
                        this.subRois.add(subListOfParticles);
                    }
                }


                this.maxRadius = Double.parseDouble(eps.getText());
                this.minPoints = Integer.parseInt(minPts.getText());
                System.out.println("Still under development");
                System.out.println("Max radius is: " + this.maxRadius);
                System.out.println("Min points is: " + this.minPoints);

                run();

                System.out.println("done!");

                break;
        }

    }
}

class RDComparator implements Comparator<Particle>{

    @Override
    public int compare(Particle o1, Particle o2) {
        if (o1.opticsRD < o2.opticsRD) return -1;
        else if (o1.opticsRD == o2.opticsRD) return 0;
        else return 1;
    }
}