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

		DataOutputStream dos = new DataOutputStream(socketServidorInicial.getOutputStream());
		BufferedReader dis   = new BufferedReader(new InputStreamReader(socketServidorInicial.getInputStream()));

		String ipAdress = InetAddress.getLocalHost().getHostAddress() + "\n";
		byte[] data = ipAdress.getBytes();
		dos.write(data);
		dos.flush();

		String line;
		while ((line = dis.readLine()) != null) {
			System.out.println("Adicionado vizinho: " + line);
			vizinhos.add(line);
		}

		dos.close();
		dis.close();
		socketServidorInicial.close();


		for (String vizinho : vizinhos) {
			Socket socket = new Socket(vizinho, 8080);

			ThreadOTTReceiver receiver = new ThreadOTTReceiver(dis, socket);
			ThreadOTTSender sender     = new ThreadOTTSender(socket, dos);
			receiver.start();
			sender.start();
		}

    }
}
