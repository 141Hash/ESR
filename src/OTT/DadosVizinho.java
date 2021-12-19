package OTT;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class DadosVizinho {
    private String ipVizinho;
    private DataOutputStream dos;
    private BufferedReader dis;
    private Socket socket;
    private LinkedList<String> messagesToSend;

    public DadosVizinho (String ipVizinho, DataOutputStream dos, BufferedReader dis, Socket socket) {
        this.ipVizinho = ipVizinho;
        this.dos = dos;
        this.dis = dis;
        this.socket = socket;
        this.messagesToSend = new LinkedList<>();
    }

    public String getIpVizinho() {
        return ipVizinho;
    }

    public DataOutputStream getDos() {
        return dos;
    }

    public BufferedReader getDis() {
        return dis;
    }

    public Socket getSocket() {
        return socket;
    }

    public LinkedList<String> getMessagesToSend() {
        return messagesToSend;
    }

    public void addMessageToQueue (String message) {
        this.messagesToSend.add(message);
    }
}
