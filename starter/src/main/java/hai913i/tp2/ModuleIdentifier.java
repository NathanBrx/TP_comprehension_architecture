package hai913i.tp2;

import java.io.PrintStream;
import java.util.*;

public final class ModuleIdentifier {

    /**
     * module candidat extrait après coupe
     */
    public record ModuleCandidate(
            int id,
            DendrogramNode node,
            Set<String> classNames,
            double internalCoupling
    ) {
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
     * coupe de l'arbre et identification les modules sous le seuil CP
     */
    public static void identifyModules(
            DendrogramNode root,
            CouplingAnalysis.CouplingResult couplingResult,
            double cp,
            int totalClassesM,
            PrintStream out
    ) {
        List<ModuleCandidate> modules = new ArrayList<>();

        // parcours récursif pour la coupe
        cutRecursive(root, couplingResult, cp, modules);

        // vérification de la contrainte M/2
        int maxAllowedModules = totalClassesM / 2; // Division entière (15 / 2 = 7)
        boolean constraintRespected = modules.size() <= maxAllowedModules;

        // affichage du bilan de l'étape
        out.println("\n--- RÉSULTATS ÉTAPE B3 : IDENTIFICATION DES MODULES ---");
        out.printf(Locale.ROOT, "Seuil CP retenu    : %.4f%n", cp);
        out.println("Nombre de modules  : " + modules.size());
        out.println("M / 2              : " + maxAllowedModules);
        out.println("Contrainte M/2     : " +
                (constraintRespected ? "RESPECTÉE" : "VIOLÉE, cette coupe est rejetée"));

        out.println("\n--- DÉTAIL DES MODULES CANDIDATS ---");
        out.printf(Locale.ROOT, "%-8s | %-8s | %-12s | %-18s | %-65s%n",
                "Module", "Nœud ID", "Nb Classes", "Couplage Interne", "Classes membres");
        out.println("-".repeat(120));

        for (int i = 0; i < modules.size(); i++) {
            ModuleCandidate m = modules.get(i);
            out.printf(Locale.ROOT, "Mod %-4d | Nœud %-3d | %-12d | %-18.4f | %-65s%n",
                    (i + 1), m.id(), m.classNames().size(), m.internalCoupling(), m.getSimpleNames());
        }
    }

    /**
     * parcours récursif depuis la racine pour la coupe
     */
    private static void cutRecursive(
            DendrogramNode node,
            CouplingAnalysis.CouplingResult couplingResult,
            double cp,
            List<ModuleCandidate> result
    ) {
        // une feuille est toujours retenue intacte
        if (node.isLeaf()) {
            result.add(new ModuleCandidate(node.getId(), node, node.getMemberClasses(), 0.0));
            return;
        }

        // calcul du couplage interne moyen C_barre(G)
        double internalCoupling = computeInternalCoupling(node.getMemberClasses(), couplingResult);

        // si C_barre(G) > CP (Strictement supérieur), le nœud devient un module
        if (internalCoupling > cp) {
            result.add(new ModuleCandidate(node.getId(), node, node.getMemberClasses(), internalCoupling));
        } else {
            // sinon, on descend dans les deux fils
            if (node.getLeft() != null) {
                cutRecursive(node.getLeft(), couplingResult, cp, result);
            }
            if (node.getRight() != null) {
                cutRecursive(node.getRight(), couplingResult, cp, result);
            }
        }
    }

    /**
     * couplage interne moyen C_barre(G) pour un ensemble de classes
     */
    public static double computeInternalCoupling(
            Set<String> memberClasses,
            CouplingAnalysis.CouplingResult couplingResult
    ) {
        int n = memberClasses.size();
        if (n < 2) {
            return 0.0;
        }

        List<String> list = new ArrayList<>(memberClasses);
        double totalCoupling = 0.0;
        Map<CouplingAnalysis.ClassPair, Double> couplings = couplingResult.couplingValues();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                CouplingAnalysis.ClassPair pair = CouplingAnalysis.ClassPair.of(list.get(i), list.get(j));
                totalCoupling += couplings.getOrDefault(pair, 0.0);
            }
        }

        double numPairs = (n * (n - 1)) / 2.0; // n parmi 2
        return totalCoupling / numPairs;
    }
}