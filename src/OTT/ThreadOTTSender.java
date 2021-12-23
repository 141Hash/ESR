package OTT;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ThreadOTTSender extends Thread{
    private final Socket socket;
    private final DataOutputStream dos;
    private QueueMensagens messagesToSend;


    public ThreadOTTSender(Socket socket, DataOutputStream dos, QueueMensagens messagesToSend){
        this.socket = socket;
        this.dos = dos;
        this.messagesToSend = messagesToSend;
    }

    public void run () {
        while (!OTT.EXIT) {
			try {
				String message = messagesToSend.remove();

				if (!OTT.EXIT) {
                    byte[] data = message.getBytes();
                    dos.write(data);
                    dos.flush();
                }

			} catch (IOException | InterruptedException ignored){ }
		}
    }

}
