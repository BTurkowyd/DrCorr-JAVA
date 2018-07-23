package com.company;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NUNeNA extends NeNA {

    float[][] nunenaHistogram;
//    float[][] nunena100thHistogram;
//    Float[][] nunenaTransientHist;
//    float[][] nunenaHist;
    double NUNeNAvalue;
    private double peakX = 0;
    private double peakValue = 0;

    NUNeNA(ROIManager.ROIs roi, List<Particle> listOfLocalizations) {
        super(roi, listOfLocalizations);
    }

    @Override
    void populateSubROI() {
        super.populateSubROI();
    }

    @Override
    void nextFramePartners() {
        super.nextFramePartners();
    }

    @Override
    void findNNs() {
        super.findNNs();
    }

    void next100thFramePartners() {
        for (Particle p : subROI) {
            for (Particle r : subROI) {
                if (r.getTime() == p.getTime() + 100) {
                    p.next100thFramePartners.add(r);
                }
            }
        }
        System.out.println("Next 100th frame partners found!");

    }

    void find100thNNs() {
        for (Particle p : subROI) {
            for (Particle r : p.next100thFramePartners) {
                double nnDist = getHypotenuse(p, r);
                if (p.nn100thDist == -1) {
                    p.nn100thDist = nnDist;
                    p.nn100thDistToHistogram = (int) nnDist;
                    p.nearest100thNeighbor = r;
                } else {
                    if (nnDist < p.nn100thDist) {
                        p.nn100thDist = nnDist;
                        p.nn100thDistToHistogram = (int) nnDist;
                        p.nearest100thNeighbor = r;
                    }
                }
            }
        }
        System.out.println("Nearest neighbor found!");

    }

    void makeHist() {
        double MAXNN = 150;
        double MIN = 0;
        nunenaHistogram = new float[(int) MAXNN+1][3];
//        nunena100thHistogram = new float[(int) MAXNN+1][3];
//        nunenaTransientHist = new Float[(int) MAXNN+1][3];
        for (int i = 0; i < MAXNN; i++) {
            nunenaHistogram[i][0] = i;
//            nunena100thHistogram[i][0] = i;
//            nunenaTransientHist[i][0] = Float.valueOf(i);
        }

        int counter = 0;
        for (Particle p : subROI) {
            if (p.nnDistToHistogram < MAXNN && p.nnDistToHistogram >= MIN) {
                nunenaHistogram[p.nnDistToHistogram][1]++;
                counter++;
            }
        }

        for (int i = 0; i < MAXNN; i++) {
            nunenaHistogram[i][1] /= counter;
        }

//        int counter100 = 0;
//        for (Particle p : subROI) {
//            if (p.nn100thDistToHistogram < MAXNN && p.nn100thDistToHistogram >= MIN) {
//                nunena100thHistogram[p.nn100thDistToHistogram][1]++;
//                counter100++;
//            }
//
//        }

//        for (int i = 0; i < MAXNN; i++) {
//            nunena100thHistogram[i][1] /= counter100;
//        }
//
//        for (int i = 0; i < MAXNN; i++) {
//            if ((nunenaHistogram[i][1] - nunena100thHistogram[i][1]) >= 0){
//                nunenaTransientHist[i][1] = nunenaHistogram[i][1] - nunena100thHistogram[i][1];
//            } else {
//                nunenaTransientHist[i][0] = nunenaTransientHist[i][1] = Float.NaN;
//            }
//
//        }

//        int notNANcounter = 0;
//        for (int i=0; i < MAXNN; i++) {
//            if (!Float.isNaN(nunenaTransientHist[i][1])){
//                notNANcounter++;
//            }
//        }
//
//        nunenaHist = new float[notNANcounter][3];
//        int index = 0;
//        for (int i=0; i < MAXNN; i++) {
//            if (!Float.isNaN(nunenaTransientHist[i][1])){
//                nunenaHist[index][0] = nunenaTransientHist[i][0];
//                nunenaHist[index][1] = nunenaTransientHist[i][1];
//                index++;
//            }
//        }


        for (int i=0; i <= MAXNN; i ++){
            if (nunenaHistogram[i][1] > peakValue){
                peakValue = nunenaHistogram[i][1];
                peakX = nunenaHistogram[i][0];
            }
        }

        System.out.println(nunenaHistogram[1].length);
        System.out.println("Peak value is: " + peakValue + peakX);


        NUNeNAFitter fitter = new NUNeNAFitter();
        ArrayList<WeightedObservedPoint> points = new ArrayList<>();

        for (float[] aNunenaHistogram : nunenaHistogram) {
            points.add(new WeightedObservedPoint(1.0, aNunenaHistogram[0], aNunenaHistogram[1]));
        }

        try {
            final double coefficients[] = fitter.fit(points);
            System.out.println(Arrays.toString(coefficients));

            for (int i = 0; i < nunenaHistogram.length; i++) {
                nunenaHistogram[i][2] = (float) new NUNeNAFunc().value(nunenaHistogram[i][0], coefficients[0], coefficients[1]);
            }
            this.NUNeNAvalue = coefficients[0];

        } catch (ConvergenceException e) {
            System.out.println("Fit can't be performed");
        }

    }


    class NUNeNAFunc implements ParametricUnivariateFunction{

        @Override
        public double value(double r, double... parameters) {
            return (r / (2 * Math.pow(parameters[0], 2))) * Math.exp((-1) * r * r / (4 * Math.pow(parameters[0], 2))) * parameters[1];
        }

        @Override
        // Jacobian matrix of the above. In this case, this is just an array of
        // partial derivatives of the above function, with one element for each parameter.
        public double[] gradient(double x, double... parameters) {

            final double sigmaSMLM = parameters[0];
            final double A = parameters[1];

            // Jacobian Matrix Edit

            // Using Derivative Structures...
            // constructor takes 3 arguments - the number of parameters in your
            // equation to be differentiated (2 in this case), the order of
            // differentiation for the DerivativeStructure, the index of the
            // parameter represented by the DS, and the value of the parameter itself

            DerivativeStructure sigmaSMLMDev = new DerivativeStructure(2, 1, 0, sigmaSMLM);
            DerivativeStructure ADev = new DerivativeStructure(2, 1, 1, A);

            // define the equation to be differentiated using another DerivativeStructure

            DerivativeStructure y = sigmaSMLMDev.pow(2).multiply(2).reciprocal().multiply(x).multiply((sigmaSMLMDev.pow(2)
                    .multiply(4).reciprocal().multiply(Math.pow(x, 2)).negate()).exp()).multiply(ADev);


            return new double[]{
                    y.getPartialDerivative(1, 0),
                    y.getPartialDerivative(0, 1),
            };
        }
    }


    class NUNeNAFitter extends AbstractCurveFitter {

        @Override
        protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {

            final int len = points.size();
            final double[] target = new double[len];
            final double[] weights = new double[len];
            final double[] initialGuess = {peakX, 0.5};

            int i = 0;
            for (WeightedObservedPoint point : points) {
                target[i] = point.getY();
                weights[i] = point.getWeight();
                i += 1;
            }

            final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(new NUNeNAFunc(), points);

            return new LeastSquaresBuilder().
                    maxEvaluations(Integer.MAX_VALUE).
                    maxIterations(Integer.MAX_VALUE).
                    start(initialGuess).
                    target(target).
                    weight(new DiagonalMatrix(weights)).
                    model(model.getModelFunction(), model.getModelFunctionJacobian()).
                    build();
        }
    }


}
