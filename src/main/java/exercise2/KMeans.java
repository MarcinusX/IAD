package exercise2;

import org.jfree.data.xy.XYSeries;
import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.dense.BasicVector;
import utils.DataSetChart;
import utils.ErrorChart;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by marcinus on 20.04.17.
 */
public class KMeans {

    int k;
    Matrix neurons;
    Matrix inputs;
    Vector ids; //dla każdego punktu reprezentuje numer neurona, który go reprezentuje.
    // Numer ten odwzorowuje indeks tego neurona w macierzy neurons

    public KMeans(Matrix inputMatrix, int k) {
        this.k = k;
        neurons = new Utils().initNeurons(inputMatrix, k);
        this.inputs = inputMatrix;
        this.ids = new BasicVector(inputMatrix.rows());
    }

    public void perform(int maxIter) {
        double diff;
        int it = 0;
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File("kmeans"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ErrorChart errorChart = new ErrorChart();
        XYSeries errorSeries = new XYSeries("Błędy");
        new File("kmeans").mkdir();
        double error = 0;
        do {
            DataSetChart dataSetChart = new DataSetChart(2);
            dataSetChart.addEntries(0, inputs);
            dataSetChart.addEntries(1, neurons);
            dataSetChart.generateChart("kmeans" + File.separator + "ex2kmeans" + it + ".jpg");
            System.out.println("generating ex2kmeans" + it);

            updateIds();
            diff = updateNeurons();
            if (it > 4) {
                removeDeadNeurons();
            }
            it++;
            error = calculateError();
            errorSeries.add(it, error);

        } while (diff > 0 && it < maxIter);
        System.out.println(error);
        errorChart.addSeries(errorSeries);
        errorChart.generateChart("kmeans" + File.separator + "error.jpg");
    }

    private double calculateError() {
        double error = 0;
        for (int i = 0; i < inputs.rows(); i++) {
            double xPunktu = inputs.get(i, 0);
            double yPunkty = inputs.get(i, 1);
            double xNeuronu = neurons.get((int) ids.get(i), 0);
            double yNeuronu = neurons.get((int) ids.get(i), 1);
            double distance = distance(xPunktu, yPunkty, xNeuronu, yNeuronu);
            error += distance;
        }
        return error / inputs.rows();
    }

    private void removeDeadNeurons() {
        HashMap<Integer, Integer> aliveNeuronIds = new HashMap<>();//Old index, new Index
        int newIndex = 0;
        //System.out.println(neurons.rows());
        for (int i = 0; i < ids.length(); i++) {
            Integer neuronId = aliveNeuronIds.get((int) ids.get(i));
            if (neuronId == null) {
                aliveNeuronIds.put((int) ids.get(i), newIndex++);
            }
        }
        //System.out.println(neurons.rows());
        if (k != aliveNeuronIds.size()) {
            k = aliveNeuronIds.size();
            Basic2DMatrix aliveNeurons = new Basic2DMatrix(k, 2);
            for (Integer i : aliveNeuronIds.keySet()) {
                //System.out.println("klucz"+i);
                aliveNeurons.set(aliveNeuronIds.get(i), 0, neurons.get(i, 0));
                aliveNeurons.set(aliveNeuronIds.get(i), 1, neurons.get(i, 1));
            }
            for (int i = 0; i < ids.length(); i++) {
                ids.set(i, aliveNeuronIds.get((int) ids.get(i)));
            }
            neurons = aliveNeurons;
        }

    }

    private double updateNeurons() {
        double totalDiff = 0;
        for (int i = 0; i < k; i++) {
            double meanX = 0;
            double meanY = 0;
            int counter = 0;
            for (int j = 0; j < ids.length(); j++) {
                if (ids.get(j) == i) {
                    meanX += inputs.get(j, 0);
                    meanY += inputs.get(j, 1);
                    counter++;
                }
            }
            if (counter > 0) {
                meanX /= counter;
                meanY /= counter;

                if (totalDiff < 10) {
                    totalDiff += Math.abs(neurons.get(i, 0) - meanX);
                    totalDiff += Math.abs(neurons.get(i, 1) - meanY);
                }
                neurons.set(i, 0, meanX);
                neurons.set(i, 1, meanY);
            }
        }
        return totalDiff;
    }

    private void updateIds() {
        for (int i = 0; i < inputs.rows(); i++) {
            double minDistance = -1;
            for (int j = 0; j < k; j++) {
                double distance = distance(neurons.get(j, 0), neurons.get(j, 1), inputs.get(i, 0), inputs.get(i, 1));
                if (minDistance == -1 || distance < minDistance) {
                    minDistance = distance;
                    ids.set(i, j);
                }
            }
        }
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
    }
}
