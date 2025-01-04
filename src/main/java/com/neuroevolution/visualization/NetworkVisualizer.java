package com.neuroevolution.visualization;

import com.neuroevolution.core.Genome;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NetworkVisualizer extends JFrame {
    private XYSeries fitnessHistory;
    private XYSeriesCollection dataset;
    private int generation;
    private JPanel networkPanel;
    private Genome currentGenome;

    public NetworkVisualizer() {
        super("Neural Network Evolution Visualization");
        
        // Initialize chart data
        fitnessHistory = new XYSeries("Best Fitness");
        dataset = new XYSeriesCollection(fitnessHistory);
        generation = 0;

        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Evolution Progress",
            "Generation",
            "Fitness",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        // Add chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 300));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(chartPanel, gbc);

        // Add network visualization panel
        networkPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentGenome != null) {
                    drawNetwork((Graphics2D) g, currentGenome);
                }
            }
        };
        networkPanel.setPreferredSize(new Dimension(600, 300));
        networkPanel.setBorder(BorderFactory.createTitledBorder("Network Structure"));
        gbc.gridy = 1;
        mainPanel.add(networkPanel, gbc);

        // Add main panel to frame
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void updateVisualization(Genome bestGenome) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Updating visualization with fitness: " + bestGenome.getFitness());
            fitnessHistory.add(generation++, bestGenome.getFitness());
            currentGenome = bestGenome;
            networkPanel.repaint();
            this.setTitle("Neural Network Evolution - Generation " + generation);
        });
    }

    private void drawNetwork(Graphics2D g, Genome genome) {
        List<Integer> layers = genome.getLayerSizes();
        int maxLayerSize = layers.stream().mapToInt(Integer::intValue).max().getAsInt();
        
        int width = networkPanel.getWidth();
        int height = networkPanel.getHeight();
        int layerSpacing = width / (layers.size() + 1);
        int nodeRadius = 5;
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw nodes and connections
        for (int layerIdx = 0; layerIdx < layers.size(); layerIdx++) {
            int layerSize = layers.get(layerIdx);
            int x = (layerIdx + 1) * layerSpacing;
            
            for (int nodeIdx = 0; nodeIdx < layerSize; nodeIdx++) {
                int y = (height * (nodeIdx + 1)) / (layerSize + 1);
                
                // Draw node
                g.setColor(Color.BLUE);
                g.fillOval(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);
                
                // Draw connections to previous layer
                if (layerIdx > 0) {
                    int prevLayerSize = layers.get(layerIdx - 1);
                    int prevX = layerIdx * layerSpacing;
                    
                    g.setColor(new Color(0, 0, 0, 50)); // Transparent black
                    for (int prevNodeIdx = 0; prevNodeIdx < prevLayerSize; prevNodeIdx++) {
                        int prevY = (height * (prevNodeIdx + 1)) / (prevLayerSize + 1);
                        g.drawLine(prevX, prevY, x, y);
                    }
                }
            }
        }
    }
}
