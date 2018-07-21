package com.company;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class NeNA {

    List<Particle> subROI = new ArrayList<>();
    float[][] nenaHistogram;
    private ROIManager.ROIs roi;
    private List<Particle> listOfLocalizations;
    double NeNAValue;
    private double peakX = 0;
    private double peakValue = 0;

    NeNA(ROIManager.ROIs roi, List<Particle> listOfLocalizations) {
        this.roi = roi;
        this.listOfLocalizations = listOfLocalizations;
    }

    void populateSubROI() {
        for (Particle p : listOfLocalizations) {
            if ((p.getRescaledX()) > roi.x && (p.getRescaledX()) < roi.x2 &&
                    (p.getRescaledY()) > roi.y && (p.getRescaledY()) < roi.y2) {
                this.subROI.add(p);

            }
        }
        System.out.println(this.subROI.size());
        System.out.println("Populated");
    }

    void nextFramePartners() {
        for (Particle p : subROI) {
            for (Particle r : subROI) {
                if (r.getTime() == p.getTime() + 1) {
                    p.nextFramePartners.add(r);
                }
            }
        }
        System.out.println("Next frame partners found!");

    }

    void findNNs() {
        for (Particle p : subROI) {
            for (Particle r : p.nextFramePartners) {
                double nnDist = getHypotenuse(p, r);
                if (p.nnDist == -1) {
                    p.nnDist = nnDist;
                    p.nnDistToHistogram = (int) Math.round(nnDist);
                    p.nearestNeighbor = r;
                } else {
                    if (nnDist < p.nnDist) {
                        p.nnDist = nnDist;
                        p.nnDistToHistogram = (int) Math.round(nnDist);
                        p.nearestNeighbor = r;
                    }
                }
            }
        }
        System.out.println("Nearest neighbor found!");

    }

    void makeHistograms() {
        double MAXNN = 150;
        nenaHistogram = new float[(int) MAXNN+1][3];
        for (int i = 0; i <= MAXNN; i++) {
            nenaHistogram[i][0] = i;
        }

        int counter = 0;
        for (Particle p : subROI) {
            double MINNN = 0;
            if (p.nnDistToHistogram < MAXNN && p.nnDistToHistogram >= MINNN) {
                nenaHistogram[p.nnDistToHistogram][1]++;
                counter++;
            }

        }

        for (int i = 0; i <= MAXNN; i++) {
            nenaHistogram[i][1] /= counter;
        }

        for (int i=0; i <= MAXNN; i ++){
            if (nenaHistogram[i][1] > peakValue){
                peakValue = nenaHistogram[i][1];
                peakX = nenaHistogram[i][0];
            }
        }

        System.out.println(nenaHistogram[1].length);
        System.out.println("Peak value is: " + peakValue + peakX);

        NeNAFitter fitter = new NeNAFitter();
        ArrayList<WeightedObservedPoint> points = new ArrayList<>();

        for (int i = 0; i < MAXNN; i++) {
            points.add(new WeightedObservedPoint(1.0, nenaHistogram[i][0], nenaHistogram[i][1]));
        }

//        for (WeightedObservedPoint p : points){
//            System.out.println(p.getWeight() + "   " + p.getX() + "   " + p.getY());
//        }
//        System.out.println(points.size());

        final double coefficients[] = fitter.fit(points);
        System.out.println(Arrays.toString(coefficients));

        for (int i = 0; i < MAXNN; i++) {
            nenaHistogram[i][2] = (float) new NeNAFunc().value(i, coefficients[0], coefficients[1], coefficients[2], coefficients[3], coefficients[4], coefficients[5]);
        }
        this.NeNAValue = coefficients[0];
    }


    double getHypotenuse(Particle particleOfInterest, Particle otherParticles) {
        return Math.hypot(particleOfInterest.getX() - otherParticles.getX(),
                particleOfInterest.getY() - otherParticles.getY());

    }


    class NeNAFunc implements ParametricUnivariateFunction {

//        public double value(double r, double sigmaSMLM, double rc, double w, double F, double A, double O) {
//            return 0;
//        }

        @Override
        public double value(double r, double... parameters) {
            return (r / (2 * Math.pow(parameters[0], 2))) * Math.exp((-1) * r * r / (4 * Math.pow(parameters[0], 2))) * parameters[4] +
                    (parameters[3] / (parameters[2] * Math.sqrt(Math.PI / 2))) * Math.exp((-2) * Math.pow((r - parameters[1]) / parameters[2], 2)) +
                    parameters[5] * r;
        }

        @Override
        // Jacobian matrix of the above. In this case, this is just an array of
        // partial derivatives of the above function, with one element for each parameter.
        public double[] gradient(double x, double... parameters) {

            final double sigmaSMLM = parameters[0];
            final double rc = parameters[1];
            final double w = parameters[2];
            final double F = parameters[3];
            final double A = parameters[4];
            final double O = parameters[5];

            // Jacobian Matrix Edit

            // Using Derivative Structures...
            // constructor takes 3 arguments - the number of parameters in your
            // equation to be differentiated (6 in this case), the order of
            // differentiation for the DerivativeStructure, the index of the
            // parameter represented by the DS, and the value of the parameter itself

            DerivativeStructure sigmaSMLMDev = new DerivativeStructure(6, 1, 0, sigmaSMLM);
            DerivativeStructure rcDev = new DerivativeStructure(6, 1, 1, rc);
            DerivativeStructure wDev = new DerivativeStructure(6, 1, 2, w);
            DerivativeStructure FDev = new DerivativeStructure(6, 1, 3, F);
            DerivativeStructure ADev = new DerivativeStructure(6, 1, 4, A);
            DerivativeStructure ODev = new DerivativeStructure(6, 1, 5, O);

            // define the equation to be differentiated using another DerivativeStructure

            DerivativeStructure y = sigmaSMLMDev.pow(2).multiply(2).reciprocal().multiply(x).multiply((sigmaSMLMDev.pow(2)
                    .multiply(4).reciprocal().multiply(Math.pow(x, 2)).negate()).exp()).multiply(ADev).add(
                    FDev.divide(wDev.multiply(Math.sqrt(Math.PI / 2))).multiply((wDev.reciprocal().multiply(rcDev.negate().add(x))).pow(2).exp())).add(ODev.multiply(x));
//            DerivativeStructure y = ((x / (2 * Math.pow(sigmaSMLMDev, 2))) * Math.exp((-1) * x * x / (4 * Math.pow(sigmaSMLMDev, 2))) * ADev) +
//                    ((FDev / (wDev * Math.sqrt(Math.PI / 2))) * Math.exp((-2) * Math.pow((x - rcDev) / wDev, 2))) +
//                    ODev]*x;

            return new double[]{

                    y.getPartialDerivative(1, 0, 0, 0, 0, 0),
                    y.getPartialDerivative(0, 1, 0, 0, 0, 0),
                    y.getPartialDerivative(0, 0, 1, 0, 0, 0),
                    y.getPartialDerivative(0, 0, 0, 1, 0, 0),
                    y.getPartialDerivative(0, 0, 0, 0, 1, 0),
                    y.getPartialDerivative(0, 0, 0, 0, 0, 1)

            };
        }
    }

    class NeNAFitter extends AbstractCurveFitter {

        @Override
        protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {

            final int len = points.size();
            final double[] target = new double[len];
            final double[] weights = new double[len];
            final double[] initialGuess = {peakX, 15.0, 100.0, 0.5, 0.5, 0.0};

            int i = 0;
            for (WeightedObservedPoint point : points) {
                target[i] = point.getY();
                weights[i] = point.getWeight();
                i += 1;
            }

            final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(new NeNAFunc(), points);

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
