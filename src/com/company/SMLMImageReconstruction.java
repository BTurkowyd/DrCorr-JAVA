package com.company;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;

import javax.swing.*;
import java.awt.*;

public class SMLMImageReconstruction extends JFrame {

    private String title;
    private float[][] beforeDrCorr;
    private float[][] afterDrCorr;
    public JFreeChart chartBefore;
    public JFreeChart chartAfter;
    float maxX;
    float maxY;

    public SMLMImageReconstruction(String title) {
        this.title = title;
        System.out.println("SMLM Particle size is: " + DrCorrGUI.getParticles().size());

    }

    private void getMaxXY(){
        for (Particle p : DrCorrGUI.getParticles()) {
            if (p.getX() > this.maxX) {
                this.maxX = p.getX();
            }

            if (p.getY() > this.maxY) {
                this.maxY = p.getY();
            }
        }
    }

    public void beforePrint() {

        this.getMaxXY();
        System.out.println(this.maxX + " " + this.maxY);

        /** The beforeDrCorr. */
        this.beforeDrCorr = new float[2][DrCorrGUI.getParticles().size()];

        for (int i=0; i < DrCorrGUI.getParticles().size(); i++) {
            this.beforeDrCorr[0][i] = DrCorrGUI.getParticles().get(i).getX();
            this.beforeDrCorr[1][i] = DrCorrGUI.getParticles().get(i).getY();
        }

        final NumberAxis domainAxis = new NumberAxis("X [nm]");
        domainAxis.setRange(0, this.maxX);
        final NumberAxis rangeAxis = new NumberAxis("Y [nm]");
        rangeAxis.setRange(0, this.maxY);

        final FastScatterPlot plot = new FastScatterPlot(this.beforeDrCorr, domainAxis, rangeAxis);
        plot.getRangeAxis().setInverted(true);
        plot.getDomainAxis().setVisible(false);
        plot.getRangeAxis().setVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setBackgroundPaint(Color.BLACK);
        plot.setPaint(Color.GREEN);

        this.chartBefore = new JFreeChart(this.title, plot);
        chartBefore.getTitle().setVisible(false);



        // force aliasing of the rendered content..
        chartBefore.getRenderingHints().put
                (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final ChartPanel panel = new ChartPanel(chartBefore, true);

        panel.setPreferredSize(new java.awt.Dimension(1280, 1060));
        panel.setMinimumDrawHeight(100);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(100);
        panel.setMaximumDrawWidth(2000);

        setContentPane(panel);

    }

    public void afterPrint() {

        /** The beforeDrCorr. */
        this.afterDrCorr = new float[2][DrCorrGUI.getParticles().size()];

        for (int i=0; i < DrCorrGUI.getParticles().size(); i++) {
            this.afterDrCorr[0][i] = DrCorrGUI.getParticles().get(i).getNewX();
            this.afterDrCorr[1][i] = DrCorrGUI.getParticles().get(i).getNewY();
        }

        final NumberAxis domainAxis = new NumberAxis("X [nm]");
        domainAxis.setRange(0, (int) (RescalingFactor.rescalingFactorX*1280*10));
        final NumberAxis rangeAxis = new NumberAxis("Y [nm]");
        rangeAxis.setRange(0, (int) (RescalingFactor.rescalingFactorY*1060*10));

        final FastScatterPlot plot = new FastScatterPlot(this.afterDrCorr, domainAxis, rangeAxis);
        plot.getRangeAxis().setInverted(true);
        plot.getDomainAxis().setVisible(false);
        plot.getRangeAxis().setVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setBackgroundPaint(Color.BLACK);
        plot.setPaint(Color.RED);

        this.chartAfter = new JFreeChart(this.title, plot);
        chartAfter.getTitle().setVisible(false);


        // force aliasing of the rendered content..
        chartBefore.getRenderingHints().put
                (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final ChartPanel panel = new ChartPanel(chartAfter, true);

        panel.setPreferredSize(new java.awt.Dimension((int) this.maxX/10, (int) this.maxY/10));
        panel.setMinimumDrawHeight(100);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(100);
        panel.setMaximumDrawWidth(2000);

        setContentPane(panel);

    }


}
