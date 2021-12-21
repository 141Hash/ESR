package OTT;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ThreadOTTSenderUDP extends Thread {
    private DatagramSocket ds;
    private PacketQueue pq;

    public ThreadOTTSenderUDP(DatagramSocket ds, PacketQueue pq) {
        this.ds = ds;
        this.pq = pq;
    }

    public void run () {
        while (true) {
            try {
                DatagramPacket p = pq.remove();
                ds.send(p);
                System.out.println("Enviei um pacote");
            } catch (Exception ignored) {}
        }
    }
}
