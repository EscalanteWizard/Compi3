
import java.io.IOException;
// import AppAux; // Si es necesario, pero solo si AppAux.java está en src/ y sin paquete

public class App {

    private static AppAux app = new AppAux();
    private static final String ERROR_FILE = "src/output/errors.log";

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        try {
            // Verificar si el archivo de errores existe y eliminarlo si es necesario
            app.ejecutarLexer();

            // Ejecutar el análisis léxico y sintáctico
            app.ejecutarLexerParser();
            System.out.println("Proceso completado exitosamente");

            // --- Generar código MIPS ---
            MIPSGenerator mipsGen = new MIPSGenerator();
            mipsGen.generateMips();
            System.out.println("Código MIPS generado en src/output/mipsCode.asm");
            
        } catch (Exception e) {
            try {
                FileManager.writeFile(ERROR_FILE, "Error: " + e.getMessage());
            } catch (IOException ioEx) {
                System.err.println("Error al escribir log: " + ioEx.getMessage());
            }
            System.err.println("Error durante el análisis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}