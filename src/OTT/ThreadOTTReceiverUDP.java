package OTT;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ThreadOTTReceiverUDP extends Thread {
    private DatagramSocket ds;
    private PacketQueue pq;
    private String ipAdress;
    private Rota rotaFluxo;
    private RTPpacketQueue rtpQueue;

    public ThreadOTTReceiverUDP(DatagramSocket ds, PacketQueue pq, String ipAdress, Rota rotaFluxo, RTPpacketQueue rtpQueue) {
        this.ds = ds;
        this.pq = pq;
        this.ipAdress = ipAdress;
        this.rotaFluxo = rotaFluxo;
        this.rtpQueue = rtpQueue;
    }

    public void recebePacketVideo (RTPpacket rtp_packet) throws UnknownHostException {

        String destination = rtp_packet.getDestinationIP();

        if (destination.equals(ipAdress)) {
            rtpQueue.add(rtp_packet);
        }
        else {
            InetAddress clientIPAddr = null;
            if (rotaFluxo.getDestinosVizinhos().containsKey(destination)) {
                clientIPAddr = InetAddress.getByName(destination);
            } else {
                for (String ipAdress : rotaFluxo.getDestinosVizinhos().keySet()) {
                    if (rotaFluxo.getDestinosVizinhos().get(ipAdress).contains(destination)) {
                        clientIPAddr = InetAddress.getByName(ipAdress);
                        break;
                    }
                }
            }

            int packet_length = rtp_packet.getlength();
            // Retrieve the packet bitstream and store it in an array of bytes
            byte[] packet_bits = new byte[packet_length];
            rtp_packet.getpacket(packet_bits);
            // Add the packet to the Packet Queue
            DatagramPacket dp = new DatagramPacket(packet_bits, packet_length, clientIPAddr, 8888);
            pq.add(dp);
        }

    }

    public void run() {
        while (true) {
            try {

                byte[] arr = new byte[Servidor.MAX_SIZE_PACKET + 10]; //Acrescentamos 10 bytes apenas para proteção neste momento
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

                RTPpacket rtp_packet = new RTPpacket(dp.getData(), dp.getLength());

                if (rtp_packet.getpayloadtype() == 26) {
                    recebePacketVideo(rtp_packet);
                } else if (rtp_packet.getpayloadtype() == 1) {
                    // Podemos colocar aqui os KeepAlive por exemplo (Beacons)
                }


            } catch (Exception ignored) {}
        }
    }
}
