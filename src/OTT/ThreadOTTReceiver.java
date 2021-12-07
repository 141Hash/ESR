package OTT;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadOTTReceiver extends Thread{
    private String ipOTT;
    private int portaOTT;
    private BufferedReader dis;
    private Socket s;

    public ThreadOTTReceiver(BufferedReader dis, Socket s) throws IOException{
        this.dis = dis;
        this.s = s;
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
