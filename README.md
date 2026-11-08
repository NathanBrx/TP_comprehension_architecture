# HAI913I - TP2 : Compréhension et Architectures

Suite du TP1 : https://github.com/NathanBrx/TP_analyse_statique

## Projet et Architecture

- Chemin d'entrée retenu : Chemin 1 (export des faits depuis le TP1 via la classe FactsCsvExporter dans le dossier resources-TP1).

- Entrées attendues : Trois fichiers CSV (classes.csv, methods.csv, calls.csv).

- Sorties produites : Toutes les réponses aux étapes de vérification, ex : graphe pondéré de couplage (CSV et DOT), historique des fusions du clustering hiérarchique, dendrogramme textuel et graphique, et liste des modules candidats validés par le seuil CP, etc.

- Limites connues : Exclusion par conception de l'héritage et de l'instanciation simple.

## Prérequis

- OpenJDK 17 ou supérieur

- Apache Maven 3.9 ou supérieur

- Graphviz (pour la conversion des fichier dot en png)

## Compilation

Pour construire le projet depuis un état propre, exécutez la commande suivante **dans le dossier starter** :

mvn clean package

Cela générera une archive exécutable dans le dossier target/

## Utilisation

java -jar target/hai913i-tp2.jar <DOSSIER_DES_FICHIERS_CSV> [--cp <valeur>]

Le dossier des faits fourni est ../resources-TP1/

L'option --cp est optionnelle, par défaut égale à 0,025

## Validation et Points de Contrôle

L'exécution de la commande produit les indicateurs de référence suivants (jeu de données de validation) :

- A0 : 15 classes, 59 méthodes, 44 arcs de graphe.

- A1 : 37 arcs inter-classes (7 arcs internes écartés), |Einter| = 37, couple le + couplé est Item--LoanService avec r = 4 et C = 0,1081. Somme des couplages égale à 1.0000.

- A2 : 15 sommets, 19 arêtes, 2 classes isolées (Dvd et LoanException).

- B1 : Exactement 14 fusions, trois premières fusions : {Item, LoanService} : 0,1081 ; {LibraryApp, Catalog} : 0,0811 ; {Item, LoanService, LibraryApp, Catalog} : 0,0608.

- B3 : Pour CP = 0.025, obtention de 7 modules (respectant strictement la limite M/2 = 7).
