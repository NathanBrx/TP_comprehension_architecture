package demo;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * La règle de départage des égalités, sur un exemple minuscule.
 *
 * <p><b>Le problème.</b> Quand plusieurs couples de groupes atteignent
 * exactement la même similarité maximale, il faut une règle pour en désigner un
 * seul ; sinon deux exécutions du même programme produisent deux arbres
 * différents. Sur le projet de validation, la
 * moitié des quatorze étapes présentent au moins deux couples à égalité, et
 * jusqu'à trois.</p>
 *
 * <p><b>La règle imposée par le sujet</b> :</p>
 * <ol>
 *   <li>chaque groupe actif porte un <b>identifiant entier stable</b> : les
 *       classes initiales sont numérotées de 0 à M−1 par ordre alphabétique de
 *       nom qualifié, puis chaque groupe créé par une fusion reçoit
 *       l'identifiant suivant ;</li>
 *   <li>un couple s'écrit toujours dans le même sens, du plus petit identifiant
 *       vers le plus grand : <code>(min(i,j), max(i,j))</code> ;</li>
 *   <li>parmi les couples de similarité maximale, on les compare
 *       <b>lexicographiquement</b> : d'abord le premier identifiant, puis, à
 *       premier identifiant égal, le second ;</li>
 *   <li>on retient le <b>plus petit</b> couple.</li>
 * </ol>
 *
 * <p><b>Pourquoi cette règle-là.</b> Ce n'est pas un meilleur choix de fusion, 
 * c'est une <b>convention déterministe</b>.
 * Elle garantit que votre résultat, celui de
 * votre binôme et celui du correcteur coïncident, et qu'ils coïncident avec
 * l'exemple filé du CM 03.</p>
 *
 * <p><b>Ce que cet exemple ne fait pas.</b> Il n'implémente pas le
 * regroupement : il applique la règle à une matrice de similarités écrite à la
 * main. Chercher la paire la plus similaire à chaque itération, recalculer les
 * similarités après une fusion et construire l'arbre restent à votre charge.</p>
 *
 * <pre>
 *   java -cp target/classes demo.TieBreakDemo
 * </pre>
 */
public final class TieBreakDemo {

    /** Sortie forcée en UTF-8, pour que les accents s'affichent partout. */
    private static final PrintStream OUT =
            new PrintStream(System.out, true, StandardCharsets.UTF_8);

    /** Tolérance de comparaison des similarités ; jamais {@code ==}. */
    private static final double EPS = 1e-12;

    /** Quatre groupes fictifs, déjà numérotés 0, 1, 2, 3. */
    private static final String[] NAMES = {"g0", "g1", "g2", "g3"};

    /**
     * Similarités, symétriques et de diagonale nulle.
     *
     * <p>La valeur 0,25 apparaît <b>trois</b> fois : sur les couples (0,2),
     * (1,3) et (2,3). C'est exactement la situation que la règle doit trancher,
     * et elle retient (0,2) parce que 0 &lt; 1 &lt; 2.</p>
     */
    private static final double[][] SIM = {
        //        g0     g1     g2     g3
        /* g0 */ {0.00, 0.10, 0.25, 0.05},
        /* g1 */ {0.10, 0.00, 0.05, 0.25},
        /* g2 */ {0.25, 0.05, 0.00, 0.25},
        /* g3 */ {0.05, 0.25, 0.25, 0.00},
    };

    private TieBreakDemo() {
    }

    /**
     * Un couple candidat à la fusion, toujours écrit avec {@code i < j}.
     *
     * @param i     plus petit identifiant du couple
     * @param j     plus grand identifiant du couple
     * @param value similarité des deux groupes
     */
    private record Candidate(int i, int j, double value) {
    }

    /**
     * Affiche tous les couples candidats, puis celui que la règle retient.
     *
     * @param args ignorés
     */
    public static void main(String[] args) {
        // Tous les couples, ecrits canoniquement : i < j par construction.
        List<Candidate> all = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            for (int j = i + 1; j < NAMES.length; j++) {
                all.add(new Candidate(i, j, SIM[i][j]));
            }
        }

        // Tri d'affichage : similarité décroissante, puis (i, j) croissants.
        all.sort(Comparator.comparingDouble(Candidate::value).reversed()
                .thenComparingInt(Candidate::i)
                .thenComparingInt(Candidate::j));

        OUT.println("couples candidats, du plus similaire au moins similaire :");
        double best = all.get(0).value();
        for (Candidate c : all) {
            boolean tied = Math.abs(c.value() - best) < EPS;
            OUT.printf(Locale.ROOT, "  (%d, %d)  %-4s - %-4s  %.4f%s%n",
                    c.i(), c.j(), NAMES[c.i()], NAMES[c.j()], c.value(),
                    tied ? "   <-- à égalité au maximum" : "");
        }

        // La règle : parmi les couples à égalité, le plus petit dans l'ordre
        // lexicographique — d'abord i, puis j à i égal.
        Candidate winner = all.stream()
                .filter(c -> Math.abs(c.value() - best) < EPS)
                .min(Comparator.comparingInt(Candidate::i)
                        .thenComparingInt(Candidate::j))
                .orElseThrow();

        long tied = all.stream().filter(c -> Math.abs(c.value() - best) < EPS).count();
        OUT.println();
        OUT.printf(Locale.ROOT,
                "%d couples atteignent %.4f ; la règle retient (%d, %d) : %s + %s%n",
                tied, best, winner.i(), winner.j(),
                NAMES[winner.i()], NAMES[winner.j()]);
        OUT.println("comparaison lexicographique : d'abord le premier identifiant,");
        OUT.println("puis le second à premier identifiant égal — (0,2) l'emporte");
        OUT.println("sur (1,3) et (2,3) parce que 0 < 1 < 2, et il l'emporterait");
        OUT.println("sur (0,3) parce qu'à premier identifiant égal, 2 < 3.");
        OUT.println();
        OUT.println("Attention à la comparaison des réels : deux valeurs calculées");
        OUT.println("différemment peuvent différer du dernier bit. Comparez avec une");
        OUT.println("tolérance (ici 1e-12), jamais avec ==, sans quoi une égalité");
        OUT.println("passe inaperçue et le résultat cesse d'être reproductible.");
    }
}
