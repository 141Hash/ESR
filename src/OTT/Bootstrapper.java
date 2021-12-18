package OTT;

//import org.json.simple.*;
//import org.json.simple.parser.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Bootstrapper {

    public static HashMap <String, Set<String>> readJSonFile () throws Exception {
        /*
        HashMap <String, Set<String>> topologiaRede = new HashMap<>();
        JSONArray jo = (JSONArray) new JSONParser().parse(new FileReader("NetworkFiles/rede1.json"));

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
         */
        return null;
    }

    public static HashMap <String, Set<String>> readTxtFile () throws Exception {

        HashMap <String, Set<String>> topologiaRede = new HashMap<>();

        File ficheiro          = new File("NetworkFiles/rede1.txt");

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

    public static void main(String[] args) throws Exception {

        HashMap <String, Set<String>> topologiaRede = readTxtFile();
        Topologia topologia = new Topologia(topologiaRede);;

        System.out.println(topologia.getTopologia().toString());

        ServerSocket ss = new ServerSocket(Integer.parseInt("8080"));

        while(true) {
            Socket socket = ss.accept();

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            BufferedReader dis   = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line = dis.readLine();
            //line = line.replaceAll("[\\n\\t ]", "");
            System.out.println(line);

            byte[] data = topologia.getVizinhos(line).toString().getBytes();
            dos.write(data);
            dos.flush();

            dos.close();
            dis.close();
            socket.close();
        }
    }
}
