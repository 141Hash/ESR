package OTT;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class BeaconSender extends Thread {

    private PacketQueue packetQueue;
    private String ipAdress;
    private DadosNodo dadosNodo;
    private final int SLEEP_TIME = 5000;

    public BeaconSender(PacketQueue packetQueue, String ipAdress, DadosNodo dadosNodo) {
        this.packetQueue = packetQueue;
        this.ipAdress = ipAdress;
        this.dadosNodo = dadosNodo;
    }

    public void run() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException ignored) { }

        while(!OTT.EXIT) {
            try {

                byte[] data  = ipAdress.getBytes(StandardCharsets.UTF_8);
                RTPpacket rtp_packet = new RTPpacket(1, 0, 0, data, data.length);

                int packet_length = rtp_packet.getlength();
                byte[] packet_bits = new byte[packet_length];
                rtp_packet.getpacket(packet_bits);

                for (String vizinho : this.dadosNodo.getIpsVizinhos()) {
                    if (this.dadosNodo.getVizinho(vizinho) != null) {
                        DatagramPacket dp = new DatagramPacket(packet_bits, packet_length, InetAddress.getByName(vizinho), 8888);
                        packetQueue.addFirst(dp);
                    }
                }

                Thread.sleep(SLEEP_TIME);

            } catch (InterruptedException | UnknownHostException e) {}
        }
    }
}
