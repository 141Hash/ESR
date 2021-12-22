package OTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

public class ThreadOTTPedidos extends Thread {

    private DatagramSocket ds;
    private RTPpacketQueue rtpQueue;
    private Map<String, DadosVizinho> vizinhos;
    private Rota rotaFluxo;
    private Set<String> destinosQueremVerStream;

    public ThreadOTTPedidos(DatagramSocket ds, RTPpacketQueue rtpQueue, Map<String, DadosVizinho> vizinhos, Rota rotaFluxo, Set<String> destinosQueremVerStream) {
        this.ds = ds;
        this.rtpQueue = rtpQueue;
        this.vizinhos = vizinhos;
        this.rotaFluxo = rotaFluxo;
        this.destinosQueremVerStream = destinosQueremVerStream;
    }

    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String pedido = reader.readLine();

            while (!pedido.equals("exit")) {

                if (pedido.startsWith("PLAYER")) {
                    String videoFile;

                    String[] pedidoArray = pedido.split(" ");
                    if (pedidoArray.length == 2) {
                        videoFile = pedidoArray[1];
                    } else {
                        videoFile = "";
                    }

                    Thread threadCliente = new Thread(() -> { Cliente cli = new Cliente(ds, rtpQueue, vizinhos, rotaFluxo, videoFile, destinosQueremVerStream); });
                    threadCliente.start();

                }

                System.out.println(pedido);

                pedido = reader.readLine();
            }

        }
        catch (IOException ignored) { }

    }

}
