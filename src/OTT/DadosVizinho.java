package OTT;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class DadosVizinho {
    private String ipVizinho;
    private DataOutputStream dos;
    private BufferedReader dis;
    private Socket socket;
    private QueueMensagens messagesToSend;

    public DadosVizinho (String ipVizinho, DataOutputStream dos, BufferedReader dis, Socket socket) {
        this.ipVizinho = ipVizinho;
        this.dos = dos;
        this.dis = dis;
        this.socket = socket;
        this.messagesToSend = new QueueMensagens();
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

    public QueueMensagens getMessagesToSend() {
        return messagesToSend;
    }
}
