package demo;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Trois façons de restituer un arbre de regroupement.
 *
 * <p><b>Cet exemple ne calcule rien.</b> L'arbre est construit <em>à la main</em>,
 * à partir du micro jeu de données {@code facts-mini} : trois classes, deux
 * fusions, valeurs 0,6667 puis 0,1667, celles que {@code examples/README.txt}
 * établit.</p>
 *
 * <p><b>Pourquoi il existe.</b> Pour montrer des formats de sortie exploitables,
 * afin que la restitution du résultat ne devienne pas le vrai problème du TP.
 * L'étape B2 demande un dendrogramme lisible <em>sans exécuter votre code</em> :
 * les trois formats ci-dessous répondent chacun à un usage différent.</p>
 *
 * <p><b>Ce qu'il ne fait pas.</b> Aucun couplage, aucune recherche de la paire
 * la plus similaire, aucun recalcul après fusion, aucune coupe. Ces cinq
 * opérations sont le TP.</p>
 *
 * <pre>
 *   mvn -q clean package
 *   java -cp target/classes demo.TinyTreeDemo
 *   java -cp target/classes demo.TinyTreeDemo sortie.dot
 * </pre>
 */
public final class TinyTreeDemo {

    /** Sortie forcée en UTF-8, pour que les accents s'affichent partout. */
    private static final PrintStream OUT =
            new PrintStream(System.out, true, StandardCharsets.UTF_8);

    private TinyTreeDemo() {
    }

    /**
     * Affiche l'arbre du micro exemple dans les trois formats.
     *
     * @param args éventuellement un chemin de fichier où écrire l'export DOT ;
     *             sans argument, le DOT est écrit sur la sortie standard
     * @throws IOException si le fichier DOT ne peut pas être écrit
     */
    public static void main(String[] args) throws IOException {
        // Arbre du micro exemple, écrit à la main (voir examples/README.txt).
        Node ab = Node.merge(Node.leaf("demo.A"), Node.leaf("demo.B"), 0.6667);
        Node root = Node.merge(ab, Node.leaf("demo.C"), 0.1667);

        OUT.println("== 1. arbre indenté ==");
        printTree(root, "");

        OUT.println();
        OUT.println("== 2. fusions, une par ligne ==");
        OUT.println("etape;valeur;groupeA;groupeB");
        int step = 1;
        for (Node n : mergesBottomUp(root)) {
            OUT.printf(Locale.ROOT, "%d;%.4f;%s;%s%n",
                    step++, n.value(), n.left(), n.right());
        }

        OUT.println();
        OUT.println("== 3. export DOT ==");
        String dot = toDot(root);
        if (args.length > 0) {
            Files.writeString(Path.of(args[0]), dot, StandardCharsets.UTF_8);
            OUT.println("écrit dans " + args[0]
                    + "   (dot -Tpng " + args[0] + " -o arbre.png)");
        } else {
            OUT.println(dot);
        }
    }

    /**
     * Arbre indenté : lisible directement dans un terminal.
     *
     * <p>Le format qui coûte le moins cher à produire, et celui qui répond le
     * plus vite à la question « mon arbre a-t-il la bonne forme ? ».</p>
     *
     * @param n      nœud à afficher, avec tout son sous-arbre
     * @param indent préfixe d'indentation courant
     */
    private static void printTree(Node n, String indent) {
        if (n.isLeaf()) {
            OUT.println(indent + "- " + n.members().get(0));
            return;
        }
        OUT.printf(Locale.ROOT, "%s+ %s   (fusion à %.4f)%n",
                indent, n, n.value());
        printTree(n.left(), indent + "   ");
        printTree(n.right(), indent + "   ");
    }

    /**
     * Les fusions dans l'ordre où un regroupement ascendant les produit.
     *
     * <p>Une ligne par fusion, la plus similaire d'abord : c'est le format qui
     * se compare le mieux d'une exécution à l'autre, et sur lequel un test
     * unitaire s'écrit en deux lignes.</p>
     *
     * @param root racine de l'arbre
     * @return les nœuds internes, similarité décroissante
     */
    private static List<Node> mergesBottomUp(Node root) {
        List<Node> out = new ArrayList<>();
        collectMerges(root, out);
        out.sort((a, b) -> Double.compare(b.value(), a.value()));
        return out;
    }

    private static void collectMerges(Node n, List<Node> out) {
        if (n.isLeaf()) {
            return;
        }
        collectMerges(n.left(), out);
        collectMerges(n.right(), out);
        out.add(n);
    }

    /**
     * Export DOT : chaque fusion devient un nœud étiqueté par sa valeur.
     *
     * <p>Converti en image par {@code dot -Tpng arbre.dot -o arbre.png}.</p>
     *
     * @param root racine de l'arbre
     * @return le contenu d'un fichier {@code .dot}, en UTF-8
     */
    private static String toDot(Node root) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph dendrogramme {\n");
        sb.append("  rankdir=BT;\n");
        sb.append("  node [shape=box, fontname=\"Helvetica\", fontsize=10];\n");
        appendDot(root, sb, new int[] {0});
        sb.append("}\n");
        return sb.toString();
    }

    private static String appendDot(Node n, StringBuilder sb, int[] counter) {
        String name = "n" + (counter[0]++);
        if (n.isLeaf()) {
            sb.append(String.format(Locale.ROOT,
                    "  %s [label=\"%s\", style=filled, fillcolor=\"#F3F6F9\"];%n",
                    name, n.members().get(0)));
            return name;
        }
        sb.append(String.format(Locale.ROOT,
                "  %s [label=\"%.4f\", shape=ellipse];%n", name, n.value()));
        String l = appendDot(n.left(), sb, counter);
        String r = appendDot(n.right(), sb, counter);
        sb.append(String.format("  %s -> %s [dir=none];%n", l, name));
        sb.append(String.format("  %s -> %s [dir=none];%n", r, name));
        return name;
    }
}
