package OTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class ThreadOTTPedidos extends Thread {

    private DatagramSocket ds;
    private RTPpacketQueue rtpQueue;
    private Map<String, DadosVizinho> vizinhos;
    private Rota rotaFluxo;

    public ThreadOTTPedidos(DatagramSocket ds, RTPpacketQueue rtpQueue, Map<String, DadosVizinho> vizinhos, Rota rotaFluxo) {
        this.ds = ds;
        this.rtpQueue = rtpQueue;
        this.vizinhos = vizinhos;
        this.rotaFluxo = rotaFluxo;
    }

    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String pedido = reader.readLine();

            while (!pedido.equals("exit")) {

                if (pedido.startsWith("get")) {
                    String videoFile = pedido.split(" ")[1];

                    Thread threadCliente = new Thread(() -> { Cliente cli = new Cliente(ds, rtpQueue, vizinhos, rotaFluxo, videoFile); });
                    threadCliente.start();

                }

                System.out.println(pedido);

                pedido = reader.readLine();
            }

        }
        catch (IOException ignored) { }

    }

}
