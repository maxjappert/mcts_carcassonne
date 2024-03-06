./create_jar.sh
java -jar carcassonne.jar --p1 human --p2 decaying-uct-tuned --p2trainingiterations 500 --p2ensembleiterations 4 --p2meepleplacementprob 0.3 --p2explorationconstant 512
