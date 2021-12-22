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

	static boolean querVerStream;

	private static void estabeleConnectioVizinho(String vizinho, Map<String, DadosVizinho> vizinhos, String ipAdress, Rota rotaFluxo, DatagramSocket ds, PacketQueue pq, Set<String> destinosQueremVerStream) throws IOException {
		Socket socket = new Socket(vizinho, 8080);

		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		if (socket.getInetAddress().isReachable(1000)) {
			vizinhos.put(vizinho, new DadosVizinho(vizinho, dos, dis, socket));

			byte[] mensagemConnectionVizinho = ("VIZINHO-" + ipAdress + "\n").getBytes();
			dos.write(mensagemConnectionVizinho);
			dos.flush();

			ThreadOTTReceiver receiver = new ThreadOTTReceiver(false, ipAdress, dis, socket, vizinhos, rotaFluxo, ds, pq, destinosQueremVerStream);
			ThreadOTTSender sender     = new ThreadOTTSender(socket, dos, vizinhos.get(vizinho).getMessagesToSend());

			receiver.start();
			sender.start();
		}
	}

	private static void estabeleConnectioVizinhoWaiting(String[] dadosConnection, Map<String, DadosVizinho> vizinhos, DataOutputStream dos, BufferedReader dis, Socket socket, String ipAdress, Rota rotaFluxo, DatagramSocket ds, PacketQueue pq,  Set<String> destinosQueremVerStream) throws IOException {

		if (dadosConnection.length > 1 && dadosConnection[0].equals("VIZINHO")) {
			vizinhos.put(dadosConnection[1], new DadosVizinho(dadosConnection[1], dos, dis, socket));

			ThreadOTTReceiver receiver = new ThreadOTTReceiver(false, ipAdress, dis, socket, vizinhos, rotaFluxo, ds, pq, destinosQueremVerStream);
			ThreadOTTSender sender     = new ThreadOTTSender(socket, dos, vizinhos.get(dadosConnection[1]).getMessagesToSend());

			receiver.start();
			sender.start();

			System.out.println(vizinhos.toString());
		}
		else {
			dos.close();
			dis.close();
			socket.close();
		}
	}

	public static void main(String[] args) throws IOException{

		Map<String, DadosVizinho> vizinhos = new HashMap<>();
		Rota rotaFluxo = new Rota();

		Set<String> destinosQueremVerStream = new TreeSet<String>();
		querVerStream = false;

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
			vizinhos.put(line, null);
		}

		dosServidorInicial.close();
		disServidorInicial.close();
		socketServidorInicial.close();


		ThreadOTTPedidos threadOTTPedidos = new ThreadOTTPedidos(RTPsocket, rtpQueue, vizinhos, rotaFluxo);
		threadOTTPedidos.start();

		ThreadOTTReceiverUDP receiverUDP   = new ThreadOTTReceiverUDP(RTPsocket, queue, ipAdress, rotaFluxo, rtpQueue, destinosQueremVerStream);
		ThreadOTTSenderUDP senderUDP = new ThreadOTTSenderUDP(RTPsocket, queue);
		receiverUDP.start();
		senderUDP.start();


		// Tenta ligar a outros OTTs
		for (String vizinho : vizinhos.keySet()) {
			try {
				estabeleConnectioVizinho(vizinho, vizinhos, ipAdress, rotaFluxo, RTPsocket, queue, destinosQueremVerStream);
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
			//System.out.println(linha);
			String[] dadosConnection = linha.split("-");

			estabeleConnectioVizinhoWaiting(dadosConnection, vizinhos, dos, dis, socket, ipAdress, rotaFluxo, RTPsocket, queue, destinosQueremVerStream);

		}

    }

}
