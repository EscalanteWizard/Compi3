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
            // Aquí solo ejecuta el análisis, NO generes analizadores aquí
            app.ejecutarLexer();
            app.ejecutarLexerParser();
            System.out.println("Proceso completado exitosamente");
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