package OTT;

import java.net.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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

    	Set<String> vizinhos = new TreeSet<>();

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
			vizinhos.add(line);
		}

		dosServidorInicial.close();
		disServidorInicial.close();
		socketServidorInicial.close();

		// Tenta ligar a outros OTTs
		for (String vizinho : vizinhos) {
			try {
				Socket socket = new Socket(vizinho, 8080);

				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				byte[] mensagemConnectionVizinho = ("VIZINHO$" + ipAdress).getBytes();
				dos.write(data);
				dos.flush();

				ThreadOTTReceiver receiver = new ThreadOTTReceiver(dis, socket);
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

			ThreadOTTReceiver receiver = new ThreadOTTReceiver(dis, socket);
			ThreadOTTSender sender     = new ThreadOTTSender(socket, dos);
			receiver.start();
			sender.start();
		}

    }
}
