package demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nœud d'un arbre de regroupement : soit une feuille qui porte le nom d'une
 * classe, soit un nœud interne qui porte deux fils et la valeur de la fusion.
 *
 * <p><b>Ce que cette structure représente.</b> Un dendrogramme complet, tel que
 * l'étape B2 du TP en demande un. Une feuille est une classe ; un nœud interne
 * est le groupe formé par une fusion, et il retient la similarité <em>au moment
 * de cette fusion</em>.</p>
 *
 * <p><b>Pourquoi elle existe.</b> Pour vous épargner la question « quelle forme
 * donner à mon arbre ? », qui n'est pas l'objet du TP. Ce n'est
 * <em>pas</em> une API imposée : toute structure équivalente convient. Ce que le
 * TP exige, c'est de pouvoir restituer, pour chaque fusion, les deux groupes
 * réunis et la similarité de la fusion.</p>
 *
 * <p><b>Ce qu'elle ne fait pas.</b> Elle ne calcule aucun couplage, ne cherche
 * pas la paire la plus similaire, ne recalcule rien après une fusion et ne
 * coupe pas l'arbre. Elle se contente de stocker un arbre déjà décidé.</p>
 *
 * <p><b>Invariants utiles à tester</b> : une feuille n'a pas de fils ; un nœud
 * interne en a exactement deux ; les membres d'un nœud interne sont l'union
 * <b>disjointe</b> des membres de ses deux fils ; un arbre construit sur M
 * classes a M feuilles et M − 1 nœuds internes.</p>
 */
public final class Node {

    /** Nom de la classe portée ; non nul si et seulement si c'est une feuille. */
    private final String leaf;

    /** Fils gauche ; nul pour une feuille. */
    private final Node left;

    /** Fils droit ; nul pour une feuille. */
    private final Node right;

    /** Similarité au moment de la fusion ; 0 pour une feuille, qui n'en a pas. */
    private final double value;

    private Node(String leaf, Node left, Node right, double value) {
        this.leaf = leaf;
        this.left = left;
        this.right = right;
        this.value = value;
    }

    /**
     * Crée une feuille, c'est-à-dire un groupe réduit à une classe.
     *
     * @param className nom qualifié de la classe
     * @return une feuille, jamais nulle
     */
    public static Node leaf(String className) {
        return new Node(className, null, null, 0.0);
    }

    /**
     * Crée un nœud interne : la fusion de deux groupes.
     *
     * @param left  premier groupe réuni
     * @param right second groupe réuni
     * @param value similarité au moment de la fusion, telle que la donne le
     *              critère de liaison ; elle n'est pas recalculée ici
     * @return un nœud interne, jamais nul
     */
    public static Node merge(Node left, Node right, double value) {
        return new Node(null, left, right, value);
    }

    /** @return vrai si ce nœud est une feuille, donc un groupe d'une classe */
    public boolean isLeaf() {
        return leaf != null;
    }

    /** @return le fils gauche, ou {@code null} pour une feuille */
    public Node left() {
        return left;
    }

    /** @return le fils droit, ou {@code null} pour une feuille */
    public Node right() {
        return right;
    }

    /**
     * La similarité de la fusion qui a créé ce nœud.
     *
     * <p>Attention : ce n'est <b>pas</b> le couplage interne moyen du groupe.
     * Cette valeur mesure la proximité <em>entre les deux fils</em> ; la
     * contrainte CP de l'étape B3 porte sur la moyenne du couplage sur
     * <em>tous</em> les couples de classes du nœud, fils confondus. Confondre
     * les deux est l'erreur la plus fréquente de l'étape B3.</p>
     *
     * @return la similarité de la fusion, ou 0 pour une feuille
     */
    public double value() {
        return value;
    }

    /**
     * Les classes contenues, triées : c'est ce qu'on appelle un groupe.
     *
     * @return nouvelle liste triée ; la modifier n'affecte pas l'arbre
     */
    public List<String> members() {
        List<String> out = new ArrayList<>();
        collect(out);
        Collections.sort(out);
        return out;
    }

    private void collect(List<String> out) {
        if (isLeaf()) {
            out.add(leaf);
        } else {
            left.collect(out);
            right.collect(out);
        }
    }

    /** @return le nombre de feuilles du sous-arbre, donc la taille du groupe */
    public int size() {
        return isLeaf() ? 1 : left.size() + right.size();
    }

    /**
     * Identifiant lisible d'un groupe : le plus petit nom de classe qu'il
     * contient, dans l'ordre alphabétique.
     *
     * <p>Utile pour déboguer et pour comparer deux exécutions, puisque deux
     * exécutions donnent le même identifiant au même groupe. <b>Ne l'utilisez
     * pas pour départager les égalités</b> : la règle du sujet porte sur les
     * identifiants entiers d'ordre de création, pas sur les noms.</p>
     *
     * @return le plus petit nom de classe du groupe
     */
    public String id() {
        return members().get(0);
    }

    @Override
    public String toString() {
        return "{" + String.join(", ", members()) + "}";
    }
}
