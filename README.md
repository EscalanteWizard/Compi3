# Compi3
MIPS code generation based on 3dCode from project 2

Terminal command to run .jFlex file and generate the Lex file:
java -jar src/libs/jflex-full-1.9.1.jar -d src src/BasicLexerCup.jflex

Command to execute .cup file:
java -jar src/libs/java-cup-11b.jar -symbols sym -destdir src src/BasicParser.cup

Delete all .class files:
del src\*.class

Program compile:
javac -cp "src/libs/*" src/*.java

Command to run the main file:
java -cp "src;src/libs/*" App

To access to a full documentation for this repo please follow the next link:
https://deepwiki.com/EscalanteWizard/Compi3