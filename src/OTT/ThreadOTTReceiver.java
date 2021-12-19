package OTT;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class ThreadOTTReceiver extends Thread{
    private String ipOTT;
    private int portaOTT;
    private BufferedReader dis;
    private Socket s;
    private Map<String, DadosVizinho> vizinhos;

    public ThreadOTTReceiver(BufferedReader dis, Socket s, Map<String, DadosVizinho> vizinhos) throws IOException{
        this.dis = dis;
        this.s = s;
        this.vizinhos = vizinhos;
    }

    public void run(){
	    String line;
    	try {       
   		    while ((line = dis.readLine()) != null) {
			    System.out.println(line);
		    }
	    } catch(IOException e){
        	System.out.println(e.getMessage());
        }
    }

}
