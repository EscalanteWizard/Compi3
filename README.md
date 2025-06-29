# Compi3
Generación de código MIPS basado en codigo de tres direcciones

Comando para ejecutar el archivo .jFlex:
java -jar src/libs/jflex-full-1.9.1.jar -d src src/BasicLexerCup.jflex

Comando para ejecutar el archivo .cup:
java -jar src/libs/java-cup-11b.jar -symbols sym -destdir src src/BasicParser.cup

Borrar todos los archivos.class
del src\*.class

Comando para compilar el programa y generar los archivos.class
javac -cp "src/libs/*" src/*.java

Comando para correr el programa:
java -cp "src;src/libs/*" App