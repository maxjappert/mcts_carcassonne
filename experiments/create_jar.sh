echo Creating jar file...
cd ../out/production/mcts_carcassonne || exit
jar cfm carcassonne.jar META-INF/MANIFEST.MF * && echo .jar file successfully created!
cp carcassonne.jar ../../../experiments
rm carcassonne.jar

