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
                p.getAddress().getHostAddress();
                ds.send(p);
                System.out.println("Beacon enviado para " + p.getAddress().getHostAddress()  + "-> Pacote enviado");
            } catch (Exception ignored) {}
        }
    }
}
