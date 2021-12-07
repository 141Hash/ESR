package OTT;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ThreadOTTSender extends Thread{
    private final Socket socket;
    private final DataOutputStream dos;

    public ThreadOTTSender(Socket socket, DataOutputStream dos){
        this.socket = socket;
        this.dos = dos;
    }

    public void run(){
	int number = 0;
        while (true) {
		try {
        		byte[] data = "Hello\n".getBytes();
        		dos.write(data);
			dos.flush();
			System.out.println("Sended " + number + "messages\n");
			number++;
			Thread.sleep(1000);
    		} catch (IOException e){
            		System.out.println(e.getMessage());
        	} catch (InterruptedException e){
            		System.out.println(e.getMessage());
        	}

	}
    }


}
