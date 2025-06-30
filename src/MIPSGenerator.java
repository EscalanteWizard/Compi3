import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * MIPSGenerator es una clase que se encarga de generar código MIPS a partir de un código en 3D.
 * Esta clase procesa el código 3D, identifica las instrucciones y genera el código MIPS correspondiente.
 * Utiliza un archivo de texto para leer el código 3D y otro para escribir el código MIPS generado.
 */
public class MIPSGenerator {

    /**
     * Clase encargada de generar el código MIPS a partir del código 3D.
     * Esta clase lee el código 3D desde un archivo, lo procesa y genera el código MIPS correspondiente.
     */
    private static FileWriter writer;
    private StringBuilder code;
    String dataSection = ".data\n";

    //Contadores de registros temporales
    int numTemporalesReg = 0;
    int numFlotantes = 0;
    int numFlotantesAux = 0;
    

    //Mensajes de la seccion de datos
    Map<String, String> dataSectionStrings = new HashMap<String, String>();

    //Listas de valores temporales
    Map<String, String> valoresTemporales = new HashMap<String, String>();
    Map<String, String> temps3DMap = new HashMap<String, String>();
    boolean[] tempRegistros = new boolean[10];

    //Diccionario de tipos de datos
    Map<String, String> tiposDatos = new HashMap<String, String>();
    Map<String, String> listaTiposDatosMap = new HashMap<String, String>();
    
    //Instrucciones de carga y almacenamiento
    String[] listaFunciones = new String[100];
    ArrayList<String> funcArgs = new ArrayList<String>();
    String tipoDatoActual = "";

    /**
     * Constructor de la clase MIPSGenerator.
     * Inicializa el StringBuilder para almacenar el código MIPS.
     */
    public void generateMips() throws IOException {
        code = new StringBuilder();
        initTiposDatos();

        String cod3Direcciones = get3DCode();
        Translate(cod3Direcciones);
    }
    
    /**
     * Obtiene el código 3D desde un archivo de texto.
     * @return el código 3D como una cadena
     * @throws IOException si ocurre un error al leer el archivo
     */
    private static String get3DCode() throws IOException {
        String code = leerArchivoTexto("src/output/codigo3D.txt");
        return code; // Return the code as a string
    }

    /**
     * Inicializa la sección de código MIPS.
     * Esta función se encarga de establecer el encabezado y las directivas necesarias      
     * @throws IOException
     */
    private void mipsInit() throws IOException {
        code.append(".text\n");
        code.append(".globl main\n");
    }

    /**
     * Divide el código 3D en instrucciones individuales y las procesa.
     * @param code el código 3D a procesar
     */
    public boolean asignacionTemp(String instruction){
        Pattern pattern = Pattern.compile("= t\\d+");
        Matcher matcher = pattern.matcher(instruction);
        return matcher.find();
    }

    private void Translate(String code) {
        try {
            writer = new FileWriter("src/output/mipsCode.asm");
            mipsInit();
            splitter(code);
            writer.write(dataSection);
            writer.write(this.code.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica si una cadena representa un número entero válido.
     * @param str la cadena a verificar
     * @return true si es un número entero válido, false en caso contrario
     */
    public static boolean verifTipoEntero(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Verifica si una cadena representa un número flotante válido.
     * @param str la cadena a verificar
     * @return true si es un número flotante válido, false en caso contrario
     */
    public static boolean verifTipoFlotante(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Lee el contenido de un archivo de texto y lo devuelve como una cadena.
     * @param fileName el nombre del archivo a leer
     * @return una cadena con el contenido del archivo
     * @throws IOException si ocurre un error al leer el archivo
     */
    public static String leerArchivoTexto(String fileName) throws IOException {
    byte[] bytes = leerBytesArchivo(fileName);
    return new String(bytes);
    }

    /**
     * Lee el contenido de un archivo y lo devuelve como un arreglo de bytes.
     * @param fileName el nombre del archivo a leer
     * @return un arreglo de bytes con el contenido del archivo
     * @throws IOException si ocurre un error al leer el archivo
     */
    public static byte[] leerBytesArchivo(String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(fileName));
    }

    /**
     * Inicializa los tipos de datos y sus instrucciones asociadas
     * así como los registros temporales.
     * Esta función se llama al inicio del proceso de generación de MIPS.
     * @param none
     * @return void
     */
    public void initTiposDatos() {
        inicializarInstruccionesCarga();
        inicializarInstruccionesAlmacenamiento();
        inicializarRegistrosTemporales();
    }

    /**
     * Inicializa el mapa de instrucciones de carga (load) para cada tipo de dato
     */
    private void inicializarInstruccionesCarga() {
        configurarTipoDatoCarga("int", "lw");
        configurarTipoDatoCarga("float", "lwci");
        configurarTipoDatoCarga("char", "lb");
        configurarTipoDatoCarga("String", "la");
    }

    /**
     * Inicializa el mapa de instrucciones de almacenamiento (store) para cada tipo de dato
     */
    private void inicializarInstruccionesAlmacenamiento() {
        configurarTipoDatoAlmacenamiento("int", "sw");
        configurarTipoDatoAlmacenamiento("float", "swci");
        configurarTipoDatoAlmacenamiento("char", "sb");
        configurarTipoDatoAlmacenamiento("String", "la");
    }

    /**
     * Inicializa los registros temporales vacíos
     */
    private void inicializarRegistrosTemporales() {
        int numRegistros = 11;
        for (int i = 0; i < numRegistros; i++) {
            configurarRegistroTemporal(i);
        }
    }

    /**
     * Configura una instrucción de carga para un tipo de dato específico
     */
    private void configurarTipoDatoCarga(String tipoDato, String instruccion) {
        tiposDatos.put(tipoDato, instruccion);
    }

    /**
     * Configura una instrucción de almacenamiento para un tipo de dato específico
     */
    private void configurarTipoDatoAlmacenamiento(String tipoDato, String instruccion) {
        listaTiposDatosMap.put(tipoDato, instruccion);
    }

    /**
     * Configura un registro temporal individual
     */
    private void configurarRegistroTemporal(int indice) {
        String nombreRegistro = crearNombreRegistroTemporal(indice);
        valoresTemporales.put(nombreRegistro, "");
    }

    /**
     * Crea el nombre de un registro temporal basado en su índice
     */
    private String crearNombreRegistroTemporal(int indice) {
        return "t" + indice;
    }

    /**
     * Maneja la asignación de registros temporales y flotantes
     * dependiendo del tipo de dato que se necesite.
     * @param nextTipoDato el tipo de dato del siguiente registro
     * @return el registro temporal o flotante correspondiente
     */
    public String obtenerTipoSiguienteDato(String nextTipoDato) {
    if (nextTipoDato == null) {
        System.err.println("Advertencia: nextTipoDato es null, usando 'int' por defecto.");
        nextTipoDato = "int";
    }
    if (esIntegerType(nextTipoDato)) {
        return obtenerRegistroTemporal(false); // no incrementar antes
    }
    else if (esFloatType(nextTipoDato)) {
        return obtenerRegistroFlotante(false); // no incrementar antes
    }
    else if (esCharType(nextTipoDato)) {
        return obtenerRegistroTemporal(true); // incrementar antes
    }
    else if (esStringType(nextTipoDato)) {
        return obtenerRegistroTemporal(true); // incrementar antes
    }
    // Si no se reconoce el tipo, retorna un registro temporal por defecto
    System.err.println("Advertencia: tipo de dato desconocido (" + nextTipoDato + "), usando $t0 por defecto.");
    return " $t0";
    }

    /**
     * Verifica si el tipo de dato es entero
     */
    private boolean esIntegerType(String tipo) {
    return "int".equals(tipo); // Protege contra null
    }

    /**
     * Verifica si el tipo de dato es flotante
     */
    private boolean esFloatType(String tipo) {
    return "float".equals(tipo); // Protege contra null
    }

    /**
     * Verifica si el tipo de dato es caracter
     */
    private boolean esCharType(String tipoDato) {
        return "char".equals(tipoDato); // Protege contra null
    }

    /**
     * Verifica si el tipo de dato es String
     */
    private boolean esStringType(String tipoDato) {
        return "String".equals(tipoDato); // Protege contra null
    }

    /**
     * Obtiene un registro temporal ($t)
     * @param incrementarAntes si debe incrementar el contador antes de usarlo
     */
    private String obtenerRegistroTemporal(boolean incrementarAntes) {
        if (incrementarAntes) {
            numTemporalesReg++;
            return " $t" + numTemporalesReg;
        } else {
            numTemporalesReg++;
            return " $t" + (numTemporalesReg - 1);
        }
    }

    /**
     * Obtiene un registro flotante ($f)
     * @param incrementarAntes si debe incrementar el contador antes de usarlo
     */
    private String obtenerRegistroFlotante(boolean incrementarAntes) {
        if (incrementarAntes) {
            numFlotantesAux++;
            return " $f" + numFlotantesAux;
        } else {
            numFlotantesAux++;
            return " $f" + (numFlotantesAux - 1);
        }
    }

    /**
     * Divide el código en líneas y procesa cada línea
     * @param code el código fuente en 3 direcciones
     * @throws IOException si ocurre un error al escribir en el archivo
     */
    private void splitter(String code) throws IOException {
        String[] lines = dividirEnLineas(code);
        
        for (String line : lines) {
            procesarLinea(line);
        }
    }

    /**
     * Divide el código en líneas individuales
     */
    private String[] dividirEnLineas(String code) {
        return code.split("\n");
    }

    /**
     * Procesa una línea individual del código
     */
    private void procesarLinea(String line) throws IOException {
        String lineaLimpia = limpiarLinea(line);
        
        if (debeIgnorarLinea(lineaLimpia)) {
            return;
        }
        
        if (esEtiqueta(lineaLimpia)) {
            etiquetado(lineaLimpia);
        } else if (esInstruccion(lineaLimpia)) {
            manejadorDeBloques(lineaLimpia);
        }
    }

    /**
     * Limpia una línea removiendo espacios en blanco al inicio y final
     */
    private String limpiarLinea(String line) {
        return line.trim();
    }

    /**
     * Verifica si una línea debe ser ignorada (vacía o comentario)
     */
    private boolean debeIgnorarLinea(String line) {
        return esLineaVacia(line) || esComentario(line);
    }

    /**
     * Verifica si una línea está vacía
     */
    private boolean esLineaVacia(String line) {
        return line.isEmpty();
    }

    /**
     * Verifica si una línea es un comentario
     */
    private boolean esComentario(String line) {
        return line.startsWith("#");
    }

    /**
     * Verifica si una línea es una etiqueta
     */
    private boolean esEtiqueta(String line) {
        return line.endsWith(":");
    }

    /**
     * Verifica si una línea es una instrucción
     */
    private boolean esInstruccion(String line) {
        // Nota: La condición original line.startsWith("") siempre es true
        // Asumo que debería verificar si no es etiqueta y no está vacía
        return !esEtiqueta(line) && !esLineaVacia(line);
    }

    /**
     * Procesa una etiqueta y la agrega al código MIPS
     * @param label la etiqueta a procesar
     * @throws IOException si ocurre un error al escribir en el archivo
     */
    private void etiquetado(String label) throws IOException {
        code.append(label + "\n");
    }

    /**
 * Función principal que maneja las instrucciones de bloques
 */
private void manejadorDeBloques(String instruction) throws IOException {
    String[] data = dividirInstruccion(instruction);
    int largo = data.length;
    
    if (esDeclaracionVariable(instruction)) {
        manejarDeclaracionVariable(instruction, data);
    } else if (esInstruccionControl(instruction)) {
        manejarInstruccionControl(instruction, data);
    } else if (esInstruccionFuncion(instruction)) {
        manejarInstruccionFuncion(instruction, data);
    } else if (esInstruccionTemporal(instruction)) {
        manejarInstruccionTemporal(instruction, data, largo);
    } else if (esAsignacionTemporal(instruction, largo)) {
        manejarAsignacionTemporal(instruction, data);
    } else {
        manejarInstruccionGenerica(instruction);
    }
}

/**
 * Divide la instrucción en sus componentes
 */
private String[] dividirInstruccion(String instruction) {
    return instruction.split(" ");
}

/**
 * Verifica si es una declaración de variable
 */
private boolean esDeclaracionVariable(String instruction) {
    return instruction.startsWith("dataArray") || 
           instruction.startsWith("dataChar") || 
           instruction.startsWith("dataInt") || 
           instruction.startsWith("dataFloat");
}

/**
 * Verifica si es una instrucción de control de flujo
 */
private boolean esInstruccionControl(String instruction) {
    return instruction.startsWith("if") || 
           instruction.startsWith("goto") || 
           instruction.startsWith("return");
}

/**
 * Verifica si es una instrucción de función
 */
private boolean esInstruccionFuncion(String instruction) {
    return instruction.startsWith("param") || 
           instruction.startsWith("call");
}

/**
 * Verifica si es una instrucción temporal
 */
private boolean esInstruccionTemporal(String instruction) {
    return instruction.startsWith("t");
}

/**
 * Verifica si es una asignación temporal
 */
private boolean esAsignacionTemporal(String instruction, int largo) {
    return asignacionTemp(instruction) && largo == 3;
}

/**
 * Maneja todas las declaraciones de variables
 */
private void manejarDeclaracionVariable(String instruction, String[] data) {
    if (instruction.startsWith("dataArray")) {
        manejarDeclaracionArray(data);
    } else if (instruction.startsWith("dataChar")) {
        manejarDeclaracionChar(data);
    } else if (instruction.startsWith("dataInt")) {
        manejarDeclaracionInt(data);
    } else if (instruction.startsWith("dataFloat")) {
        manejarDeclaracionFloat(data);
    }
}

/**
 * Maneja declaración de array
 */
private void manejarDeclaracionArray(String[] data) {
    code.append("#declaracion de array\n");
    String nombreArray = extraerNombreArray(data[1]);
    String size = extraerTamanoArray(data[1]);
    int espacioTotal = Integer.parseInt(size) * 4;
    dataSection += nombreArray + ": .space " + espacioTotal + "\n";
}

/**
 * Extrae el nombre del array de la declaración
 */
private String extraerNombreArray(String declaracion) {
    return declaracion.substring(0, declaracion.indexOf("["));
}

/**
 * Extrae el tamaño del array de la declaración
 */
private String extraerTamanoArray(String declaracion) {
    return declaracion.substring(declaracion.indexOf("[") + 1, declaracion.indexOf("]"));
}

/**
 * Maneja declaración de char
 */
private void manejarDeclaracionChar(String[] data) {
    code.append("#declaracion de char\n");
    String nombreChar = data[1];
    dataSection += nombreChar + ": .space 1\n";
}

/**
 * Maneja declaración de int
 */
private void manejarDeclaracionInt(String[] data) {
    code.append("#declaracion de int\n");
    String nombreInt = data[1];
    dataSection += nombreInt + ": .word 0\n";
    dataSectionStrings.put(nombreInt, "int");
    tipoDatoActual = "int";
}

/**
 * Maneja declaración de float
 */
private void manejarDeclaracionFloat(String[] data) {
    code.append("#declaracion de float\n");
    String nombreFloat = data[1];
    dataSection += nombreFloat + ": .float 0.0\n";
    dataSectionStrings.put(nombreFloat, "float");
    tipoDatoActual = "float";
}

/**
 * Maneja instrucciones de control de flujo
 */
private void manejarInstruccionControl(String instruction, String[] data) {
    if (instruction.startsWith("if")) {
        manejarIf(data);
    } else if (instruction.startsWith("goto")) {
        manejarGoto(data);
    } else if (instruction.startsWith("return")) {
        manejarReturn();
    }
}

/**
 * Maneja instrucción if
 */
private void manejarIf(String[] data) {
    code.append("#if\n");
    code.append("beqz $t" + (numTemporalesReg - 1) + ", " + data[3] + "\n");
}

/**
 * Maneja instrucción goto
 */
private void manejarGoto(String[] data) {
    code.append("#goto\n");
    code.append("j " + data[1] + "\n");
}

/**
 * Maneja instrucción return
 */
private void manejarReturn() {
    code.append("#return\n");
    if (tieneUltimaFuncion()) {
        code.append("j " + listaFunciones[listaFunciones.length - 1] + "\n");
    } else {
        generarSalidaPrograma();
    }
}

/**
 * Verifica si hay una última función disponible
 */
private boolean tieneUltimaFuncion() {
    return listaFunciones[listaFunciones.length - 1] != null;
}

/**
 * Genera código para salir del programa
 */
private void generarSalidaPrograma() {
    code.append("li $v0, 10\n");
    code.append("syscall\n");
}

/**
 * Maneja instrucciones de función
 */
private void manejarInstruccionFuncion(String instruction, String[] data) {
    if (instruction.startsWith("param")) {
        manejarParametro(data);
    } else if (instruction.startsWith("call")) {
        manejarLlamadaFuncion(data);
    }
}

/**
 * Maneja parámetros de función
 */
private void manejarParametro(String[] data) {
    funcArgs.add(data[1]);
}

/**
 * Maneja llamadas a función
 */
private void manejarLlamadaFuncion(String[] data) {
    if (data[1].equals("print,")) {
        manejarLlamadaPrint();
    }
}

/**
 * Maneja llamada a función print
 */
private void manejarLlamadaPrint() {
    code.append("#print\n");
    String param = funcArgs.remove(funcArgs.size() - 1);
    String registroParam = temps3DMap.get(param);
    
    System.out.println(registroParam);
    System.out.println(valoresTemporales.get(registroParam));
    
    code.append("move $a0, $" + param + "\n");
    code.append("li $v0, 1\n");
    code.append("syscall\n");
}

/**
 * Maneja instrucciones temporales
 */
private void manejarInstruccionTemporal(String instruction, String[] data, int largo) {
    if (largo == 3) {
        manejarAsignacionSimple(instruction, data);
    } else if (largo == 5) {
        manejarOperacionBinaria(data);
    }
}

/**
 * Maneja asignaciones simples a temporales
 */
private void manejarAsignacionSimple(String instruction, String[] data) {
    if (data[2].startsWith("++")) {
        manejarIncremento(data);
    } else if (esLiteralCaracterOEntero(data[2])) {
        manejarAsignacionLiteral(data);
    } else if (verifTipoFlotante(data[2])) {
        manejarAsignacionFlotante(data);
    } else if (instruction.contains("= t")) {
        manejarAsignacionTemporal(data);
    } else {
        manejarCargaVariable(data);
    }
}

/**
 * Verifica si es un literal de carácter o entero
 * @param valor el valor a verificar
 * @return true si es un literal de carácter (entre comillas simples) o un entero
 */
private boolean esLiteralCaracterOEntero(String valor) {
    // Verifica si el valor es un literal de carácter (entre comillas simples) o un entero
    // También verifica si es un entero válido
    return valor.startsWith("'") || verifTipoEntero(valor);
}

/**
 * Maneja operación de incremento
 * Esta función se encarga de manejar el incremento de una variable.
 * Carga el valor de la variable en un registro temporal, incrementa su valor
 */
private void manejarIncremento(String[] data) {
    String variable = data[2].substring(2);
    code.append("lw $t" + numTemporalesReg + ", " + variable + "\n");
    numTemporalesReg++;
    code.append("addi $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 1) + ", 1\n");
    code.append("sw $t" + numTemporalesReg + ", " + variable + "\n");
}

/**
 * Maneja asignación de literal
 * Esta función se encarga de manejar la asignación de un valor literal a un registro temporal.
 * Si es la primera asignación de un literal, se inicia la sección de datos.
 */
private void manejarAsignacionLiteral(String[] data) {
    if (numTemporalesReg == 0) {
        code.append(".data\n");
    }
    code.append("li $t" + numTemporalesReg + ", " + data[2] + "\n");
    numTemporalesReg++;
}

/**
 * Maneja asignación de flotante
 * Esta función se encarga de manejar la asignación de un valor flotante a un registro flotante.
 * Si es la primera asignación de flotante, se inicia la sección de datos.
 */
private void manejarAsignacionFlotante(String[] data) {
    if (numFlotantesAux == 0) {
        code.append(".data\n");
    }
    code.append("li.s $f" + numFlotantesAux + ", " + data[2] + "\n");
    numFlotantesAux++;
}

/**
 * Maneja asignación temporal
 * Esta función se encarga de manejar la asignación de un valor temporal a un registro temporal.
 * Si el registro temporal ya tiene un valor asignado, se mueve ese valor al nuevo registro temporal.
 * Si no, se carga el valor de la variable en el registro temporal.
 */
private void manejarAsignacionTemporal(String[] data) {
    // Verifica si el registro temporal ya tiene un valor asignado
    if (valoresTemporales.containsKey(data[2])) {
        String registroTemporal = valoresTemporales.get(data[2]);
        code.append("move $t" + numTemporalesReg + ", " + registroTemporal + "\n");
    } else {
        // Si no, carga el valor de la variable en el registro temporal
        manejarCargaVariable(data);
    }
    code.append("sb $t" + numTemporalesReg + ", " + data[2] + "\n");
}

/**
 * Maneja carga de variable
 * Esta función se encarga de cargar el valor de una variable en un registro temporal.
 * Si el tipo de dato no está definido, se asigna un tipo por defecto (int).
 * @param data un arreglo de cadenas que contiene la información de la variable
 * @return void
 * @throws IOException si ocurre un error al escribir en el archivo
 */
private void manejarCargaVariable(String[] data) {
    String tipoDato = dataSectionStrings.get(data[2]);
    if (tipoDato == null) {
        System.err.println("Advertencia: tipoDato null para variable: " + data[2]);
        tipoDato = "int";
    }
    String register = obtenerTipoSiguienteDato(tipoDato).substring(1);

    code.append(tiposDatos.get(tipoDato) + " " + register + ", " + data[2] + "\n");

    System.out.println("inter:" + data[0] + " register:" + register + ",  data[2] = " + data[2]);
    valoresTemporales.put(register, data[2]);
    temps3DMap.put(data[0], register);
}

/**
 * Maneja operaciones binarias
 */
private void manejarOperacionBinaria(String[] data) {
    String operador = data[3];
    code.append("#op = " + operador + "\n");
    
    switch (operador) {
        case "+":
            manejarSuma();
            break;
        case "-":
            manejarResta();
            break;
        case ">":
            manejarComparacionMayor();
            break;
        case "<":
            manejarComparacionMenor();
            break;
        case "==":
            manejarComparacionIgual();
            break;
        case "!=":
            manejarComparacionDistinto();
            break;
        case "^":
            manejarOperacionAnd();
            break;
        case "#":
            manejarOperacionOr();
            break;
        case "*":
            manejarMultiplicacion();
            break;
        case "/":
            manejarDivision();
            break;
    }
}

/**
 * Maneja operación de suma
 */
private void manejarSuma() {
    if (tipoDatoActual.equals("int")) {
        code.append("add $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
        numTemporalesReg++;
    } else if (tipoDatoActual.equals("float")) {
        code.append("add.s $f" + numFlotantesAux + ", $f" + (numFlotantesAux - 2) + ", $f" + (numFlotantesAux - 1) + "\n");
        numFlotantesAux++;
    }
}

/**
 * Maneja operación de resta
 */
private void manejarResta() {
    code.append("sub $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    numTemporalesReg++;
    code.append("move $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 1) + "\n");
}

/**
 * Maneja comparación mayor que
 */
private void manejarComparacionMayor() {
    code.append("sgt $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    numTemporalesReg++;
}

/**
 * Maneja comparación menor que
 */
private void manejarComparacionMenor() {
    code.append("slt $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    numTemporalesReg++;
}

/**
 * Maneja comparación igual
 */
private void manejarComparacionIgual() {
    code.append("seq $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    numTemporalesReg++;
}

/**
 * Maneja comparación distinto
 * Esta instrucción verifica si dos valores son diferentes
 * y almacena el resultado en un registro temporal.
 * @param none
 * @return void
 * sne = set not equal
 */
private void manejarComparacionDistinto() {
    code.append("sne $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    numTemporalesReg++;
}

/**
 * Maneja operación AND lógico
 */
private void manejarOperacionAnd() {
    code.append("and $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    numTemporalesReg++;
}

/**
 * Maneja operación OR lógico
 */
private void manejarOperacionOr() {
    code.append("or $t" + numTemporalesReg + ", $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    numTemporalesReg++;
}

/**
 * Maneja operación de multiplicación
 */
private void manejarMultiplicacion() {
    code.append("mult $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    code.append("mflo $t" + numTemporalesReg + "\n");
    numTemporalesReg++;
}

/**
 * Maneja operación de división
 */
private void manejarDivision() {
    code.append("div $t" + (numTemporalesReg - 2) + ", $t" + (numTemporalesReg - 1) + "\n");
    code.append("mflo $t" + numTemporalesReg + "\n");
    numTemporalesReg++;
}

/**
 * Maneja asignación temporal final
 */
private void manejarAsignacionTemporal(String instruction, String[] data) {
    if (tipoDatoActual.equals("int")) {
        code.append(listaTiposDatosMap.get(tipoDatoActual) + " $t" + (numTemporalesReg - 1) + ", " + data[0] + "\n");
    } else if (tipoDatoActual.equals("float")) {
        code.append(listaTiposDatosMap.get(tipoDatoActual) + " $f" + (numFlotantesAux - 1) + ", " + data[0] + "\n");
    }
}

/**
 * Maneja instrucciones genéricas que no tienen tratamiento especial
 */
private void manejarInstruccionGenerica(String instruction) {
    code.append(instruction + "\n");
}

}
