package OTT;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class ThreadOTTReceiverUDP extends Thread {
    private DatagramSocket ds;
    private PacketQueue pq;
    private RTPpacketQueue rtpQueue;
    private DadosNodo dadosNodo;

    public ThreadOTTReceiverUDP(DatagramSocket ds, PacketQueue pq, RTPpacketQueue rtpQueue, DadosNodo dadosNodo) {
        this.ds = ds;
        this.pq = pq;
        this.rtpQueue = rtpQueue;
        this.dadosNodo = dadosNodo;
    }

    public void recebePacketVideo (RTPpacket rtp_packet) throws UnknownHostException {

        if (OTT.querVerStream) {
            rtpQueue.add(rtp_packet);
        }

        int packet_length = rtp_packet.getlength();
        // Retrieve the packet bitstream and store it in an array of bytes
        byte[] packet_bits = new byte[packet_length];
        rtp_packet.getpacket(packet_bits);

        for (String destino : this.dadosNodo.getDestinosQueremVerStream()) {
            InetAddress clientIPAddr = InetAddress.getByName(destino);

            DatagramPacket dp = new DatagramPacket(packet_bits, packet_length, clientIPAddr, 8888);
            pq.add(dp);
        }

    }

    private void atualizaValorBeacon(RTPpacket rtp_packet) {
        byte[] payload = new byte[rtp_packet.getpayload_length()];
        rtp_packet.getpayload(payload);

        String ipVizinho = new String(payload, StandardCharsets.UTF_8);

        if (this.dadosNodo.getVizinho(ipVizinho) != null)
            this.dadosNodo.updateTime(ipVizinho);
    }

    public void run() {
        while (true) {
            try {

                byte[] arr = new byte[Servidor.MAX_SIZE_PACKET + 10]; //Acrescentamos 10 bytes apenas para proteção neste momento
                DatagramPacket dp = new DatagramPacket(arr, arr.length);
                ds.receive(dp);

                RTPpacket rtp_packet = new RTPpacket(dp.getData(), dp.getLength());

                int payloadType = rtp_packet.getpayloadtype();

                switch (payloadType) {
                    case 26:
                        recebePacketVideo(rtp_packet);
                    case 1:
                        atualizaValorBeacon(rtp_packet);
                }

            } catch (Exception ignored) {}
        }
    }


}
