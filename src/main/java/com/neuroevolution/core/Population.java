package com.neuroevolution.core;

import java.util.*;

public class Population {
    private List<Genome> genomes;
    private int populationSize;
    private Random random;
    private int inputSize;
    private int outputSize;

    public Population(int populationSize, int inputSize, int outputSize) {
        this.populationSize = populationSize;
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.random = new Random();
        this.genomes = new ArrayList<>();
        
        initializePopulation();
    }

    private void initializePopulation() {
        for (int i = 0; i < populationSize; i++) {
            genomes.add(new Genome(inputSize, outputSize));
        }
    }

    public void evolve() {
        // Sort genomes by fitness (descending)
        genomes.sort((g1, g2) -> Double.compare(g2.getFitness(), g1.getFitness()));

        // Keep top 20% of performers
        int eliteCount = (int) (populationSize * 0.2);
        List<Genome> newPopulation = new ArrayList<>(genomes.subList(0, eliteCount));

        // Create offspring from top performers
        while (newPopulation.size() < populationSize) {
            // Select parent from top 50%
            Genome parent = genomes.get(random.nextInt(populationSize / 2));
            
            // Create offspring (deep copy)
            Genome offspring = new Genome(inputSize, outputSize);
            
            // Mutate offspring
            offspring.mutate();
            
            newPopulation.add(offspring);
        }

        genomes = newPopulation;
    }

    public List<Genome> getGenomes() {
        return genomes;
    }

    public Genome getBestGenome() {
        return genomes.stream()
                .max(Comparator.comparingDouble(Genome::getFitness))
                .orElse(null);
    }
}
