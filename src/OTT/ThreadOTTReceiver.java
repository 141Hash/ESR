package OTT;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ThreadOTTReceiver extends Thread{
    private String ipOTT;
    private BufferedReader dis;
    private Socket s;
    private Map<String, DadosVizinho> vizinhos;

    public ThreadOTTReceiver(String ipOTT, BufferedReader dis, Socket s, Map<String, DadosVizinho> vizinhos) throws IOException {
        this.ipOTT = ipOTT;
        this.dis = dis;
        this.s = s;
        this.vizinhos = vizinhos;
    }

    public void run(){
	    String line;
    	try {       
   		    while ((line = dis.readLine()) != null) {

                String[] mensagemControlo = line.split("#");
   		        if (mensagemControlo.length > 2 && mensagemControlo[0].equals("RouteControl")) {
   		            int nrSaltos =  Integer. parseInt(mensagemControlo[1]);
   		            nrSaltos++;

                    Set<String> historico = new HashSet<>(Arrays.asList(mensagemControlo[2].split("-")));

                    for (String vizinho : this.vizinhos.keySet()) {
                        if (!historico.contains(vizinho) && this.vizinhos.get(vizinho) != null) {
                            String nextMessage = mensagemControlo[0] + "#" + nrSaltos + "#" + mensagemControlo[2] + "-" + ipOTT + "\n";
                            this.vizinhos.get(vizinho).getMessagesToSend().add(nextMessage);
                        }
                    }
                }
   		        System.out.println(line);
		    }
	    } catch(IOException e){
        	System.out.println(e.getMessage());
        }
    }

}
