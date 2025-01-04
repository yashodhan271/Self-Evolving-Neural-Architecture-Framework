package com.neuroevolution;

import com.neuroevolution.core.Genome;
import com.neuroevolution.core.Population;
import com.neuroevolution.visualization.NetworkVisualizer;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

public class NeuroEvolutionDemo {
    private static final int POPULATION_SIZE = 20;
    private static final int INPUT_SIZE = 784;  // MNIST image size 28x28
    private static final int OUTPUT_SIZE = 10;  // MNIST has 10 classes
    private static final int MAX_GENERATIONS = 50;
    private static final int BATCH_SIZE = 128;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting NeuroEvolution Demo...");
        
        try {
            // Initialize visualization
            NetworkVisualizer visualizer = new NetworkVisualizer();
            visualizer.setVisible(true);
            System.out.println("Visualization window created");

            // Create initial population
            System.out.println("Creating initial population of size " + POPULATION_SIZE);
            Population population = new Population(POPULATION_SIZE, INPUT_SIZE, OUTPUT_SIZE);

            // Load MNIST dataset
            System.out.println("Loading MNIST dataset...");
            DataSetIterator trainData = new MnistDataSetIterator(BATCH_SIZE, true, 12345);  // Fixed constructor
            DataSetIterator testData = new MnistDataSetIterator(BATCH_SIZE, false, 12345);  // Fixed constructor
            System.out.println("Dataset loaded successfully");

            // Evolution loop
            for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
                System.out.println("\nGeneration " + generation + " started");

                // Evaluate each genome
                double totalFitness = 0;
                int evaluatedGenomes = 0;
                
                for (Genome genome : population.getGenomes()) {
                    try {
                        MultiLayerNetwork network = new MultiLayerNetwork(genome.toMultiLayerConfiguration());
                        network.init();
                        System.out.println("Network initialized for genome " + evaluatedGenomes);

                        // Train for one epoch
                        trainData.reset();
                        int batchCount = 0;
                        while (trainData.hasNext() && batchCount < 10) { // Limit batches for testing
                            network.fit(trainData.next());
                            batchCount++;
                            if (batchCount % 5 == 0) {
                                System.out.println("Processed " + batchCount + " batches");
                            }
                        }

                        // Evaluate on test set
                        testData.reset();
                        Evaluation eval = network.evaluate(testData);
                        double fitness = eval.accuracy();
                        genome.setFitness(fitness);
                        totalFitness += fitness;
                        evaluatedGenomes++;
                        
                        System.out.println("Genome " + evaluatedGenomes + " evaluated. Fitness: " + fitness);
                    } catch (Exception e) {
                        System.err.println("Error evaluating genome: " + e.getMessage());
                        e.printStackTrace();
                        genome.setFitness(0.0); // Assign zero fitness to failed genomes
                    }
                }

                // Get best performing genome
                Genome bestGenome = population.getBestGenome();
                System.out.println("Generation " + generation + " complete");
                System.out.println("Average fitness: " + (totalFitness / evaluatedGenomes));
                System.out.println("Best fitness: " + bestGenome.getFitness());

                // Update visualization
                visualizer.updateVisualization(bestGenome);
                System.out.println("Visualization updated");

                // Evolve population
                population.evolve();
                System.out.println("Population evolved");

                // Reset iterators for next generation
                trainData.reset();
                testData.reset();
            }
        } catch (Exception e) {
            System.err.println("Fatal error in NeuroEvolution Demo:");
            e.printStackTrace();
        }
    }
}
