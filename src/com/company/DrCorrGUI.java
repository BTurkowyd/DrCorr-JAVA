package com.company;


import org.jfree.chart.ChartUtilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

class DrCorrGUI implements ActionListener {

    private static List<Particle> particles = new ArrayList<>();
    private static List<ROIManager.ROIs> rois;
    private static ROIManager roiManager;
    private JFileChooser file;
    private JTextArea intensityThres;
    static JLabel status;
    private List<String> localizationFile;
    private static Image rescaledImage;
    private static ROIManager image;
    private static File currentDir = new File(System.getProperty("user.dir"));
    private JButton closeImage;
    private JComboBox<String> fileType = new JComboBox<>(new String[]{"RapidStorm", "ThunderStorm"});
    private String fileExtension = ".txt";
    private static JProgressBar progressBar = new JProgressBar();
    private JCheckBox nenaCorrTerms;
    static boolean nenaCorrectionTerms = false;
    private SMLMImageReconstruction imageReconstruction = new SMLMImageReconstruction("Image reconstruction");


    DrCorrGUI() {
    }

    static List<Particle> getParticles() {
        return particles;
    }

    static List<ROIManager.ROIs> getRois() {
        return rois;
    }

    static ROIManager getImage() {
        return image;
    }

    static File getCurrentDir() {
        return currentDir;
    }

    static Image getRescaledImage() {
        return rescaledImage;
    }

    /**
     * Generates the GUI of the software
     */
    void createAndShowGUI() {

        JFrame frame = new JFrame(Main.class.getSimpleName());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Dogetor Corr");
        ImageIcon icon = new ImageIcon(getClass().getResource("/doge.jpg"));
        frame.setIconImage(icon.getImage());
        JLabel logoLabel = new JLabel();
        ImageIcon logo = new ImageIcon(getClass().getResource("/Doge.png"));
        logoLabel.setIcon(new ImageIcon(logo.getImage()));
        logoLabel.setBounds(new Rectangle(280, 140, 255, 306));
        frame.add(logoLabel);
        /*
          Create buttons
         */
        JButton openImage = new JButton("Load data");
        JButton removeROI = new JButton("Del. last ROI");
        JButton removeAllROIs = new JButton("Del. all ROIs");
        JButton generateFids = new JButton("Dr. corr.");
        this.nenaCorrTerms = new JCheckBox("Ignore corr. terms in NeNA");
        JButton nena = new JButton("NeNA");
        JButton neNa2 = new JButton("NeNa2");

        JButton temporalNeNA = new JButton("Temp. NeNA");
        JButton exitButton = new JButton("Exit");
        JButton dbScan = new JButton("DBScan");
        JButton optics = new JButton("OPTICS");

        JLabel threshold = new JLabel("Fiducial intensity (only drift)");
        this.intensityThres = new JTextArea();
        intensityThres.setFont(new Font("Arial", Font.PLAIN, 20));
        intensityThres.setText("10000");



        status = new JLabel("Ready to go!");
        status.setFont(new Font("Comic Sans MS", Font.BOLD, 24));

        JLabel copyright = new JLabel("\u00A9 2018 Bartosz Turkowyd, Max Planck Institut für terrestrische Mikrobiologie");
        copyright.setFont(new Font("Arial", Font.PLAIN, 16));

        /*
          Add actions listeners to buttons
         */
        openImage.addActionListener(this);
//        importLocFile.addActionListener(this);
        removeROI.addActionListener(this);
        removeAllROIs.addActionListener(this);
        generateFids.addActionListener(this);
        nenaCorrTerms.addActionListener(this);
        nena.addActionListener(this);
        neNa2.addActionListener(this);
        temporalNeNA.addActionListener(this);
        fileType.addActionListener(this);
        exitButton.addActionListener(this);
        dbScan.addActionListener(this);
        optics.addActionListener(this);

        openImage.setActionCommand("openImage");
        removeROI.setActionCommand("removeROI");
        removeAllROIs.setActionCommand("removeAllROIs");
        generateFids.setActionCommand("genFids");
        nenaCorrTerms.setActionCommand("nenacorrterms");
        nena.setActionCommand("nena");
        neNa2.setActionCommand("nunena");
        temporalNeNA.setActionCommand("nenaInTime");
        fileType.setActionCommand("filetype");
        exitButton.setActionCommand("exitButton");
        dbScan.setActionCommand("dbscan");
        optics.setActionCommand("optics");



        JPanel panel = new JPanel();
        MyLayoutManager mgr = new MyLayoutManager();
        panel.setLayout(mgr);

        /*
         * Places buttons into the root window.
         */
        panel.setPreferredSize(new Dimension(580, 800));
        panel.add(openImage, new Rectangle(20, 220, 150, 40));
        panel.add(removeROI, new Rectangle(20, 280, 150, 40));
        panel.add(removeAllROIs, new Rectangle(20, 340, 150, 40));
        panel.add(generateFids, new Rectangle(20, 400, 150, 40));
        panel.add(nenaCorrTerms, new Rectangle(20, 470, 300, 40));
        panel.add(nena, new Rectangle(20, 520, 150, 40));
        panel.add(neNa2, new Rectangle(20, 580, 150, 40));
        panel.add(temporalNeNA, new Rectangle(185, 580,150,40));
        panel.add(fileType, new Rectangle(20, 40, 180, 40));
        panel.add(exitButton, new Rectangle(350, 640, 150, 40));
        panel.add(threshold, new Rectangle(20, 110, 300, 40));
        panel.add(intensityThres, new Rectangle(20, 160, 150, 40));
        panel.add(status, new Rectangle(295, 505, 255, 60));
        panel.add(dbScan, new Rectangle(20, 640, 150, 40));
        panel.add(optics, new Rectangle(185, 640, 150, 40));
        panel.add(progressBar, new Rectangle(20, 700, 540,60));
        panel.add(copyright, new Rectangle(20, 740, 620, 80));

        neNa2.setEnabled(false);
//        optics.setEnabled(false);

        /*
         * Creates a menu bar on the top of the root window. Adds the Close7Open image window
         */
        JMenuBar mb = new JMenuBar();
        this.closeImage = new JButton("Hide image");
        mb.add(closeImage);
        closeImage.addActionListener(this);
        closeImage.setActionCommand("closeImage");
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        frame.setJMenuBar(mb);

        frame.add(panel);
        frame.pack();
        SwingUtilities.updateComponentTreeUI(frame);
        frame.setVisible(true);
    }

    /*
     * Button event listener
     * @param e Button event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case "openImage":
                Thread loadData = new Thread(() -> {
                    openImage();

                    rois = image.getRois();
                    roiManager = image;
                    System.out.println("Image is opened");

                    particles.clear();
                    openLocFile();
                    System.out.println("Localization file is imported");
                    int counter = 0;
                    for (String ignored : localizationFile) {
                        counter++;
                    }
                    System.out.println(counter);
                    progressBar.setMaximum(counter);

                    if (fileType.getSelectedItem() == "RapidStorm") {
                        for (int i = 1; i < counter; i++) {
                            String[] splitLines = localizationFile.get(i).split(" ");
                            particles.add(new Particle(Float.parseFloat(splitLines[0]), Float.parseFloat(splitLines[1]),
                                    Float.parseFloat(splitLines[2]), Float.parseFloat(splitLines[3])));

                            progressBar.setValue(i);
                            progressBar.setString("Loading data: " +  (int) (progressBar.getPercentComplete()*100) + "%");
                        }
                        System.out.println("Number of localizations: " + particles.size());
                        for (Particle p : particles) {
                            p.rescale();
                        }

                    } else {
                        // Improve Particle and ThunderParticle classes, so you don't have to create two arrays.
                        for (int i = 1; i < counter; i++) {
                            String[] splitLines = localizationFile.get(i).split(",");
                            particles.add(new Particle(Float.parseFloat(splitLines[2]), Float.parseFloat(splitLines[3]),
                                    Float.parseFloat(splitLines[1]), Float.parseFloat(splitLines[5]), Float.parseFloat(splitLines[0]), Float.parseFloat(splitLines[4]),
                                    Float.parseFloat(splitLines[6]), Float.parseFloat(splitLines[7]), Float.parseFloat(splitLines[8]), Float.parseFloat(splitLines[9])));

                            progressBar.setValue(i);
                            progressBar.setString("Loading data: " +  (int) (progressBar.getPercentComplete()*100) + "%");
                        }


                        System.out.println("Number of localizations: " + particles.size());
                        for (Particle p : particles) {
                            p.rescale();
                        }

                    }


                    for (int i = 0; i < 10; i++) {
                        System.out.println(particles.get(i).getX() + "--" + particles.get(i).getY() + "--" + particles.get(i).getTime() + "--" + particles.get(i).getIntensity() + "--");
                    }

                    status.setText("Data loaded, WOW!");
                    progressBar.setString("Localization file imported!");

                    imageReconstruction.beforePrint();
                    imageReconstruction.setUndecorated(true);
                    imageReconstruction.pack();
                    imageReconstruction.setVisible(true);
                    imageReconstruction.dispose();


                    try {
                        ChartUtilities.saveChartAsPNG(new File(currentDir.getParentFile() + "\\beforeDriftCorr.png"), imageReconstruction.chartBefore, 1280, 1060);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }



                });

                loadData.start();

                break;
            case "removeROI":
                if (rois.size() == 0) {
                    System.out.println("No ROIs saved. Mark with mouse an interesting region, ROI will be automatically added to list. ");
                } else {
                    rois.remove(rois.size() - 1);
                    roiManager.rectangles.remove(roiManager.rectangles.size() - 1);
                    image.setVisible(false);
                    image.setVisible(true);
                    System.out.println("ROI removed");
                }

                break;
            case "removeAllROIs":
                rois.clear();
                roiManager.rectangles.clear();
                image.setVisible(false);
                image.setVisible(true);
                System.out.println("All ROIs are removed. You can start again. ");


                break;
            case "genFids":
                Thread driftCorrect = new Thread(() -> {
                    float threshold = Float.parseFloat(this.intensityThres.getText());
                    image.generateFiducials(rois, particles, threshold);
                    Particle.loadDrift();

                    int progressCounter = 0;
                    for (Particle p : particles) {
                        p.driftCorrection();
                        progressCounter++;
                        progressBar.setValue(progressCounter);
                        progressBar.setString("Drift correction applied: " +  (int) (progressBar.getPercentComplete()*100) + "%");
                    }
                    System.out.println("Drift correction is done!");
                    progressBar.setString("Drift correction is done!");


                    try (PrintWriter writer = new PrintWriter(currentDir.getParentFile() + "\\drift_corrected" + fileExtension)) {
                        writer.println(localizationFile.get(0));

                        if (fileType.getSelectedItem() == "RapidStorm") {
                            for (Particle p : particles) {
                                writer.println(String.format(Locale.US, "%.1f %.1f %.0f %.0f", p.getNewX(), p.getNewY(), p.getTime(), p.getIntensity()));
                            }
                        } else {
                            for (Particle particle : particles) {
                                writer.println(particle.getId() + "," + particle.getTime() + "," + particle.getNewX() + "," + particle.getNewY() + "," +
                                        particle.getSigma() + "," + particle.getIntensity() + "," + particle.getOffset() + "," +
                                        particle.getBkgstd() + "," + particle.getChi2() + "," + particle.getUncertainity_xy());
                            }

                        }
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }


                    try (PrintWriter writer = new PrintWriter(currentDir.getParentFile() + "\\drift_trace.txt")) {
                        writer.println("x [nm]\ty [nm]");
                        for (int i=0; i < ROIManager.getSmoothDrift().length; i++){
                            writer.println(String.format(Locale.US, "%.2f\t%.2f", ROIManager.getSmoothDrift()[i][0], ROIManager.getSmoothDrift()[i][1]));
                        }
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }

                    BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics g = bufferedImage.getGraphics();
                    g.drawImage(rescaledImage,0,0, null);
                    g.setColor(Color.MAGENTA);
                    g.setFont(new Font("Arial", Font.BOLD, 20));
                    for (int i=0; i < rois.size(); i++){
                        g.drawString(Integer.toString(i+1), (int) rois.get(i).x + 2, (int) rois.get(i).y + 20);
                        g.drawRect((int) rois.get(i).x, (int) rois.get(i).y, (int) (rois.get(i).x2 - rois.get(i).x), (int) (rois.get(i).y2 - rois.get(i).y));
                    }
                    try {
                        ImageIO.write(bufferedImage, "png", new File(currentDir.getParentFile() + "\\Selected_fiducials.png"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    imageReconstruction.afterPrint();
                    imageReconstruction.setUndecorated(true);
                    imageReconstruction.pack();
                    imageReconstruction.setVisible(true);
                    imageReconstruction.dispose();


                    try {
                        if (fileType.getSelectedItem() == "RapidStorm") {
                            ChartUtilities.saveChartAsPNG(new File(currentDir.getParentFile() + "\\afterDriftCorr_RapidStorm.png"), imageReconstruction.chartAfter, (int) (RescalingFactor.rescalingFactorX*1280), (int) (RescalingFactor.rescalingFactorY*1060)-100);
                        } else {
                            ChartUtilities.saveChartAsPNG(new File(currentDir.getParentFile() + "\\afterDriftCorr_ThunderStorm.png"), imageReconstruction.chartAfter, (int) (RescalingFactor.rescalingFactorX*1280), (int) (RescalingFactor.rescalingFactorY*1060));

                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }




                    particles = new ArrayList<>();
                    image.setVisible(false);

                    status.setText("Drift, so much!");

                });

                driftCorrect.start();

                break;
            case "nenacorrterms":
                if (nenaCorrTerms.isSelected()){
                    nenaCorrectionTerms = true;
                    System.out.println("True");
                } else {
                    nenaCorrectionTerms = false;
                    System.out.println("False");
                }



                break;
            case "nena":
                if (!nenaCorrectionTerms) {
                    image.generateNeNAs(rois, particles);

                    int countNeNA = 1;
                    for (NeNA n : image.nenaList) {
                        try (PrintWriter write2 = new PrintWriter(currentDir.getParentFile() + "\\NeNA_distances_list_" + countNeNA + ".txt")) {
                            write2.println(String.format(Locale.US, "Localization precision is: " + "%.2f" + " nm.", n.NeNAValue));
                            for (Particle p : n.subROI) {
                                if (p.nnDist != -1) {
                                    write2.println(p.nnDist + "\t");
                                }
                            }
                        } catch (FileNotFoundException e2) {
                            e2.printStackTrace();
                        }

                        try (PrintWriter write3 = new PrintWriter(currentDir.getParentFile() + "\\NeNA_histogram_" + countNeNA + ".txt")) {
                            write3.println("Localization precision is: " + n.NeNAValue + " nm.");
                            for (int i = 0; i < 150; i++) {
                                write3.println(n.nenaHistogram[i][0] + "\t" + n.nenaHistogram[i][1] + "\t" + n.nenaHistogram[i][2]);
                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        countNeNA++;
                    }

                    try (PrintWriter write4 = new PrintWriter((currentDir.getParentFile() + "\\NeNA_table.txt"))) {
                        for (int i = 0; i < image.nenaList.size(); i++) {
                            write4.println(String.format(Locale.US, "%.2f", image.nenaList.get(i).NeNAValue));
                        }
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }

                    BufferedImage bufferedImage2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics g2 = bufferedImage2.getGraphics();
                    g2.drawImage(rescaledImage,0,0, null);
                    g2.setColor(Color.GREEN);
                    g2.setFont(new Font("Arial", Font.BOLD, 20));
                    for (int i=0; i < rois.size(); i++){
                        g2.drawString(Integer.toString(i+1), (int) rois.get(i).x + 2, (int) rois.get(i).y + 20);
                        g2.drawRect((int) rois.get(i).x, (int) rois.get(i).y, (int) (rois.get(i).x2 - rois.get(i).x), (int) (rois.get(i).y2 - rois.get(i).y));
                    }
                    try {
                        ImageIO.write(bufferedImage2, "png", new File(currentDir.getParentFile() + "\\NeNA_areas.png"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    image.nenaList.clear();
                    System.out.println(particles.size());

                    status.setText("NeNA, so good!");
                } else {
                    image.generateNUNeNas(rois, particles);

                    int countNUNeNA = 0;
                    for (NUNeNA n : image.nunenaList) {
                        try (PrintWriter write = new PrintWriter(currentDir.getParentFile() + "\\NeNA_corr_free_distances_list_" + countNUNeNA + ".txt")){
                            write.println(String.format(Locale.US, "Localization precision (NUNeNA) is: " + "%.2f" + " nm.", n.NUNeNAvalue));
                            for (Particle p : n.subROI) {
                                if (p.nnDist != -1){
                                    write.println(p.nnDist +"\t");
                                }
                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }

                        try (PrintWriter write = new PrintWriter(currentDir.getParentFile() + "\\NeNA_corr_free_histogram_" + countNUNeNA + ".txt")) {
                            write.println(String.format(Locale.US, "Localization precision (NUNeNA) is: " + "%.2f" + " nm.", n.NUNeNAvalue));
                            for (int i=0; i < n.nunenaHistogram.length; i++) {
                                write.println(n.nunenaHistogram[i][0] + "\t" + n.nunenaHistogram[i][1] + "\t" + n.nunenaHistogram[i][2]);
                            }
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        countNUNeNA++;
                    }

                    try (PrintWriter write4 = new PrintWriter((currentDir.getParentFile() + "\\NeNA_corr_free_table.txt"))) {
                        for (int i = 0; i < image.nunenaList.size(); i++) {
                            write4.println(String.format(Locale.US, "%.2f", image.nunenaList.get(i).NUNeNAvalue));
                        }
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }


                    image.nunenaList.clear();
                    System.out.println(particles.size());

                    status.setText("NeNA2, so good!");
                }


                break;
            case "nunena":
                image.generateNUNeNas(rois, particles);

                int countNUNeNA = 0;
                for (NUNeNA n : image.nunenaList) {
                    try (PrintWriter write = new PrintWriter(currentDir.getParentFile() + "\\NUNeNA_distances_list_" + countNUNeNA + ".txt")){
                        write.println(String.format(Locale.US, "Localization precision (NUNeNA) is: " + "%.2f" + " nm.", n.NUNeNAvalue));
                        for (Particle p : n.subROI) {
                            if ((p.nnDist != -1) && (p.nn100thDist != -1)) {
                                write.println(p.nnDist + " " + p.nn100thDist + "\t");
                            }
                        }
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }

                    try (PrintWriter write = new PrintWriter(currentDir.getParentFile() + "\\NUNeNA_histogram_" + countNUNeNA + ".txt")) {
                        write.println(String.format(Locale.US, "Localization precision (NUNeNA) is: " + "%.2f" + " nm.", n.NUNeNAvalue));
                        for (int i=0; i < n.nunenaHistogram.length; i++) {
                            write.println(n.nunenaHistogram[i][0] + "\t" + n.nunenaHistogram[i][1] + "\t" + n.nunenaHistogram[i][2]);
                        }
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    countNUNeNA++;
                }

                try (PrintWriter write4 = new PrintWriter((currentDir.getParentFile() + "\\NUNeNA_table.txt"))) {
                    for (int i = 0; i < image.nunenaList.size(); i++) {
                        write4.println(String.format(Locale.US, "%.2f", image.nunenaList.get(i).NUNeNAvalue));
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }


                image.nunenaList.clear();
                System.out.println(particles.size());

                status.setText("NeNA2, so good!");

                break;
            case "nenaInTime":
                TempNeNAFrame neNAFrame = new TempNeNAFrame("Temporal NeNA", particles, rois, image, currentDir, rescaledImage);
                neNAFrame.setTitle("Temporal NeNA");

                break;
            case "filetype":
                System.out.println((String) fileType.getSelectedItem());
                if (fileType.getSelectedItem() == "RapidStorm") {
                    fileExtension = ".txt";
                } else {
                    fileExtension = ".csv";
                }


                break;
            case "closeImage":
                if (image.isVisible()) {
                    image.setVisible(false);
                    closeImage.setText("Show image");
                } else {
                    image.setVisible(true);
                    closeImage.setText("Hide image");
                }

                break;
            case "dbscan":
                new DBScanClustering(particles, rois, currentDir, fileType);

                break;
            case "optics":
                new OpticsClustering();

                break;
            case "exitButton":
                System.exit(0);
        }
    }

    /*
     * Open localization file (RapidStorm format)
     */
    private void openLocFile() {
        file = new JFileChooser(currentDir);
        file.setDialogTitle("Select a localization file");
        if (fileType.getSelectedItem() == "RapidStorm") {


            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "TXT file", "txt");

            file.setFileFilter(filter);


            if (file.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = file.getSelectedFile();
                try {
                    localizationFile = Files.readAllLines(Paths.get(selectedFile.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentDir = selectedFile;
            }
        } else {

            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "CSV file", "csv");

            file.setFileFilter(filter);

            if (file.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = file.getSelectedFile();
                try {
                    localizationFile = Files.readAllLines(Paths.get(selectedFile.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentDir = selectedFile;
            }

            System.out.println("Not yet...");
        }
    }

    /*
     * Open super-resolution reconstruction image (RapidStorm format)
     */
    private void openImage() {
        file = new JFileChooser(currentDir);
        file.setDialogTitle("Select an image reconstruction");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PNG Images", "png");

        file.setFileFilter(filter);

        if (file.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = file.getSelectedFile();
            ImageIcon img = new ImageIcon(selectedFile.getPath());

            Image toScaleImage = img.getImage();
            rescaledImage = toScaleImage.getScaledInstance(1280, 1060, 1);
            ImageIcon rescaledImg = new ImageIcon(rescaledImage);
            image = new ROIManager(rescaledImg);
            new RescalingFactor(img);
            currentDir = selectedFile;
        }

    }

    public static class MyLayoutManager implements LayoutManager2 {

        private final Map<Component, Rectangle> constraints = new LinkedHashMap<>();

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            if (constraints instanceof Rectangle) {
                this.constraints.put(comp, (Rectangle) constraints);
            } else {
                addLayoutComponent((String) null, comp);
            }
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0;
        }

        @Override
        public void invalidateLayout(Container target) {

        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            constraints.put(comp, comp.getBounds());
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            constraints.remove(comp);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Rectangle rect = new Rectangle();
            for (Rectangle r : constraints.values()) {
                rect = rect.union(r);
            }
            return rect.getSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            for (Map.Entry<Component, Rectangle> e : constraints.entrySet()) {
                e.getKey().setBounds(e.getValue());
            }

        }

// --Commented out by Inspection START (27.05.2018 20:07):
//        public void setConstraints(Component component, Rectangle rect) {
//            constraints.put(component, rect);
//        }
// --Commented out by Inspection STOP (27.05.2018 20:07)

//        public class MouseDragger extends MouseAdapter{
//
//            private Point lastLocation;
//            private Component draggedComponent;
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                draggedComponent = e.getComponent();
//                lastLocation = SwingUtilities.convertPoint(draggedComponent, e.getPoint(), draggedComponent.getParent());
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                lastLocation = null;
//                draggedComponent = null;
//            }
//
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                Point location = SwingUtilities.convertPoint(draggedComponent, e.getPoint(), draggedComponent.getParent());
//                if (draggedComponent.getParent().getBounds().contains(location)) {
//                    Point newLocation = draggedComponent.getLocation();
//                    newLocation.translate(location.x - lastLocation.x, location.y - lastLocation.y);
//                    newLocation.x = Math.max(newLocation.x, 0);
//                    newLocation.x = Math.min(newLocation.x, draggedComponent.getParent().getWidth() - draggedComponent.getWidth());
//                    newLocation.y = Math.max(newLocation.y, 0);
//                    newLocation.y = Math.min(newLocation.y, draggedComponent.getParent().getHeight() - draggedComponent.getHeight());
//                    setConstraints(draggedComponent, new Rectangle(newLocation, draggedComponent.getSize()));
//                    if (draggedComponent.getParent() instanceof JComponent) {
//                        ((JComponent) draggedComponent.getParent()).revalidate();
//                    } else {
//                        draggedComponent.getParent().invalidate();
//                        draggedComponent.getParent().validate();
//                    }
//                    lastLocation = location;
//                }
//
//
//            }
//
//            public void makeDraggable(Component component) {
//                component.addMouseListener(this);
//                component.addMouseMotionListener(this);
//            }
//        }
    }
}

class RescalingFactor {
    private static final float WIDTH = 1280, HEIGHT = 1060;
    static float rescalingFactorX, rescalingFactorY;

    RescalingFactor(ImageIcon image) {
        rescalingFactorX = image.getIconWidth() / WIDTH;
        rescalingFactorY = image.getIconHeight() / HEIGHT;
        System.out.println(rescalingFactorX);
        System.out.println(rescalingFactorY);
    }
}

