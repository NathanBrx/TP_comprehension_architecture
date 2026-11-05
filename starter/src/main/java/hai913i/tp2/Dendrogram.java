package hai913i.tp2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class Dendrogram {

    /**
     * reconstruit l'arbre binaire du dendrogramme en mémoire à partir des fusions de B1
     */
    public static DendrogramNode buildTree(
            Facts facts,
            List<HierarchicalClustering.StepStepFusion> fusions
    ) {
        List<String> sortedClassNames = facts.classNames();
        Map<Integer, DendrogramNode> nodeMap = new LinkedHashMap<>();

        // instanciation des M feuilles (0 à M-1)
        for (int i = 0; i < sortedClassNames.size(); i++) {
            nodeMap.put(i, new DendrogramNode(i, sortedClassNames.get(i)));
        }

        // assemblage des nœuds internes par fusion successive
        for (HierarchicalClustering.StepStepFusion f : fusions) {
            DendrogramNode left = nodeMap.get(f.groupA().getId());
            DendrogramNode right = nodeMap.get(f.groupB().getId());

            DendrogramNode internalNode = new DendrogramNode(
                    f.mergedGroup().getId(),
                    f.similarity(),
                    left,
                    right
            );

            nodeMap.put(f.mergedGroup().getId(), internalNode);
        }

        // racine : dernier nœud créé (ID = 2M - 2)
        int rootId = (2 * sortedClassNames.size()) - 2;
        return nodeMap.get(rootId);
    }

    // affichage et export du fichier indenté
    public static void printIndentedTree(DendrogramNode node, String indent, boolean isLast, PrintStream out) {
        out.print(indent);
        out.print(isLast ? "└── " : "├── ");

        if (node.isLeaf()) {
            out.println("[Feuille " + node.getId() + "] " + node.getSimpleName() + " (" + node.getClassName() + ")");
        } else {
            out.printf(Locale.ROOT, "[Nœud %d] sim = %.4f (Membres : %d classes)%n",
                    node.getId(), node.getSimilarity(), node.getMemberClasses().size());

            String childIndent = indent + (isLast ? "    " : "│   ");
            printIndentedTree(node.getLeft(), childIndent, false, out);
            printIndentedTree(node.getRight(), childIndent, true, out);
        }
    }

    public static void exportText(DendrogramNode root, Path path) throws IOException {
        try (PrintStream ps = new PrintStream(Files.newOutputStream(path), true, StandardCharsets.UTF_8)) {
            ps.println("--- DENDROGRAMME DU PROJET ---");
            printIndentedTree(root, "", true, ps);
        }
    }

    // export fichier dot
    public static void exportDot(DendrogramNode root, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("digraph Dendrogram {\n");
            writer.write("    node [shape=box, style=\"rounded,filled\", fontname=\"Helvetica\"];\n");
            writer.write("    edge [fontname=\"Helvetica\", fontsize=9];\n\n");

            writeDotNodes(root, writer);

            writer.write("}\n");
        }
    }

    private static void writeDotNodes(DendrogramNode node, BufferedWriter writer) throws IOException {
        if (node.isLeaf()) {
            writer.write(String.format("    node_%d [label=\"%s\", fillcolor=\"#E1F5FE\", shape=ellipse];%n",
                    node.getId(), node.getSimpleName()));
        } else {
            writer.write(String.format(Locale.ROOT,
                    "    node_%d [label=\"Nœud %d\\nsim = %.4f\", fillcolor=\"#FFF9C4\"];%n",
                    node.getId(), node.getId(), node.getSimilarity()));

            // fils gauche
            writer.write(String.format("    node_%d -> node_%d;%n", node.getId(), node.getLeft().getId()));
            writeDotNodes(node.getLeft(), writer);

            // fils droit
            writer.write(String.format("    node_%d -> node_%d;%n", node.getId(), node.getRight().getId()));
            writeDotNodes(node.getRight(), writer);
        }
    }

    public static void etapeB2(DendrogramNode root, PrintStream out) {
        int nbLeaves = root.countLeaves();
        int nbInternal = root.countInternalNodes();
        Set<String> rootMembers = root.getMemberClasses();

        out.println("\n--- RÉSULTATS ÉTAPE B2 ---");
        out.println("Nombre de feuilles (classes)       : " + nbLeaves);
        out.println("Nombre de nœuds internes           : " + nbInternal);
        out.println("Nombre de membres dans la racine   : " + rootMembers.size());
        out.printf(Locale.ROOT, "Similarité de la racine (étape 14) : %.4f%n", root.getSimilarity());
        out.printf(Locale.ROOT, "Similarité de l'avant-dernière     : %.4f%n", root.getRight().getSimilarity());
    }
}