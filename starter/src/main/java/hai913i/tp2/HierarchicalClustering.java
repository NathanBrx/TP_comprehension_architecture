package hai913i.tp2;

import java.io.PrintStream;
import java.util.*;

public final class HierarchicalClustering {

    private static final double EPSILON = 1e-9;

    /**
     * groupe (cluster) de classes.
     */
    public record Cluster(int id, Set<String> classNames) {
        public Cluster(int id, Set<String> classNames) {
            this.id = id;
            this.classNames = Collections.unmodifiableSet(new TreeSet<>(classNames));
        }

        public int getId() {
            return id;
        }

        public String getSimpleNames() {
            List<String> simple = new ArrayList<>();
            for (String fqcn : classNames) {
                int dot = fqcn.lastIndexOf('.');
                simple.add(dot == -1 ? fqcn : fqcn.substring(dot + 1));
            }
            return "{" + String.join(", ", simple) + "}";
        }
    }

    /**
     * fusion de deux groupes.
     */
    public record StepStepFusion(
            int step,
            Cluster groupA,
            Cluster groupB,
            double similarity,
            Cluster mergedGroup
    ) {}

    /**
     * clustering hiérarchique ascendant (Étape B1).
     */
    public static List<StepStepFusion> runClustering(
            Facts facts,
            CouplingAnalysis.CouplingResult couplingResult,
            PrintStream out
    ) {
        // numérotation initiale de 0 à M-1 par ordre alphabétique de nom qualifié
        List<String> sortedClassNames = facts.classNames();
        int m = sortedClassNames.size();

        Map<Integer, Cluster> activeClusters = new LinkedHashMap<>(); // clusters qui n'ont pas encore été fusionnés dans un cluster supérieur
        for (int i = 0; i < m; i++) { // initialisation des clusters : m clusters de 1 classe
            activeClusters.put(i, new Cluster(i, Set.of(sortedClassNames.get(i))));
        }

        List<StepStepFusion> fusions = new ArrayList<>();
        int nextId = m;

        // boucle de fusion : M - 1 itérations pour finir avec 1 seul groupe
        for (int step = 1; step <= m - 1; step++) {
            List<Integer> activeIds = new ArrayList<>(activeClusters.keySet());
            Collections.sort(activeIds);

            double maxSim = -1.0;
            int bestId1 = -1;
            int bestId2 = -1;

            // recherche du couple avec la plus grande similarité
            for (int i = 0; i < activeIds.size(); i++) {
                for (int j = i + 1; j < activeIds.size(); j++) {
                    int id1 = activeIds.get(i);
                    int id2 = activeIds.get(j);

                    Cluster g1 = activeClusters.get(id1);
                    Cluster g2 = activeClusters.get(id2);

                    double sim = averageLinkage(g1, g2, couplingResult);

                    // départage avec tolérance EPSILON (1e-9, tolérance d'arrondi des double)
                    if (sim > maxSim + EPSILON) {
                        maxSim = sim;
                        bestId1 = id1;
                        bestId2 = id2;
                    } else if (Math.abs(sim - maxSim) <= EPSILON) {
                        // si égalité  : on retient le couple lexicographiquement le plus petit (min(i,j), max(i,j))
                        if (bestId1 == -1 || id1 < bestId1 || (id1 == bestId1 && id2 < bestId2)) {
                            maxSim = sim;
                            bestId1 = id1;
                            bestId2 = id2;
                        }
                    }
                }
            }

            // fusion :
            // on enlève les clusters de la liste des actifs
            Cluster groupA = activeClusters.remove(bestId1);
            Cluster groupB = activeClusters.remove(bestId2);

            // on unit les classes des clusters
            Set<String> mergedMembers = new TreeSet<>(groupA.classNames());
            mergedMembers.addAll(groupB.classNames());

            // création du nouveau cluster et ajout à la fin de la liste des clusters actifs
            Cluster mergedGroup = new Cluster(nextId, mergedMembers);
            activeClusters.put(nextId, mergedGroup);

            // enregistrement de la fusion
            fusions.add(new StepStepFusion(step, groupA, groupB, maxSim, mergedGroup));
            nextId++;
        }

        // affichage bilan
        printClusteringSummary(fusions, out);

        return fusions;
    }

    /**
     * Calcul de la similarité du lien moyen
     */
    private static double averageLinkage(
            Cluster g1,
            Cluster g2,
            CouplingAnalysis.CouplingResult couplingResult
    ) {
        double sumCoupling = 0.0;
        Map<CouplingAnalysis.ClassPair, Double> couplings = couplingResult.couplingValues();

        for (String a : g1.classNames()) { // double somme du 5.3
            for (String b : g2.classNames()) {
                CouplingAnalysis.ClassPair pair = CouplingAnalysis.ClassPair.of(a, b);
                sumCoupling += couplings.getOrDefault(pair, 0.0);
            }
        }

        int denominator = g1.classNames().size() * g2.classNames().size(); // |g1| x |g2|
        return sumCoupling / denominator;
    }

    private static void printClusteringSummary(List<StepStepFusion> fusions, PrintStream out) {
        out.println("\n--- RÉSULTATS ÉTAPE B1 : CLUSTERING ASCENDANT ---");
        out.println("Nombre de fusions effectuées : " + fusions.size());
        out.println();
        out.printf(Locale.ROOT, "%-5s | %-6s | %-6s | %-10s | %-35s | %-45s%n",
                "Étape", "ID A", "ID B", "Similarité", "Groupe A", "Groupe B");
        out.println("-".repeat(120));

        for (StepStepFusion f : fusions) {
            out.printf(Locale.ROOT, "%-5d | %-6d | %-6d | %-10.4f | %-35s | %-45s%n",
                    f.step(),
                    f.groupA().id(),
                    f.groupB().id(),
                    f.similarity(),
                    f.groupA().getSimpleNames(),
                    f.groupB().getSimpleNames());
        }
    }
}