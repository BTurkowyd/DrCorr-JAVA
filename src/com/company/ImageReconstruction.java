package com.company;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class ImageReconstruction extends JFrame {
    private final List<Particle> particles;


    ImageReconstruction(List<Particle> particles) {
        this.particles = particles;

        double xMax = 0;
        double yMax = 0;
        for (Particle p : particles) {
            if (xMax < p.getX()) {
                xMax = p.getX();
            }

            if (yMax < p.getY()) {
                yMax = p.getY();
            }
        }

        // Create dataset
        XYDataset dataset = createDataset();

        // Create chart
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Drift corrected image",
                "X-Axis", "Y-Axis", dataset);

        //Changes background color
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(new Color(255,255,255));
        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = plot.getRangeAxis();

        domainAxis.setRange(0, xMax);
        rangeAxis.setRange(0, yMax);


        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        setContentPane(panel);
    }

    private XYDataset createDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();

//        XYSeries series = new XYSeries("Before");
        XYSeries series2 = new XYSeries("After");

        for (Particle p : this.particles) {
//            series.add(p.getX(), p.getY());
            series2.add(p.getNewX(), p.getNewY());
        }

//        series.add(1, 72.9);
//        series.add(2, 81.6);
//        series.add(3, 88.9);
//        series.add(4, 96);
//        series.add(5, 102.1);
//        series.add(6, 108.5);
//        series.add(7, 113.9);
//        series.add(8, 119.3);
//        series.add(9, 123.8);
//        series.add(10, 124.4);
//        dataset.addSeries(series);
        dataset.addSeries(series2);

        return dataset;
    }

}
