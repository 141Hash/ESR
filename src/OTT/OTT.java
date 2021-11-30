package OTT;

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
        ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
        Socket socket = ss.accept();
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        BufferedReader dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ThreadOTTSender sender = new ThreadOTTSender(socket, dos);
        ThreadOTTReceiver receiver = new ThreadOTTReceiver(dis, socket);

        sender.start();
        receiver.start();
    }
}
