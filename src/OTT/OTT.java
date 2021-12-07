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
	System.out.println("Ponto conectou-se com o IP: " + InetAddress.getLocalHost().getHostAddress() + "\n");
	

       	Socket socket;	
	if (args.length > 0) {
		System.out.println("IP: " + args[0]);
		socket = new Socket(args[0], 8080);
	} else {
		socket = ss.accept();
	}
        

	DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      
       	ThreadOTTReceiver receiver = new ThreadOTTReceiver(dis, socket);
	receiver.start();
	ThreadOTTSender sender = new ThreadOTTSender(socket, dos);
	sender.start();

	while(true) {
		Socket newSocket = ss.accept();

		// Repetir a parte de cima apenas dos outputs e inputs de modo a conseguirmos receber e enviar informação
	}

    }
}
