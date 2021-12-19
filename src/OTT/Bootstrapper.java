package OTT;

//import org.json.simple.*;
//import org.json.simple.parser.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Bootstrapper {

    public static HashMap <String, Set<String>> readJSonFile () throws Exception {

        HashMap <String, Set<String>> topologiaRede = new HashMap<>();
        JSONArray jo = (JSONArray) new JSONParser().parse(new FileReader("../NetworkFiles/rede1.json"));

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

        File ficheiro          = new File("../NetworkFiles/rede1.txt");

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

    /*
        Lista dos vizinhos
            - Ip Vzinho
            - Dos vizinhos
            - Dis vizinho
            - Socket
            - Queue de cenas para mandar
        Lista de fowards
     */

    public static void main(String[] args) throws Exception {

        Map<String, DadosVizinho> vizinhos = new HashMap<String, DadosVizinho>();
        Rota rotaFluxo = new Rota();

        HashMap <String, Set<String>> topologiaRede = readJSonFile();
        Topologia topologia = new Topologia(topologiaRede);

        String ipAdress = InetAddress.getLocalHost().getHostAddress();

        System.out.println(topologia.getTopologia().toString());

        ServerSocket ss = new ServerSocket(Integer.parseInt("8080"));

        while(true) {
            Socket socket = ss.accept();

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line = dis.readLine();
            System.out.println(line);

            String[] dadosConnection = line.split("-");
            if (dadosConnection.length > 1 && dadosConnection[0].equals("VIZINHO")) {
                vizinhos.put(dadosConnection[1], new DadosVizinho(dadosConnection[1], dos, dis, socket));

                ThreadOTTReceiver receiver = new ThreadOTTReceiver(ipAdress, dis, socket, vizinhos, rotaFluxo);
                ThreadOTTSender sender = new ThreadOTTSender(socket, dos, vizinhos.get(dadosConnection[1]).getMessagesToSend());
                ThreadSendControlMessage controler = new ThreadSendControlMessage(dos);

                receiver.start();
                sender.start();
                controler.start();
            }
            else {
                if (topologia.getTopologia().containsKey(line)) {
                    for (String vizinho : topologia.getVizinhos(line)) {
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

        }

        /*
        Minha ideia para verificar a rota é servidor ir enviando pings/floods que vão crescendo e incrementando em cada nodo.
        Deixar também informação sobre os nodos em que já passou para só fazer fowards e nunca enviar para trás uma mensagem!
        Difícil de perceber, mas raciocínio terá de ser mais ao menos esse
         */
    }
}
