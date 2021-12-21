package OTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Map;

public class ThreadOTTPedidos extends Thread {

    private Map<String, DadosVizinho> vizinhos;
    private Rota rotaFluxo;

    public ThreadOTTPedidos(Map<String, DadosVizinho> vizinhos, Rota rotaFluxo) {
        this.vizinhos = vizinhos;
        this.rotaFluxo = rotaFluxo;
    }

    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String pedido = reader.readLine();

            while (!pedido.equals("exit")) {

                if (pedido.equals("get")) {
                    this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend("GetVideo##"+ InetAddress.getLocalHost().getHostAddress() + "\n");
                }

                System.out.println(pedido);

                pedido = reader.readLine();
            }

        }
        catch (IOException ignored) { }

    }

}
