package OTT;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class ThreadSendControlMessage {
    private final DataOutputStream dos;

    public ThreadSendControlMessage(DataOutputStream dos) {
        this.dos = dos;
    }

    /*
        RouteControl#1#10.0.0.10
        RouteControl#2#10.0.0.10-10.0.0.1
    */

    public void run() {
        while (true) {
            try {
                String ipAdress = InetAddress.getLocalHost().getHostAddress() + "\n";
                byte[] data = ("RouteControl#1#" + ipAdress).getBytes();
                dos.write(data);
                dos.flush();
                Thread.sleep(10000);
            } catch (IOException | InterruptedException ignored) { }

        }
    }
}
