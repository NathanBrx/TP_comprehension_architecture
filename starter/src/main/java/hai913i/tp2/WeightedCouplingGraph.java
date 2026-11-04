package hai913i.tp2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class WeightedCouplingGraph {

    /**
     * Représente une arête non orientée du graphe de couplage.
     */
    public record Edge(String source, String target, int r, double weight) {
        public Edge {
            if (source.compareTo(target) > 0) {
                String temp = source;
                source = target;
                target = temp;
            }
        }
    }

    private final List<String> nodes;
    private final List<Edge> edges;
    private static PrintStream OUT;

    /**
     * Constructeur externe utilisé dans le main
     * @param allClassNames noeuds du graphe
     * @param couplingResult résultat de l'étape a1
     */
    public WeightedCouplingGraph(List<String> allClassNames, CouplingAnalysis.CouplingResult couplingResult, PrintStream out) {
        this.nodes = new ArrayList<>(allClassNames);
        Collections.sort(this.nodes);

        this.edges = new ArrayList<>();
        for (Map.Entry<CouplingAnalysis.ClassPair, Double> entry : couplingResult.couplingValues().entrySet()) {
            double weight = entry.getValue();
            if (weight > 0.0) {
                CouplingAnalysis.ClassPair pair = entry.getKey();
                int r = couplingResult.rValues().getOrDefault(pair, 0);
                this.edges.add(new Edge(pair.classA(), pair.classB(), r, weight));
            }
        }

        this.OUT = out;
    }

    /**
     * Constructeur interne utilisé quand on load depuis le csv
     */
    private WeightedCouplingGraph(List<String> nodes, List<Edge> edges) {
        this.nodes = List.copyOf(nodes);
        this.edges = List.copyOf(edges);
    }

    public void printSummary() {
        OUT.println("\n--- RÉSULTATS ÉTAPE A2 : GRAPHE DE COUPLAGE ---");
        OUT.println("Sommets    : " + nodes.size());
        OUT.println("Arêtes     : " + edges.size());

        // 5 arêtes les plus lourdes
        List<Edge> sortedEdges = new ArrayList<>(edges);
        sortedEdges.sort((e1, e2) -> Double.compare(e2.weight(), e1.weight()));

        OUT.println("\nLes 5 arêtes les plus lourdes :");
        for (int i = 0; i < Math.min(5, sortedEdges.size()); i++) {
            Edge e = sortedEdges.get(i);
            OUT.printf(Locale.ROOT, "  %d. %s <---> %s (r = %d, weight = %.4f)%n",
                    i + 1, e.source(), e.target(), e.r(), e.weight());
        }

        // classes isolées (degré = 0)
        Set<String> connectedNodes = new HashSet<>();
        for (Edge e : edges) {
            connectedNodes.add(e.source());
            connectedNodes.add(e.target());
        }

        List<String> isolated = new ArrayList<>();
        for (String node : nodes) {
            if (!connectedNodes.contains(node)) {
                isolated.add(node);
            }
        }

        OUT.println("\nClasses isolées : " + isolated.size());
        for (String iso : isolated) {
            OUT.println("  - " + iso);
        }
    }

    /**
     * Exporte le graphe en dot
     */
    public void exportDot(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("graph CouplingGraph {\n");
            writer.write("    node [shape=ellipse, style=filled, fillcolor=lightblue];\n");
            writer.write("    edge [fontsize=10];\n\n");

            // noeuds
            for (String node : nodes) {
                String simpleName = extractSimpleName(node);
                writer.write(String.format("    \"%s\" [label=\"%s\"];%n", node, simpleName));
            }

            writer.write("\n");

            // arêtes
            for (Edge e : edges) {
                writer.write(String.format(Locale.ROOT,
                        "    \"%s\" -- \"%s\" [label=\"%.4f\", weight=%.4f];%n",
                        e.source(), e.target(), e.weight(), e.weight()));
            }

            writer.write("}\n");
        }

        OUT.println("\nFichier dot exporté dans : " + path);
    }

    /**
     * Exporte le graphe en csv
     */
    public void exportCsv(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("source;target;r;weight\n");
            for (Edge e : edges) {
                writer.write(String.format(Locale.ROOT, "%s;%s;%d;%.6f%n",
                        e.source(), e.target(), e.r(), e.weight()));
            }
        }

        OUT.println("\nFichier csv exporté dans : " + path);
    }

    /**
     * Recharge le graphe depuis un csv
     */
    public static WeightedCouplingGraph loadCsv(Path csvPath, List<String> allClassNames) throws IOException {
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        List<Edge> reloadedEdges = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(";");
            if (parts.length < 4) continue;

            String source = parts[0];
            String target = parts[1];
            int r = Integer.parseInt(parts[2]);
            double weight = Double.parseDouble(parts[3]);

            reloadedEdges.add(new Edge(source, target, r, weight));
        }

        return new WeightedCouplingGraph(allClassNames, reloadedEdges);
    }

    /**
     * Helper pour récupérer le nom de la classe seulement.<br>Ex : "Loan" au lieu de "library.service.LoanService.Loan"
     * @param qName Nom complet de la classe (nom du noeud dans le graphe)
     * @return Le nom court
     */
    private static String extractSimpleName(String qName) {
        int dot = qName.lastIndexOf('.');
        return (dot == -1) ? qName : qName.substring(dot + 1);
    }

    public List<String> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}