HAI913I - TP 2 - Compréhension et architectures (2026-2027)
Exemples de code source et micro jeu de données
===========================================================

Ces exemples sont des SUPPORTS.
Ils documentent un format, une structure de données et une convention.

  facts-mini.json                micro jeu de données, format canonique
  facts-mini-classes.csv         le même, en CSV : les classes
  facts-mini-methods.csv         le même, en CSV : les méthodes
  facts-mini-calls.csv           le même, en CSV : les arcs
  pom.xml, src/                  deux petites démonstrations Java


1. LE MICRO JEU DE DONNÉES : facts-mini
---------------------------------------
Trois classes (demo.A, demo.B, demo.C), cinq méthodes, quatre arcs d'appel.
Il sert à deux choses : comprendre le format sans ouvrir un fichier de 44 arcs, et ÉCRIRE VOS PREMIERS TESTS sur des valeurs vérifiables à la main.

Les quatre arcs :

    demo.A#f() -> demo.B#h()      2 sites      inter-classes
    demo.A#g() -> demo.B#h()      1 site       inter-classes
    demo.A#f() -> demo.A#g()      1 site       intra-classe : écarté
    demo.B#i() -> demo.C#j()      1 site       inter-classes

Donc |E_inter| = 3 arcs inter-classes, et :

    r(A, B) = 2      C(A, B) = 2/3 = 0,6667
    r(B, C) = 1      C(B, C) = 1/3 = 0,3333
    r(A, C) = 0      C(A, C) = 0
                     somme des couplages = 1        (contrôle utile)

Regroupement par lien moyen, avec M = 3 classes donc M - 1 = 2 fusions :

    étape 1 : {A} + {B}        valeur 0,6667
    étape 2 : {A, B} + {C}     valeur (0 + 0,3333) / (2 x 1) = 0,1667

Règle de coupe : un groupe G d'au moins deux classes est retenu comme module
lorsque son COUPLAGE INTERNE MOYEN dépasse strictement le seuil :

    Cbar(G) = moyenne de C(A,B) sur TOUS les couples de classes de G
    G est un module  <=>  Cbar(G) > CP

CP calibre donc la granularité du découpage : bas, les groupes sont acceptés
haut dans l'arbre et les modules sont gros et peu nombreux ; élevé, le parcours
descend plus bas et les modules deviennent petits et nombreux.
Une feuille est toujours retenue : elle n'a aucun couple, la contrainte ne s'y
applique pas.

Ici Cbar({A, B, C}) = (0,6667 + 0 + 0,3333) / 3 = 0,3333.
Deux seuils, avec au plus M/2 = 1 module (division entière) :

    CP = 0,20   la racine {A, B, C} a une moyenne interne de
                (0,6667 + 0 + 0,3333) / 3 = 0,3333 > 0,20
                -> 1 module. Contrainte M/2 RESPECTÉE.

    CP = 0,50   la racine a une moyenne de 0,3333, qui n'est pas > 0,50 :
                on descend. {A, B} a une moyenne de 0,6667 > 0,50 -> module.
                {C} est une feuille -> module d'une seule classe.
                -> 2 modules > M/2 = 1. Contrainte M/2 VIOLÉE.

Le second cas ne signifie PAS que l'algorithme est faux. Il établit un résultat :
avec CP = 0,50, aucune partition produite par cette règle de coupe ne peut
satisfaire en même temps l'exigence de couplage interne et la contrainte M/2.
C'est un constat à rapporter, pas une panne à réparer.
Vous rencontrerez la même situation sur le projet de validation.


2. LES DÉMONSTRATIONS JAVA
--------------------------
Construction et exécution (JDK 17 minimum, Maven) :

    mvn -q clean package
    java -cp target/classes demo.TinyTreeDemo
    java -cp target/classes demo.TieBreakDemo

demo/Node.java
    Une structure d'arbre minimale : une feuille porte une classe, un nœud
    interne porte deux fils et la valeur de la fusion. C'est la forme que peut
    prendre un dendrogramme en mémoire. Vous n'êtes pas obligés de la reprendre.

demo/TinyTreeDemo.java
    Construit À LA MAIN l'arbre du micro exemple ci-dessus et montre trois façons de le restituer :
      - un arbre indenté lisible dans un terminal ;
      - une liste de fusions, une par ligne, facile à comparer ou à tester ;
      - un fichier DOT, pour produire une image avec Graphviz :
            dot -Tpng dendrogramme.dot -o dendrogramme.png
    Objectif : vous donner des formats de sortie exploitables, pour que la
    restitution ne devienne pas le vrai problème du TP.

demo/TieBreakDemo.java
    Montre, sur une matrice de similarité de quatre éléments contenant une
    ÉGALITÉ volontaire, comment la règle de départage du sujet désigne un
    gagnant unique : à égalité de similarité maximale, on compare les couples
    d'identifiants lexicographiquement et on retient le plus petit. Il affiche
    tous les couples candidats, triés, puis le couple retenu.
    Il n'implémente PAS la boucle de regroupement.


3. CE QUE CES EXEMPLES NE FONT PAS
----------------------------------
Ils ne lisent pas les faits, ne calculent pas le couplage, ne cherchent pas la
paire la plus similaire, ne recalculent pas les similarités après une fusion et
ne coupent pas l'arbre.
Ces cinq opérations sont le TP.