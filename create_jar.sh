echo Compiling the code...
cd src || exit
javac Main.java
echo Creating jar file...
jar cfm carcassonne.jar ../MANIFEST.MF *.class && echo .jar file successfully created!
cp carcassonne.jar ..
rm carcassonne.jar
rm *.class

