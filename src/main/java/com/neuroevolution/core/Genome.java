package com.neuroevolution.core;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Genome {
    private List<Integer> layerSizes;
    private List<Activation> activations;
    private double fitness;
    private static final Random random = new Random();

    public Genome(int inputSize, int outputSize) {
        this.layerSizes = new ArrayList<>();
        this.activations = new ArrayList<>();
        
        // Always start with input size and end with output size
        layerSizes.add(inputSize);
        // Add random hidden layers (1-3 initially)
        int hiddenLayers = random.nextInt(3) + 1;
        for (int i = 0; i < hiddenLayers; i++) {
            layerSizes.add(random.nextInt(50) + 10); // Random size between 10 and 60 neurons
            activations.add(getRandomActivation());
        }
        layerSizes.add(outputSize);
        activations.add(Activation.SOFTMAX); // Output layer activation
    }

    public MultiLayerConfiguration toMultiLayerConfiguration() {
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .weightInit(org.deeplearning4j.nn.weights.WeightInit.XAVIER);

        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();

        // Build hidden layers
        for (int i = 0; i < layerSizes.size() - 1; i++) {
            if (i < layerSizes.size() - 2) {
                // Hidden layers
                listBuilder.layer(i, new DenseLayer.Builder()
                        .nIn(layerSizes.get(i))
                        .nOut(layerSizes.get(i + 1))
                        .activation(activations.get(i))
                        .build());
            } else {
                // Output layer
                listBuilder.layer(i, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(layerSizes.get(i))
                        .nOut(layerSizes.get(i + 1))
                        .activation(activations.get(i))
                        .build());
            }
        }

        return listBuilder.build();
    }

    public void mutate() {
        // Randomly choose mutation type
        int mutationType = random.nextInt(3);
        
        switch (mutationType) {
            case 0: // Add layer
                if (layerSizes.size() < 10) { // Max 10 layers
                    int insertIndex = random.nextInt(layerSizes.size() - 1) + 1;
                    layerSizes.add(insertIndex, random.nextInt(50) + 10);
                    activations.add(insertIndex - 1, getRandomActivation());
                }
                break;
            case 1: // Remove layer
                if (layerSizes.size() > 3) { // Keep at least one hidden layer
                    int removeIndex = random.nextInt(layerSizes.size() - 2) + 1;
                    layerSizes.remove(removeIndex);
                    activations.remove(removeIndex - 1);
                }
                break;
            case 2: // Modify layer size
                int modifyIndex = random.nextInt(layerSizes.size() - 2) + 1;
                layerSizes.set(modifyIndex, Math.max(2, layerSizes.get(modifyIndex) + 
                    (random.nextInt(21) - 10))); // ±10 neurons
                break;
        }
    }

    private Activation getRandomActivation() {
        Activation[] activations = {
            Activation.RELU,
            Activation.LEAKYRELU,
            Activation.TANH,
            Activation.SIGMOID
        };
        return activations[random.nextInt(activations.length)];
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    public List<Integer> getLayerSizes() {
        return new ArrayList<>(layerSizes);
    }

    public List<Activation> getActivations() {
        return new ArrayList<>(activations);
    }
}
