package OTT;

import java.net.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class OTT {

	public static boolean EXIT = false;
	public static boolean querVerStream;

	private static void estabeleConnectioVizinho(String vizinho, String ipAdress, RTPpacketQueue rtPpacketQueue, DadosNodo dadosNodo) throws IOException {
		Socket socket = new Socket(vizinho, 8080);

		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		if (socket.getInetAddress().isReachable(1000)) {
			dadosNodo.addVizinho(vizinho, new DadosVizinho(vizinho, dos, dis, socket));

			byte[] mensagemConnectionVizinho = ("VIZINHO-" + ipAdress + "\n").getBytes();
			dos.write(mensagemConnectionVizinho);
			dos.flush();

			ThreadOTTReceiver receiver = new ThreadOTTReceiver(false, ipAdress, dis, dadosNodo, rtPpacketQueue);
			ThreadOTTSender sender     = new ThreadOTTSender(socket, dos, dadosNodo.getVizinho(vizinho).getMessagesToSend());

			receiver.start();
			sender.start();
		}
	}

	private static void estabeleConnectioVizinhoWaiting(String[] dadosConnection, DataOutputStream dos, BufferedReader dis, Socket socket, String ipAdress, DadosNodo dadosNodo, RTPpacketQueue rtPpacketQueue) throws IOException {

		if (dadosConnection.length > 1 && dadosConnection[0].equals("VIZINHO")) {
			dadosNodo.addVizinho(dadosConnection[1], new DadosVizinho(dadosConnection[1], dos, dis, socket));
			dadosNodo.addDestinoRota(dadosConnection[1]);

			ThreadOTTReceiver receiver = new ThreadOTTReceiver(false, ipAdress, dis, dadosNodo, rtPpacketQueue);
			ThreadOTTSender sender     = new ThreadOTTSender(socket, dos, dadosNodo.getVizinho(dadosConnection[1]).getMessagesToSend());

			receiver.start();
			sender.start();

			System.out.println(dadosNodo.toString());
		}
		else {
			dos.close();
			dis.close();
			socket.close();
		}
	}

	public static void main(String[] args) throws IOException{


		Map<String, DadosVizinho> vizinhos  = new HashMap<>();
		Set<String> destinosQueremVerStream = new TreeSet<>();
		Rota rotaFluxo                      = new Rota();
		querVerStream                       = false;

		DadosNodo dadosNodo = new DadosNodo(vizinhos, rotaFluxo, destinosQueremVerStream);

		DatagramSocket RTPsocket = new DatagramSocket(8888);
		PacketQueue queue        = new PacketQueue();
		RTPpacketQueue rtpQueue  = new RTPpacketQueue();

		Socket socketServidorInicial   = new Socket(args[0], 8080);

		DataOutputStream dosServidorInicial = new DataOutputStream(socketServidorInicial.getOutputStream());
		BufferedReader disServidorInicial   = new BufferedReader(new InputStreamReader(socketServidorInicial.getInputStream()));

		String ipAdress = InetAddress.getLocalHost().getHostAddress();
		byte[] data     = (ipAdress + "\n").getBytes();

		dosServidorInicial.write(data);
		dosServidorInicial.flush();

		String line;
		while ((line = disServidorInicial.readLine()) != null) {
			dadosNodo.addVizinho(line, null);
		}

		dosServidorInicial.close();
		disServidorInicial.close();
		socketServidorInicial.close();


		ThreadOTTPedidos threadOTTPedidos = new ThreadOTTPedidos(RTPsocket, rtpQueue, dadosNodo);
		threadOTTPedidos.start();

		ThreadOTTReceiverUDP receiverUDP   = new ThreadOTTReceiverUDP(RTPsocket, queue, rtpQueue, dadosNodo);
		ThreadOTTSenderUDP senderUDP = new ThreadOTTSenderUDP(RTPsocket, queue);
		receiverUDP.start();
		senderUDP.start();

		BeaconReceiver receiverBeacon = new BeaconReceiver(dadosNodo, ipAdress, false);
		BeaconSender senderBeacon = new BeaconSender(queue, ipAdress, dadosNodo);
		receiverBeacon.start();
		senderBeacon.start();


		// Tenta ligar a outros OTTs
		for (String vizinho : dadosNodo.getIpsVizinhos()) {
			try {
				estabeleConnectioVizinho(vizinho, ipAdress, rtpQueue, dadosNodo);
			}
			catch (UnknownHostException | ConnectException ignored) { }
		}

		// Se outros OTTs não estiverem ligados, ele fica à espera que se liguem a si
		ServerSocket ss = new ServerSocket(8080);

		while (true) {
			Socket socket        = ss.accept();
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String linha = dis.readLine();
			String[] dadosConnection = linha.split("-");

			estabeleConnectioVizinhoWaiting(dadosConnection, dos, dis, socket, ipAdress, dadosNodo, rtpQueue);

		}

    }

}
