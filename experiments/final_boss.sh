./create_jar.sh
java -jar carcassonne.jar --p$1 human --p$2 decaying-uct-tuned --p$2explorationconstant 512 --p$2trainingiterations 1000 --p$2meepleplacementprob 0.3 --p$2ensembleiterations 4

