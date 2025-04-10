package org.example.app

import org.example.service.ServicioCalc
import org.example.ui.Consola
import org.example.ui.IEntradaSalida
import org.example.utils.IFicheros
import org.example.utils.Utils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import kotlin.system.exitProcess

/**
 * Clase principal que representa el menú principal de la calculadora que interactúa con
 * el usuario para realizar operaciones matemáticas básicas.
 *
 * @property calculadora Servicio que realiza las operaciones matemáticas.
 * @property consola Interfaz de E/S de datos del usuario.
 * @property ficheros Interfaz de gestión de ficheros.
 * @property args Los parámetros que recibe al iniciarse el programa mediante main.
 */
class GestorMenu(private val calculadora: ServicioCalc, private val consola: IEntradaSalida, private val ficheros: IFicheros, private val args: Array<String>) {
    companion object {
        // Constantes para establecer la ruta por defecto y el prefijo para los archivos log.
        const val RUTA_DEFECTO = "./log"
        const val PREFIJO_LOG = "log"

        // PATTERN CON EL FORMATO DE LA FECHA
        val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYYMMDDHHMMSS")
    }

    /**
     * Inicia el programa y gestiona la lógica de entrada según el número de argumentos proporcionados.
     *
     * Este método realiza las siguientes acciones:
     * - Si no se proporcionan argumentos, utiliza una ruta por defecto y crea un nuevo directorio si no existe.
     * - Si se proporciona un argumento, se utiliza como ruta y se crea un nuevo directorio si no existe.
     * - Si el directorio está vacío, se crea un nuevo fichero de log.
     * - Si hay ficheros de log existentes, se muestra el contenido del fichero más reciente.
     * - Si se proporcionan exactamente cuatro argumentos, se realiza una operación matemática (suma, resta, multiplicación o división)
     *   con los dos primeros argumentos como operandos y el tercer argumento como operador. El resultado se muestra y se registra en el fichero de log.
     * - Si el número de argumentos no es válido, se muestra un mensaje de error y se termina el programa.
     *
     * @throws IllegalArgumentException Si se proporciona un operador no válido para la operación matemática.
     */
    fun iniciar() {

        var nombreFichero = ""
        val ruta: String

        when (args.size) {

            0 -> {

                ruta = RUTA_DEFECTO

                if (!ficheros.comprobarDirectorio(ruta)) {
                    ficheros.crearDirectorio(ruta)
                    consola.mostrarMensaje("Ruta $ruta creada", salto = true)
                }

                if (ficheros.esDirectorioVacio(ruta)) {
                    consola.mostrarMensaje("No existen ficheros de Log", salto = true)
                    nombreFichero = ficheros.crearFichero(ruta, PREFIJO_LOG + LocalDateTime.now().format(FORMATO_FECHA))
                } else {
                    nombreFichero = ficheros.obtenerFicheroReciente(ruta)!!
                    for (i in ficheros.obtenerContenidoFichero(ruta, nombreFichero)) {
                        consola.mostrarMensaje(i, salto = true)
                    }
                }
            }

            1 -> {

                ruta = args[0]

                if (!ficheros.comprobarDirectorio(ruta)) {
                    ficheros.crearDirectorio(ruta)
                    consola.mostrarMensaje("Ruta $ruta creada", salto = true)
                }

                if (ficheros.esDirectorioVacio(ruta)) {
                    consola.mostrarMensaje("No existen ficheros de Log", salto = true)
                    nombreFichero = ficheros.crearFichero(ruta, PREFIJO_LOG + LocalDateTime.now().format(FORMATO_FECHA))
                } else {
                    nombreFichero = ficheros.obtenerFicheroReciente(ruta)!!
                    for (i in ficheros.obtenerContenidoFichero(ruta, nombreFichero)) {
                        consola.mostrarMensaje(i, salto = true)
                    }
                }
            }

            4 -> {
                var resultado: Double = 0.0
                ruta = args[0]
                nombreFichero = ficheros.crearFichero(ruta, PREFIJO_LOG + LocalDateTime.now().format(FORMATO_FECHA))

                try {
                    resultado = when (args[2]) {
                        "+" -> calculadora.sumar(args[1].toDouble(), args[3].toDouble())
                        "-" -> calculadora.restar(args[1].toDouble(), args[3].toDouble())
                        "*" -> calculadora.multiplicar(args[1].toDouble(), args[3].toDouble())
                        "/" -> calculadora.dividir(args[1].toDouble(), args[3].toDouble())
                        else -> throw IllegalArgumentException("Error en el cálculo. Verifique que introduzco números y un operador correcto.")
                    }
                    consola.mostrarMensaje(Utils.redondearNumero(resultado), salto = true)
                    ficheros.modificarFichero(ruta, nombreFichero, "**Cálculo** ===>> ${args[1]} ${args[2]} ${args[3]} = $resultado")
                } catch (e: IllegalArgumentException) {
                    consola.mostrarError(e.message.toString())
                    ficheros.modificarFichero(ruta, nombreFichero, e.message.toString())
                }
            }

            else -> {
                consola.mostrarError("Número de argumentos no válido. Saliendo del programa.")
                exitProcess(0)
            }
        }
        consola.pausar()
        consola.limpiarTerminal()
        menuCalculadora(ruta, nombreFichero)
    }


    /**
     * Inicia el menú de la calculadora, mostrando las opciones y permitiendo
     * al usuario ingresar dos números y un operador para realizar una operación.
     * El resultado de la operación se muestra al usuario, además de guardarse en un archivo log.
     * El proceso se repite hasta que el usuario decida salir.
     * También se manejan las excepciones que puedan ocurrir durante la operación.
     *
     * @param directorio La carpeta donde reside el archivo log.
     * @param nombreFichero El nombre del archivo donde se guardará el calculo o el error.
     */
    private fun menuCalculadora(directorio: String, nombreFichero: String) {
        var cont = 0
        consola.mostrarMensaje("*** Calculadora ***", salto = true)
        var opcion = consola.pedirOpcion("¿Desea realizar cálculos? s/n > ")

        while (opcion) {
            try {
                val num1 = consola.pedirNumero()
                val operador = consola.pedirOperador()
                val num2 = consola.pedirNumero()

                val resultado = when (operador) {
                    "+" -> calculadora.sumar(num1, num2)
                    "-" -> calculadora.restar(num1, num2)
                    "*" -> calculadora.multiplicar(num1, num2)
                    "/" -> calculadora.dividir(num1, num2)
                    else -> continue
                }
                consola.mostrarMensaje(Utils.redondearNumero(resultado), salto = true)
                ficheros.modificarFichero(directorio, nombreFichero, "**Cálculo $cont** ===>> $num1 $operador $num2 = $resultado")
                cont++
            } catch (e: IllegalArgumentException) {
                consola.mostrarError(e.message.toString())
                ficheros.modificarFichero(directorio, nombreFichero, e.message.toString())
            }
            // Vuelve a preguntar si quiere continuar (s/n)
            opcion = consola.pedirOpcion("¿Desea seguir realizando cálculos? s/n > ")
        }
    }
}