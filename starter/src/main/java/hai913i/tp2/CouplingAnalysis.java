package hai913i.tp2;

import java.io.PrintStream;
import java.util.*;

public class CouplingAnalysis {

    /**
     * Couple de classes non orienté
     * @param classA
     * @param classB
     */
    public record ClassPair(String classA, String classB) {
        public static ClassPair of(String c1, String c2) {
            if (c1.compareTo(c2) <= 0) {
                return new ClassPair(c1, c2);
            } else {
                return new ClassPair(c2, c1);
            }
        }
    }

    /**
     * Résultat de l'analyse des couplage
     * @param eInterTotal
     * @param rValues
     * @param couplingValues
     */
    public record CouplingResult(
            int eInterTotal,
            Map<ClassPair, Integer> rValues,
            Map<ClassPair, Double> couplingValues
    ) {}

    public static CouplingResult analyze(Facts facts, PrintStream OUT) {
        Map<String, String> methodOwners = facts.methodOwners();
        Map<ClassPair, Integer> rValues = new LinkedHashMap<>();
        int eInter = 0;
        int removedArcs = 0;

        // enlever les arcs internes et compter E_inter
        for (Facts.CallArc arc : facts.calls()) {
            String callerClass = methodOwners.get(arc.caller());
            String calleeClass = methodOwners.get(arc.callee());

            if (callerClass == null || calleeClass == null) {
                continue;
            }

            if (callerClass.equals(calleeClass)) {
                removedArcs++;
            } else {
                eInter++;
                // agrégation par couple de classes distinctes (en ignorant le sens)
                ClassPair pair = ClassPair.of(callerClass, calleeClass);
                rValues.put(pair, rValues.getOrDefault(pair, 0) + 1);
            }
        }

        OUT.println("\n--- RÉSULTATS ÉTAPE A1 ---");
        OUT.println("|E_inter|                  : " + eInter);
        OUT.println("Arcs écartés               : " + removedArcs);

        // calcul de C(A, B) = r(A, B) / |E_inter|
        Map<ClassPair, Double> couplingValues = new LinkedHashMap<>();
        for (Map.Entry<ClassPair, Integer> entry : rValues.entrySet()) {
            double coupling = (double) entry.getValue() / eInter;
            couplingValues.put(entry.getKey(), coupling);
        }

        // vérification de la somme des couplages (doit valoir 1.0)
        double sumCoupling = couplingValues.values().stream().mapToDouble(Double::doubleValue).sum();
        OUT.printf(Locale.ROOT, "Somme des couplages        : %.4f%n", sumCoupling);
        OUT.println("Couples non nuls           : " + couplingValues.size());

        int m = facts.classes().size();
        int totalCouplesPossibles = (m * (m - 1)) / 2;
        OUT.println("Total couples possibles    : " + totalCouplesPossibles);

        // affichage des couples par couplage décroissant
        record PairWithMetric(ClassPair pair, int r, double coupling) {}
        List<PairWithMetric> sortedList = new ArrayList<>();
        for (Map.Entry<ClassPair, Double> entry : couplingValues.entrySet()) {
            ClassPair p = entry.getKey();
            sortedList.add(new PairWithMetric(p, rValues.get(p), entry.getValue()));
        }

        sortedList.sort((p1, p2) -> Double.compare(p2.coupling(), p1.coupling()));

        OUT.printf("\n--- COUPLES PAR COUPLAGE DÉCROISSANT ---%n%n");
        for (PairWithMetric item : sortedList) {
            OUT.printf(Locale.ROOT, "  - %s <---> %s : r = %d, C = %.4f%n",
                    item.pair().classA(), item.pair().classB(), item.r(), item.coupling());
        }

        return new CouplingResult(eInter, rValues, couplingValues);
    }
}