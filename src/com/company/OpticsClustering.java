package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OpticsClustering extends JFrame implements ActionListener {

    private List<Particle> particles;
    private List<ROIManager.ROIs> rois;
    private File currentDir;
    private JComboBox<String> fileType;
    private List<ThunderParticle> thunderParticles;
    private ArrayList<List<Particle>> subRois = new ArrayList<>();

    OpticsClustering(List<Particle> particles, List<ROIManager.ROIs> rois, File currentDir, JComboBox<String> fileType) throws HeadlessException {
        this.particles = particles;
        this.rois = rois;
        this.currentDir = currentDir;
        this.fileType = fileType;

        JFrame opticsFrame = new JFrame("OPTICS clustering");
        JPanel panel = new JPanel();
        DrCorrGUI.MyLayoutManager mgr = new DrCorrGUI.MyLayoutManager();
        panel.setLayout(mgr);
        panel.setPreferredSize(new Dimension(440, 250));

        JTextArea eps = new JTextArea();
        JLabel epsLabel = new JLabel("Upper threshold limit (nm)");

        eps.setText("1000");

        Font font = new Font("Arial", Font.PLAIN, 20);
        eps.setFont(font);

        JButton executeOPTICS = new JButton("Run OPTICS");

        executeOPTICS.addActionListener(this);
        executeOPTICS.setActionCommand("optics");

        panel.add(eps, new Rectangle(20, 60, 150, 40));
        panel.add(epsLabel, new Rectangle(20, 10, 170, 40));
        panel.add(executeOPTICS, new Rectangle(200, 60, 200, 150));

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



    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        switch (action){
            case "optics":
                System.out.println("Still under development");

                this.particles = DrCorrGUI.getParticles();
                this.thunderParticles = DrCorrGUI.getThunderParticles();
                this.rois = DrCorrGUI.getRois();
                this.currentDir = DrCorrGUI.getCurrentDir();

                this.subRois.clear();

                for (ROIManager.ROIs r : rois) {
                    ArrayList<Particle> subListOfParticles = new ArrayList<>();
                    populateSubROI(r, subListOfParticles);
                }

                break;
        }

    }
}
