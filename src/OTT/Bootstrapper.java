package OTT;

import org.json.simple.*;
import org.json.simple.parser.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Bootstrapper {

    public static HashMap <String, Set<String>> readJSonFile () throws Exception {
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
    }

    public static void main(String[] args) throws Exception {

        HashMap <String, Set<String>> topologiaRede = readJSonFile();
        Topologia topologia = new Topologia(topologiaRede);;

        //System.out.println(topologia.getTopologia().toString());

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
