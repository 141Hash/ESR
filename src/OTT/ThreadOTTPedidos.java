package OTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ThreadOTTPedidos extends Thread {

    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String pedido = reader.readLine();

            while (!pedido.equals("exit")) {
                pedido = reader.readLine();
                System.out.println(pedido);
            }

        }
        catch (IOException ignored) { }

    }

}
