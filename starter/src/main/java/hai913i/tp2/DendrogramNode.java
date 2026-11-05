package hai913i.tp2;

import java.util.*;

public final class DendrogramNode {

    private final int id;
    private final String className;
    private final double similarity;
    private final DendrogramNode left;
    private final DendrogramNode right;
    private final Set<String> memberClasses;

    // une feuille (Classe)
    public DendrogramNode(int id, String className) {
        this.id = id;
        this.className = className;
        this.similarity = 0.0;
        this.left = null;
        this.right = null;
        this.memberClasses = Set.of(className);
    }

    // un noeud interne (Fusion)
    public DendrogramNode(int id, double similarity, DendrogramNode left, DendrogramNode right) {
        this.id = id;
        this.className = null;
        this.similarity = similarity;
        this.left = left;
        this.right = right;

        Set<String> members = new TreeSet<>(left.memberClasses);
        members.addAll(right.memberClasses);
        this.memberClasses = Collections.unmodifiableSet(members);
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public int getId() { return id; }
    public String getClassName() { return className; }
    public double getSimilarity() { return similarity; }
    public DendrogramNode getLeft() { return left; }
    public DendrogramNode getRight() { return right; }
    public Set<String> getMemberClasses() { return memberClasses; }

    /**
     * nombre total de feuilles dans ce sous-arbre
     */
    public int countLeaves() {
        if (isLeaf()) {
            return 1;
        }
        int leftCount = (left != null) ? left.countLeaves() : 0;
        int rightCount = (right != null) ? right.countLeaves() : 0;
        return leftCount + rightCount;
    }

    /**
     * nombre total de nœuds internes dans ce sous-arbre
     */
    public int countInternalNodes() {
        if (isLeaf()) {
            return 0;
        }
        int leftCount = (left != null) ? left.countInternalNodes() : 0;
        int rightCount = (right != null) ? right.countInternalNodes() : 0;
        return 1 + leftCount + rightCount;
    }

    /**
     * nom court d'une classe
     */
    public String getSimpleName() {
        if (isLeaf()) {
            int dot = className.lastIndexOf('.');
            return (dot == -1) ? className : className.substring(dot + 1);
        }
        return "Group_" + id;
    }
}