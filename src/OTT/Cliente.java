package OTT;

/* ------------------
   Cliente
   usage: java Cliente
   adaptado dos originais pela equipa docente de ESR (nenhumas garantias)
   colocar o cliente primeiro a correr que o servidor dispara logo!
   ---------------------- */

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class Cliente {

    //GUI
    //----
    JFrame f = new JFrame("Cliente de Testes");
    JButton playButton = new JButton("Play");
    JButton pauseButton = new JButton("Pause");
    JButton tearButton = new JButton("Teardown");
    JPanel mainPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JLabel iconLabel = new JLabel();
    ImageIcon icon;


    //RTP variables:
    //----------------
    DatagramSocket RTPsocket; //socket to be used to send and receive UDP packet
    RTPpacketQueue rtPpacketQueue;

    Rota rotaFluxo;
    Map<String, DadosVizinho> vizinhos;
    Set<String> destinosQueremVerStream;

    Timer cTimer; //timer used to receive data from the UDP socket
    byte[] cBuf; //buffer used to store data received from the server
 
    //--------------------------
    //Constructor
    //--------------------------

    public Cliente(DatagramSocket ds, RTPpacketQueue rtpQueue, Map<String, DadosVizinho> vizinhos, Rota rotaFluxo, Set<String> destinosQueremVerStream) {
        //build GUI
        //--------------------------

        //Frame
        f.addWindowListener(new WindowAdapter() {
           public void windowClosing(WindowEvent e) {
               OTT.querVerStream = false;
               f.setVisible(false); //you can't see me!
               f.dispose();
               cTimer.stop();
           }
        });

        //Buttons
        buttonPanel.setLayout(new GridLayout(1,0));
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(tearButton);

        // handlers... (so dois)
        playButton.addActionListener(new playButtonListener(rotaFluxo, vizinhos));
        pauseButton.addActionListener(new pauseButtonListener(rotaFluxo, vizinhos, destinosQueremVerStream));
        tearButton.addActionListener(new tearButtonListener(vizinhos));

        //Image display label
        iconLabel.setIcon(null);

        //frame layout
        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(buttonPanel);
        iconLabel.setBounds(0,0,380,280);
        buttonPanel.setBounds(0,280,380,50);

        f.getContentPane().add(mainPanel, BorderLayout.CENTER);
        f.setSize(new Dimension(390,370));
        f.setVisible(true);

        //init para a parte do cliente
        //--------------------------
        cTimer = new Timer(20, new clientTimerListener());
        cTimer.setInitialDelay(0);
        cTimer.setCoalesce(true);
        cBuf = new byte[Servidor.MAX_SIZE_PACKET]; //allocate enough memory for the buffer used to receive data from the server

        try {
            // socket e video
            this.vizinhos = vizinhos;
            this.rotaFluxo = rotaFluxo;
            this.destinosQueremVerStream = destinosQueremVerStream;
            rtPpacketQueue = rtpQueue;
            RTPsocket = ds; //init RTP socket (o mesmo para o cliente e servidor)
            RTPsocket.setSoTimeout(5000); // setimeout to 5s
        } catch (SocketException e) {
            System.out.println("Cliente: erro no socket: " + e.getMessage());
        }
    }

    //------------------------------------
    //Handler for buttons
    //------------------------------------

    //Handler for Play button
    //-----------------------
    class playButtonListener implements ActionListener {

        Rota rotaFluxo;
        Map<String, DadosVizinho> vizinhos;

        public playButtonListener(Rota rotaFluxo, Map<String, DadosVizinho> vizinhos) {
            this.rotaFluxo = rotaFluxo;
            this.vizinhos = vizinhos;
        }

        public void actionPerformed(ActionEvent e){
            try {
                if (rotaFluxo.getOrigem() != null) {
                    this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend("GetVideo#" + InetAddress.getLocalHost().getHostAddress() + "\n");
                    OTT.querVerStream = true;

                    System.out.println("Play Button pressed !");
                    //start the timers ...
                    cTimer.start();
                } else {
                    System.out.println("Não se encontra ligado a nenhum Nodo de momento");
                }
            } catch (UnknownHostException unknownHostException) {
                unknownHostException.printStackTrace();
            }
        }

    }

    //Handler for Pause button
    //-----------------------
    class pauseButtonListener implements ActionListener {

        Rota rotaFluxo;
        Map<String, DadosVizinho> vizinhos;
        Set<String> destinosQueremVerStream;

        public pauseButtonListener(Rota rotaFluxo, Map<String, DadosVizinho> vizinhos, Set<String> destinosQueremVerStream) {
            this.rotaFluxo = rotaFluxo;
            this.vizinhos = vizinhos;
            this.destinosQueremVerStream = destinosQueremVerStream;
        }

        public void actionPerformed(ActionEvent e){

            OTT.querVerStream = false;

            try {
                if (this.destinosQueremVerStream.size() == 0 && this.rotaFluxo.getOrigem() != null) {
                    this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend("PauseVideo#" + InetAddress.getLocalHost().getHostAddress() + "\n");

                    System.out.println("Play Pause pressed !");
                    //start the timers ...
                    cTimer.stop();
                }
            } catch (UnknownHostException unknownHostException) {
                unknownHostException.printStackTrace();
            }
        }

    }

    //Handler for tear button
    //-----------------------
    class tearButtonListener implements ActionListener {

        Map<String, DadosVizinho> vizinhos;

        public tearButtonListener(Map<String, DadosVizinho> vizinhos) {
            this.vizinhos = vizinhos;
        }

        public void actionPerformed(ActionEvent e){

            try {
                for (String vizinho: this.vizinhos.keySet()) {
                    if (this.vizinhos.get(vizinho) != null)
                        this.vizinhos.get(vizinho).getMessagesToSend().addFirst("Leaving#" + InetAddress.getLocalHost().getHostAddress() + "\n");
                }

                Thread.sleep(1000);
                OTT.EXIT = true;

                for (String vizinho: this.vizinhos.keySet()) {
                    if (this.vizinhos.get(vizinho) != null)
                        this.vizinhos.get(vizinho).getMessagesToSend().signalCon();
                }

                cTimer.stop();
                Runtime.getRuntime().halt(0);

            } catch (InterruptedException | UnknownHostException ignored) { }
        }
    }

    //------------------------------------
    //Handler for timer (para cliente)
    //------------------------------------
  
    class clientTimerListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            try {
                if (OTT.querVerStream) {
                    //create an RTPpacket object from the DP
                    RTPpacket rtp_packet = rtPpacketQueue.remove();

                    if (rtp_packet.getpayloadtype() == 26) {
                        //print important header fields of the RTP packet received:
                        System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());

                        //print header bitstream:
                        rtp_packet.printheader();

                        //get the payload bitstream from the RTPpacket object
                        int payload_length = rtp_packet.getpayload_length();
                        byte [] payload = new byte[payload_length];
                        rtp_packet.getpayload(payload);

                        //get an Image object from the payload bitstream
                        Toolkit toolkit = Toolkit.getDefaultToolkit();
                        Image image = toolkit.createImage(payload, 0, payload_length);

                        //display the image as an ImageIcon object
                        icon = new ImageIcon(image);
                        iconLabel.setIcon(icon);
                    }
                    else if (rtp_packet.getpayloadtype() == 27) {
                        cTimer.stop();
                    }
                }
            }
            catch (InterruptedException ignored) { }
        }

    }

}
//end of Class Cliente

