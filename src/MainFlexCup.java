import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.*;

import java_cup.runtime.Symbol;

public class MainFlexCup {

    private static final String INPUT_FILE = "resources/ejemplo2.txt";
    private static final String ERROR_FILE = "output/errors.log";
    private static String basePath = System.getProperty("user.dir");
    private static final String path = Paths.get(basePath, INPUT_FILE).toString();

    // Analizador léxico: solo tokens
    public void AnalizadorLexico() throws IOException {
        Reader reader = new BufferedReader(new FileReader(path));
        BasicLexerCup lex = new BasicLexerCup(reader);
        int i = 0;
        Symbol token;
        while (true) {
            token = lex.next_token();
            if (token.sym != 0) {
                // Puedes imprimir o procesar tokens aquí si lo deseas
            } else {
                // Fin de archivo
                return;
            }
            i++;
        }
    }

    // Analizador léxico y sintáctico
    public static void AnalizadorLexicoSintactico() throws Exception {
        // Leer el código fuente
        String sourceCode = FileManager.readFile(INPUT_FILE);

        // Crear el ErrorHandler compartido
        ErrorHandler errorHandler = new ErrorHandler(ERROR_FILE);
        errorHandler.setContinueOnError(true);

        try (Reader reader = new StringReader(sourceCode)) {
            // Crear el lexer
            BasicLexerCup lexer = new BasicLexerCup(reader);
            lexer.setErrorHandler(errorHandler);

            // Crear el parser y conectarlo con el lexer
            parser p = new parser(lexer);
            p.setErrorHandler(errorHandler);

            try {
                System.out.println("Iniciando análisis sintáctico...");
                Symbol parseResult = p.parse();
                System.out.println("Análisis sintáctico completado exitosamente.");
            } catch (Exception e) {
                System.out.println("Análisis sintáctico completado con errores: " + e.getMessage());
            }

            // Puedes mostrar estadísticas de errores aquí si lo deseas
            // System.out.println(errorHandler.getErrorSummary());
        }
    }
}