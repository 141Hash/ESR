package OTT;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ThreadOTTReceiverUDP extends Thread {
    private DatagramSocket ds;
    private PacketQueue pq;

    public ThreadOTTReceiverUDP(DatagramSocket ds, PacketQueue pq) {
        this.ds = ds;
        this.pq = pq;
    }

    public void run() {
        while (true) {
            try {
                byte[] arr = new byte[Servidor.MAX_SIZE_PACKET + 10]; //Acrescentamos 10 bytes apenas para proteção neste momento
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

                byte[] conteudoPacote = new byte[dp.getLength()];
                System.arraycopy(dp.getData(), 0, conteudoPacote, 0, dp.getLength());

                System.out.println(conteudoPacote.length);

            } catch (Exception ignored) {}
        }
    }
}
