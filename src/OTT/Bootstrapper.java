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
        JSONArray jo = (JSONArray) new JSONParser().parse(new FileReader("../NetworkFiles/rede3.json"));

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

    public static void estabeleConnectioVizinho(DataOutputStream dos, BufferedReader dis, Socket socket, String[] dadosConnection, String ipAdress, DadosNodo dadosNodo, RTPpacketQueue rtpQueue) {

        dadosNodo.addDestinoRota(dadosConnection[1]);
        dadosNodo.addVizinho(dadosConnection[1], new DadosVizinho(dadosConnection[1], dos, dis, socket));

        ThreadOTTReceiver receiver         = new ThreadOTTReceiver(true, ipAdress, dis, dadosNodo, rtpQueue);
        ThreadOTTSender sender             = new ThreadOTTSender(socket, dos, dadosNodo.getVizinho(dadosConnection[1]).getMessagesToSend());
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

    private static void iniciaServidorStreaming(DatagramSocket ds, PacketQueue pq, DadosNodo dadosNodo) {

        File dirName = new File("../MovieFiles/");
        File[] fileList = dirName.listFiles();

        if (dirName.exists() && fileList != null) {
            if (fileList.length > 0) {
                ArrayList<String> videoFaleNames = new ArrayList<>();

                for (File file: fileList) {
                    System.out.println("\t" + file.toString());
                    videoFaleNames.add(file.toString());
                }

                //Create a Main object
                Thread threadServidor = new Thread(() -> { Servidor s = new Servidor(ds, pq, videoFaleNames, dadosNodo);
                    //show GUI: (opcional!)
                    s.pack();
                    s.setVisible(true);
                });
                threadServidor.start();
            }
            else {
                System.out.println("No videos to watch");
            }
        }
        else {
            System.out.println("Ficheiros de videos não encontrados");
        }

    }

    public static void main(String[] args) throws Exception {

        DatagramSocket RTPsocket = new DatagramSocket(8888);
        PacketQueue queue        = new PacketQueue();
        RTPpacketQueue rtpQueue  = new RTPpacketQueue();

        Map<String, DadosVizinho> vizinhos  = new HashMap<>();
        Set<String> destinosQueremVerStream = new TreeSet<>();
        Rota rotaFluxo                      = new Rota();

        DadosNodo dadosNodo = new DadosNodo(vizinhos, rotaFluxo, destinosQueremVerStream);

        String ipAdress = InetAddress.getLocalHost().getHostAddress();

        iniciaServidorStreaming(RTPsocket, queue, dadosNodo);

        HashMap <String, Set<String>> topologiaRede = readJSonFile();
        Topologia topologia  = new Topologia(topologiaRede);

        ThreadOTTReceiverUDP receiverUDP   = new ThreadOTTReceiverUDP(RTPsocket, queue, rtpQueue, dadosNodo);
        ThreadOTTSenderUDP senderUDP = new ThreadOTTSenderUDP(RTPsocket, queue);
        receiverUDP.start();
        senderUDP.start();


        BeaconReceiver receiverBeacon = new BeaconReceiver(dadosNodo, ipAdress, true, rtpQueue);
        BeaconSender senderBeacon = new BeaconSender(queue, ipAdress, dadosNodo);
        receiverBeacon.start();
        senderBeacon.start();

        ServerSocket ss = new ServerSocket(Integer.parseInt("8080"));

        while(true) {
            Socket socket        = ss.accept();
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line              = dis.readLine();
            String[] dadosConnection = line.split("-");

            if (dadosConnection.length > 1 && dadosConnection[0].equals("VIZINHO")) {
                estabeleConnectioVizinho(dos, dis, socket, dadosConnection, ipAdress, dadosNodo, rtpQueue);
            } else {
                estabeleConnectioInicial(topologia, dos, dis, socket, line);
            }

            Thread.sleep(500);
            dadosNodo.addMessagesToAll("RouteControl#1#" + ipAdress + "\n");
        }

    }

}
