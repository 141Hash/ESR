package OTT;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Bootstrapper {

    public static HashMap <String, Set<String>> readJSonFile () throws Exception {

        HashMap <String, Set<String>> topologiaRede = new HashMap<>();
        JSONArray jo = (JSONArray) new JSONParser().parse(new FileReader("../NetworkFiles/rede2.json"));

        for (Object nodo : jo) {
            JSONObject nodoAtual = (JSONObject) nodo;

            String nomeNodo = (String) nodoAtual.get("nome");
            System.out.println(nomeNodo);

            String ipNodo = (String) nodoAtual.get("IP");
            System.out.println(ipNodo);
            topologiaRede.put(ipNodo,new TreeSet<String>());

            JSONArray vizinhos = (JSONArray) nodoAtual.get("vizinhos");
            for (Object vizinho : vizinhos) {
                JSONObject vizinhoAtual = (JSONObject) vizinho;
                String ipVizinho = (String) vizinhoAtual.get("IP");
                topologiaRede.get(ipNodo).add(ipVizinho);
            }

        }

        return topologiaRede;
    }

    public static HashMap <String, Set<String>> readTxtFile () throws Exception {

        HashMap <String, Set<String>> topologiaRede = new HashMap<>();

        File ficheiro = new File("../NetworkFiles/rede1.txt");

        if (ficheiro.exists()) {
            BufferedReader reaader = new BufferedReader(new FileReader(ficheiro));

            String linha;
            while ((linha = reaader.readLine()) != null) {
                String[] dados = linha.split(";");

                String nomeNodo = (String) dados[0];
                System.out.println(nomeNodo);

                String ipNodo = (String) dados[1];
                System.out.println(ipNodo);
                topologiaRede.put(ipNodo,new TreeSet<String>());

                String daodsVizinhos = (String) dados[2].replaceAll("[{}]", "");
                String[] vizinhos = daodsVizinhos.split("/");
                for (String vizinho : vizinhos) {
                    String ipVizinho = (String) vizinho.split(",")[1];
                    topologiaRede.get(ipNodo).add(ipVizinho);
                }
            }
        }

        return topologiaRede;
    }

    public static void estabeleConnectioVizinho(Map<String, DadosVizinho> vizinhos, DataOutputStream dos, BufferedReader dis, Socket socket, Rota rotaFluxo, String[] dadosConnection, String ipAdress, DatagramSocket ds, PacketQueue pq, Set<String> destinosQueremVerStream) {

        rotaFluxo.addDestinoVizinho(dadosConnection[1]);
        vizinhos.put(dadosConnection[1], new DadosVizinho(dadosConnection[1], dos, dis, socket));

        ThreadOTTReceiver receiver         = new ThreadOTTReceiver(true, ipAdress, dis, socket, vizinhos, rotaFluxo, ds, pq, destinosQueremVerStream);
        ThreadOTTSender sender             = new ThreadOTTSender(socket, dos, vizinhos.get(dadosConnection[1]).getMessagesToSend());
        ThreadSendControlMessage controler = new ThreadSendControlMessage(dos);

        receiver.start();
        sender.start();
        controler.start();

    }

    public static void estabeleConnectioInicial(Topologia topologia, DataOutputStream dos, BufferedReader dis, Socket socket, String linha) throws IOException {

        if (topologia.getTopologia().containsKey(linha)) {
            for (String vizinho : topologia.getVizinhos(linha)) {
                byte[] data = (vizinho + "\n").getBytes();
                dos.write(data);
                dos.flush();
            }
        } else {
            byte[] data = "Nodo não registado na Topologia\n".getBytes();
            dos.write(data);
            dos.flush();
        }

        dos.close();
        dis.close();
        socket.close();

    }

    private static void iniciaServidorStreaming(DatagramSocket ds, PacketQueue pq, Rota rotaFluxo) {

        String videoFileName = "../MovieFiles/movie.Mjpeg";
        /*
        if (!mensagemControlo[1].equals("")) {
            videoFileName = mensagemControlo[1];
            System.out.println("Servidor: VideoFileName indicado como parametro: " + videoFileName);
        } else  {
            videoFileName = "../MovieFiles/movie.Mjpeg";
            System.out.println("Servidor: parametro não foi indicado. VideoFileName = " + videoFileName);
        }

        InetAddress clientIPAddr = null;

        if (rotaFluxo.getDestinosVizinhos().containsKey(mensagemControlo[2])) {
            clientIPAddr = InetAddress.getByName(mensagemControlo[2]);
        }
        else {
            for (String ipAdress : rotaFluxo.getDestinosVizinhos().keySet()) {
                if (rotaFluxo.getDestinosVizinhos().get(ipAdress).contains(mensagemControlo[2])) {
                    clientIPAddr = InetAddress.getByName(ipAdress);
                    break;
                }
            }
        }
        */

        File f = new File(videoFileName);

        if (f.exists()) {
            //Create a Main object
            Servidor s = new Servidor(ds, pq, rotaFluxo, videoFileName);

            //show GUI: (opcional!)
            s.pack();
            s.setVisible(true);
        }
        else
            System.out.println("Ficheiro de video não existe: " + videoFileName);

    }

    public static void main(String[] args) throws Exception {

        DatagramSocket RTPsocket = new DatagramSocket(8888);
        PacketQueue queue        = new PacketQueue();

        Map<String, DadosVizinho> vizinhos = new HashMap<>();
        Rota rotaFluxo = new Rota();

        Set<String> destinosQueremVerStream = new TreeSet<String>();
        iniciaServidorStreaming(RTPsocket, queue, rotaFluxo);

        HashMap <String, Set<String>> topologiaRede = readJSonFile();
        Topologia topologia  = new Topologia(topologiaRede);

        ThreadOTTSenderUDP senderUDP = new ThreadOTTSenderUDP(RTPsocket, queue);
        senderUDP.start();

        String ipAdress = InetAddress.getLocalHost().getHostAddress();
        ServerSocket ss = new ServerSocket(Integer.parseInt("8080"));

        while(true) {
            Socket socket        = ss.accept();
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line              = dis.readLine();
            String[] dadosConnection = line.split("-");

            if (dadosConnection.length > 1 && dadosConnection[0].equals("VIZINHO")) {
                estabeleConnectioVizinho(vizinhos, dos, dis, socket, rotaFluxo, dadosConnection, ipAdress, RTPsocket, queue, destinosQueremVerStream);
            } else {
                estabeleConnectioInicial(topologia, dos, dis, socket, line);
            }
        }

    }

}
