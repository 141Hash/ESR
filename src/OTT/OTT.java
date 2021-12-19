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
    private String nome;
    
    public OTT(String nome){
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

	public void setNome(String nome) {
		this.nome = nome;
	}

	public static void main(String[] args) throws IOException{

		Map<String, DadosVizinho> vizinhos = new HashMap<String, DadosVizinho>();

		Socket socketServidorInicial   = new Socket(args[0], 8080);

		DataOutputStream dosServidorInicial = new DataOutputStream(socketServidorInicial.getOutputStream());
		BufferedReader disServidorInicial   = new BufferedReader(new InputStreamReader(socketServidorInicial.getInputStream()));

		String ipAdress = InetAddress.getLocalHost().getHostAddress() + "\n";
		byte[] data = ipAdress.getBytes();
		dosServidorInicial.write(data);
		dosServidorInicial.flush();

		String line;
		while ((line = disServidorInicial.readLine()) != null) {
			System.out.println("Adicionado vizinho: " + line);
			vizinhos.put(line, null);
		}

		dosServidorInicial.close();
		disServidorInicial.close();
		socketServidorInicial.close();

		// Tenta ligar a outros OTTs
		for (String vizinho : vizinhos.keySet()) {
			try {
				Socket socket = new Socket(vizinho, 8080);

				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				vizinhos.put(vizinho, new DadosVizinho(vizinho, dos, dis, socket));

				byte[] mensagemConnectionVizinho = ("VIZINHO-" + ipAdress).getBytes();
				dos.write(mensagemConnectionVizinho);
				dos.flush();

				ThreadOTTReceiver receiver = new ThreadOTTReceiver(dis, socket, vizinhos);
				ThreadOTTSender sender = new ThreadOTTSender(socket, dos);
				receiver.start();
				sender.start();
			}
			catch (UnknownHostException | ConnectException ignored) { }
		}

		// Se outros OTTs não estiverem ligados, ele fica à espera que se liguem a si
		ServerSocket ss = new ServerSocket(8080);

		while (true) {
			Socket socket   = ss.accept();

			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String linha = dis.readLine();
			System.out.println(linha);
			String[] dadosConnection = linha.split("-");


			if (dadosConnection.length > 1 && dadosConnection[0].equals("VIZINHO")) {
				vizinhos.put(dadosConnection[1], new DadosVizinho(dadosConnection[1], dos, dis, socket));
				ThreadOTTReceiver receiver = new ThreadOTTReceiver(dis, socket, vizinhos);
				ThreadOTTSender sender = new ThreadOTTSender(socket, dos);
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

    }
}
