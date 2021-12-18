package OTT;

import java.net.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class OTT {
    private String nome;
    
    public OTT(String nome){
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public static void main(String[] args) throws IOException{
        
		ServerSocket ss = new ServerSocket(Integer.parseInt("8080"));

		Socket socket   = new Socket(args[0], 8080);

		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));


		String ipAdress = InetAddress.getLocalHost().getHostAddress() + "\n";
		byte[] data = ipAdress.getBytes();
		dos.write(data);
		dos.flush();

		String line;
		while ((line = dis.readLine()) != null) {
			System.out.println(line);
		}

		dos.close();
		dis.close();
		socket.close();

		/*
		ThreadOTTReceiver receiver = new ThreadOTTReceiver(dis, socket);
		ThreadOTTSender sender     = new ThreadOTTSender(socket, dos);
		receiver.start();
		sender.start();
		 */

		while(true) {
			//Socket newSocket = ss.accept();
		}

    }
}
