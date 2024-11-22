package servidor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * TODO: Complementa esta clase para que acepte conexiones TCP con clientes para
 * recibir un boleto, generar la respuesta y finalizar la sesion
 */

public class ServidorTCP {
	private Socket socketCliente;
	private ServerSocket socketServidor;
	private BufferedReader entrada;
	private PrintWriter salida;
	private String[] respuesta;
	private int[] combinacion;
	private int reintegro;
	private int complementario;
	private int[] clienteBoleto;

	public ServidorTCP(int puerto) {
		this.respuesta = new String[9];
		this.respuesta[0] = "Boleto inv lido - N meros repetidos";
		this.respuesta[1] = "Boleto inv lido - n meros incorretos (1-49)";
		this.respuesta[2] = "6 aciertos";
		this.respuesta[3] = "5 aciertos + complementario";
		this.respuesta[4] = "5 aciertos";
		this.respuesta[5] = "4 aciertos";
		this.respuesta[6] = "3 aciertos";
		this.respuesta[7] = "Reintegro";
		this.respuesta[8] = "Sin premio";
		generarCombinacion();
		imprimirCombinacion();
		try {
			socketServidor = new ServerSocket(puerto);
			System.out.println("Esperando conexi�n...");
			socketCliente = socketServidor.accept();
			System.out.println("Conexi�n acceptada: " + socketCliente);
			entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
			salida = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketCliente.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println("No puede escuchar en el puerto: " + puerto);
			System.exit(-1);
		}
	}

	/**
	 * @return Debe leer la combinacion de numeros que le envia el cliente
	 */
	public String leerCombinacion() {

		String respuesta = "";
		try {
			// Relleno la respuesta con el resultado que viene de cliente
			respuesta = entrada.readLine();
			// Si esta vacio devuelvo "FIN" en respuesta pra que termine tambien el servidor
			if (respuesta == null || respuesta.equals("FIN")) {
				respuesta = "FIN";
			} else {
				// Como es un string y tengo que pasarlo a un int primero le hago un split lo
				// meto en un array
				String[] partes = respuesta.split(" ");

				clienteBoleto = new int[6];
				// Y aqui con el envoltorio los paso al nuevo array y ahora de ints
				for (int i = 0; i < partes.length; i++) {
					clienteBoleto[i] = Integer.parseInt(partes[i]);
				}
				return respuesta;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return respuesta;

	}

	/**
	 * @return Debe devolver una de las posibles respuestas configuradas
	 */
	public String comprobarBoleto() {
		// Variables que necesito para comprobar el boleto
		int aciertos = 0;
		boolean tieneComplementario = false;
		boolean tieneReintegro = false;
		boolean numerosNoValidos = false;
		boolean numerosRepetidos = false;
		// Doble for super curte para comprobar si hay numeros repetidos
		for (int i = 0; i < clienteBoleto.length; i++) {
			for (int j = i + 1; j < clienteBoleto.length; j++) {
				if (clienteBoleto[i] == clienteBoleto[j]) {
					numerosRepetidos = true;
				}
			}
		}
		// Doble for para ir comprobando cada numero del boletoCliente con la
		// combinacion
		for (int i = 0; i < combinacion.length; i++) {

			for (int j = 0; j < clienteBoleto.length; j++) {
				// Si acerto el complementario
				if (clienteBoleto[i] == complementario) {
					tieneComplementario = true;
					// Si acerto el reintegro
				} else if (clienteBoleto[i] == reintegro) {
					tieneReintegro = true;
					// Si se pasa de rango
				} else if (clienteBoleto[i] < 1 || clienteBoleto[i] > 49) {
					numerosNoValidos = true;
					// Los aciertos que tiene
				} else if (combinacion[i] == clienteBoleto[j]) {
					aciertos++;
				}
			}
			// A partir de aqui devuelvo todas las opciones disponibles
		}
		if (numerosRepetidos) {
			return respuesta[0];
		}
		if (tieneComplementario && aciertos == 5) {
			return respuesta[3];
		} else if (tieneReintegro) {
			return respuesta[7];
		} else if (numerosNoValidos) {
			return respuesta[1];
		} else if (numerosRepetidos) {
			return respuesta[0];
		} else if ((aciertos >= 3 && aciertos <= 6) && tieneComplementario == false) {
			switch (aciertos) {
			case 6:
				return respuesta[2];
			case 5:
				return respuesta[4];
			case 4:
				return respuesta[5];
			case 3:
				return respuesta[6];
			default:
			}
		} else {
			return respuesta[8];
		}
		return "ha fallado algo";

	}

	public void enviarRespuesta(String respuesta) {
		salida.println(respuesta);
	}

	public void finSesion() {
		try {
			salida.close();
			entrada.close();
			socketCliente.close();
			socketServidor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("-> Servidor Terminado");
	}

	private void generarCombinacion() {
		Set<Integer> numeros = new TreeSet<Integer>();
		Random aleatorio = new Random();
		while (numeros.size() < 6) {
			numeros.add(aleatorio.nextInt(49) + 1);
		}
		int i = 0;
		this.combinacion = new int[6];
		for (Integer elto : numeros) {
			this.combinacion[i++] = elto;
		}
		this.reintegro = aleatorio.nextInt(49) + 1;
		this.complementario = aleatorio.nextInt(49) + 1;
	}

	private void imprimirCombinacion() {
		System.out.print("Combinaci n ganadora: ");
		for (Integer elto : this.combinacion)
			System.out.print(elto + " ");
		System.out.println("");
		System.out.println("Complementario:       " + this.complementario);
		System.out.println("Reintegro:            " + this.reintegro);
	}

}