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

            while (!pedido.equals("EXIT")) {

                if (pedido.startsWith("PLAYER")) {
                    Thread threadCliente = new Thread(() -> { Cliente cli = new Cliente(ds, rtpQueue, vizinhos, rotaFluxo, destinosQueremVerStream); });
                    threadCliente.start();
                }

                System.out.println(pedido);
                pedido = reader.readLine();
            }

            for (String vizinho: this.vizinhos.keySet()) {
                if (this.vizinhos.get(vizinho) != null)
                    this.vizinhos.get(vizinho).addMessagesToSend("Leaving#" + InetAddress.getLocalHost().getHostAddress() + "\n");
            }

            OTT.EXIT = true;
            Thread.sleep(1000);

            for (String vizinho: this.vizinhos.keySet()) {
                if (this.vizinhos.get(vizinho) != null)
                    this.vizinhos.get(vizinho).getMessagesToSend().signalCon();
            }

            Runtime.getRuntime().halt(0);

        }
        catch (IOException | InterruptedException ignored) { }

    }

}
