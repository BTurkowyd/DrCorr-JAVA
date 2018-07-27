package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class TempNeNAFrame extends JFrame implements ActionListener {

    private List<Particle> particles;
    private List<ROIManager.ROIs> rois;
    private ROIManager image;
    private JTextArea initFrame;
    private JTextArea lastFrame;
    private JTextArea jumpFrame;
    private JTextArea windowSize;
    private File currentDir;
    private Image rescaledImage;


    TempNeNAFrame(String title, List<Particle> particles, List<ROIManager.ROIs> rois, ROIManager image, File currentDir, Image rescaledImage) throws HeadlessException {
        this.particles = particles;
        this.rois = rois;
        this.image = image;
        this.currentDir = currentDir;
        this.rescaledImage = rescaledImage;

        System.out.println(particles.size());

        JFrame tempNeNAframe = new JFrame(title);
        tempNeNAframe.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        JPanel panel = new JPanel();
        DrCorrGUI.MyLayoutManager mgr = new DrCorrGUI.MyLayoutManager();
        panel.setLayout(mgr);
        panel.setPreferredSize(new Dimension(600, 250));


        initFrame = new JTextArea();
        lastFrame = new JTextArea();
        jumpFrame = new JTextArea();
        windowSize = new JTextArea();

        JLabel initFrameLabel = new JLabel("First frame");
        JLabel lastFrameLabel = new JLabel("Last frame");
        JLabel jumpFrameLabel = new JLabel("Increment (fr)");
        JLabel windowSizeLabel = new JLabel("Window size (fr)");

        initFrame.setText("0");
        if (particles.isEmpty()) {
            lastFrame.setText("0");
        } else {

            lastFrame.setText(Integer.toString((int) particles.get(particles.size() - 1).getTime()));
        }

        jumpFrame.setText("0");
        windowSize.setText("0");

        Font font = new Font("Arial", Font.PLAIN, 20);
        initFrame.setFont(font);
        lastFrame.setFont(font);
        jumpFrame.setFont(font);
        windowSize.setFont(font);

        JButton nena = new JButton("NeNA");

        nena.addActionListener(this);
        nena.setActionCommand("nena");

        panel.add(initFrame, new Rectangle(20, 60, 150, 40));
        panel.add(lastFrame, new Rectangle(180, 60, 150, 40));
        panel.add(jumpFrame, new Rectangle(20, 170, 150, 40));
        panel.add(windowSize, new Rectangle(180, 170, 150, 40));

        panel.add(initFrameLabel, new Rectangle(20, 10, 170, 40));
        panel.add(lastFrameLabel, new Rectangle(180, 10, 170, 40));
        panel.add(jumpFrameLabel, new Rectangle(20, 120, 170, 40));
        panel.add(windowSizeLabel, new Rectangle(180, 120, 170, 40));

        panel.add(nena, new Rectangle(360, 60, 200, 150));

        tempNeNAframe.add(panel);
        tempNeNAframe.pack();
        SwingUtilities.updateComponentTreeUI(tempNeNAframe);
        tempNeNAframe.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        switch (action) {
            case "nena":

                this.particles = DrCorrGUI.getParticles();
                this.rois = DrCorrGUI.getRois();
                this.image = DrCorrGUI.getImage();
                this.currentDir = DrCorrGUI.getCurrentDir();
                this.rescaledImage = DrCorrGUI.getRescaledImage();

                if (Integer.parseInt(this.jumpFrame.getText()) == 0 || Integer.parseInt(this.windowSize.getText()) == 0 || Integer.parseInt(this.lastFrame.getText()) == 0) {
                    System.out.println("Wrong parameters for frames");
                } else {
                    image.generateTempNeNAs(rois, particles, this.initFrame, this.lastFrame, this.jumpFrame, this.windowSize);
                }

                int countNeNA = 0;
                if (!DrCorrGUI.nenaCorrectionTerms){
                    for (int i = 0; i < image.subNenaList.size(); i++) {
                        try (PrintWriter write2 = new PrintWriter(currentDir.getParentFile() + "\\Tempoeral_NeNAs_" + countNeNA + ".txt")) {
                            write2.println("First frame: " + this.initFrame.getText() + "; last frame: " + this.lastFrame.getText() +
                                    "; increment (frames): " + this.jumpFrame.getText() + "; window size: " + this.windowSize.getText());
                            for (int j = 0; j < image.subNenaList.get(i).size(); j++) {
                                write2.println(image.subNenaList.get(i).get(j).NeNAValue);
                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }

                        try (PrintWriter write3 = new PrintWriter(currentDir.getParentFile() + "\\Temporal_NeNA_histogram_" + countNeNA + ".txt")) {
                            write3.println("Localization precision is: " + image.subNenaList.get(i).get(0).NeNAValue + " nm.");
                            for (int j = 0; j < 150; j++) {
                                write3.println(image.subNenaList.get(i).get(0).nenaHistogram[j][0] + "\t" + image.subNenaList.get(i).get(0).nenaHistogram[j][1] + "\t" + image.subNenaList.get(i).get(0).nenaHistogram[j][2]);
                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        countNeNA++;
                    }
                } else {
                    for (int i = 0; i < image.subNuNenaList.size(); i++) {
                        try (PrintWriter write2 = new PrintWriter(currentDir.getParentFile() + "\\Tempoeral_NeNAs_corr_free" + countNeNA + ".txt")) {
                            write2.println("First frame: " + this.initFrame.getText() + "; last frame: " + this.lastFrame.getText() +
                                    "; increment (frames): " + this.jumpFrame.getText() + "; window size: " + this.windowSize.getText());
                            for (int j = 0; j < image.subNuNenaList.get(i).size(); j++) {
                                write2.println(image.subNuNenaList.get(i).get(j).NUNeNAvalue);
                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }

                        try (PrintWriter write3 = new PrintWriter(currentDir.getParentFile() + "\\Temporal_NeNA_corr_free_histogram_" + countNeNA + ".txt")) {
                            write3.println("Localization precision is: " + image.subNuNenaList.get(i).get(0).NUNeNAvalue + " nm.");
                            for (int j = 0; j < 150; j++) {
                                write3.println(image.subNuNenaList.get(i).get(0).nunenaHistogram[j][0] + "\t" + image.subNuNenaList.get(i).get(0).nunenaHistogram[j][1] + "\t" + image.subNuNenaList.get(i).get(0).nunenaHistogram[j][2]);
                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        countNeNA++;
                    }
                }




                BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics g = bufferedImage.getGraphics();
                g.drawImage(rescaledImage,0,0, null);
                g.setColor(Color.CYAN);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                for (int i=0; i < rois.size(); i++){
                    g.drawString(Integer.toString(i+1), (int) rois.get(i).x + 2, (int) rois.get(i).y + 20);
                    g.drawRect((int) rois.get(i).x, (int) rois.get(i).y, (int) (rois.get(i).x2 - rois.get(i).x), (int) (rois.get(i).y2 - rois.get(i).y));
                }
                try {
                    ImageIO.write(bufferedImage, "png", new File(currentDir.getParentFile() + "\\Temporal_NeNA_areas.png"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                image.nenaList.clear();
                image.subNenaList.clear();
                image.nunenaList.clear();
                image.subNuNenaList.clear();
                DrCorrGUI.status.setText("Temp NeNA, wow...");
                break;
        }
    }
}
