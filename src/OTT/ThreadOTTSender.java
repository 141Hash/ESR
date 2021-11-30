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
        try
        {
        byte[] data = "Hello".getBytes();
        dos.write(data);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }


    }


}
