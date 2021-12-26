package OTT;
/* ------------------
   Servidor
   usage: java Servidor [Video file]
   adaptado dos originais pela equipa docente de ESR (nenhumas garantias)
   colocar primeiro o cliente a correr, porque este dispara logo
   ---------------------- */

import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.LineBorder;


public class Servidor extends JFrame implements ActionListener {

    //GUI:
    //----------------
    JLabel label;
    final JButton button = new JButton("ChangeVideo!");

    //RTP variables:
    //----------------
    DatagramPacket senddp; //UDP packet containing the video frames (to send)
    DatagramSocket RTPsocket; //socket to be used to send and receive UDP packet
    PacketQueue queue;
    DadosNodo dadosNodo;

    int RTP_dest_port = 8888; //destination port for RTP packets

    static ArrayList<String> VideoFileNames; //video file to request to the server

    //Video constants:
    //------------------
    int imagenb = 0; //image number of the image currently transmitted
    int counter = 0; //contador do vídeo em que estámos
    VideoStream video; //VideoStream object used to access video frames
    static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
    static int FRAME_PERIOD = 34; //Frame period of the video to stream, in ms
    static int VIDEO_LENGTH = 500; //length of the video in frames

    Timer sTimer; //timer used to send the images at the video frame rate
    byte[] sBuf; //buffer used to store the images to send to the client

    static final int MAX_SIZE_PACKET = 15000;

    //--------------------------
    //Constructor
    //--------------------------
    public Servidor(DatagramSocket ds, PacketQueue pq, ArrayList<String> videoFileNames, DadosNodo dadosNodo) {
        //init Frame
        super("Servidor");

        // init para a parte do servidor
        sTimer = new Timer(FRAME_PERIOD, this); //init Timer para servidor
        sTimer.setInitialDelay(0);
        sTimer.setCoalesce(true);
        sBuf = new byte[MAX_SIZE_PACKET]; //allocate memory for the sending buffer

        try {
            RTPsocket      = ds; // RTP socket
            queue          = pq; // PacketQueue
            this.dadosNodo = dadosNodo;

            VideoFileNames = videoFileNames; // Video name
            video         = new VideoStream(VideoFileNames.get(counter)); //init the VideoStream object:

            System.out.println("Servidor: vai enviar video da file " + videoFileNames.get(counter));

        } catch (SocketException e) {
            System.out.println("Servidor: erro no socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Servidor: erro no video: " + e.getMessage());
        }

        //Handler to close the main window
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    //stop the timer and exit
                    label.setVisible(false); //you can't see me!
                    sTimer.stop();
                }
            }
        );

        button.addActionListener(new buttonListener());

        //GUI:
        label = new JLabel("Send frame #        ", JLabel.LEFT);
        getContentPane().add(label, BorderLayout.CENTER);
        label.setPreferredSize(new Dimension(400, 50));
        label.setBorder(new LineBorder(Color.GRAY, 1));
        label.setLayout(new BorderLayout());
        label.add(button, BorderLayout.EAST);

        sTimer.start();
    }

    //------------------------------------
    //Handler for buttons
    //------------------------------------

    //Handler for Change Video button
    //-----------------------
    class buttonListener implements ActionListener {

        public void actionPerformed (ActionEvent e) {
            try {

                int counterInicial = counter;
                counter++;

                if (counter >= VideoFileNames.size()) {
                    counter = 0;
                }

                if (counter != counterInicial) {
                    imagenb = 0;

                    sTimer = new Timer(FRAME_PERIOD, this);
                    sTimer.setInitialDelay(0);
                    sTimer.setCoalesce(true);

                    video = new VideoStream(VideoFileNames.get(counter));

                    sTimer.start();
                }

            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

        }

    }


    //------------------------
    //Handler for timer
    //------------------------
    public void actionPerformed (ActionEvent e) {

        try {
            //if the current image nb is less than the length of the video
            if (imagenb < VIDEO_LENGTH) {
                //update current imagenb
                imagenb++;

                //get next frame to send from the video, as well as its size
                int image_length = video.getnextframe(sBuf);

                //Builds an RTPpacket object containing the frame
                RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb, imagenb*FRAME_PERIOD, sBuf, image_length);

                //get to total length of the full rtp packet to send
                int packet_length = rtp_packet.getlength();

                //retrieve the packet bitstream and store it in an array of bytes
                byte[] packet_bits = new byte[packet_length];
                rtp_packet.getpacket(packet_bits);

                //send the packet as a DatagramPacket over the UDP socket

                if (dadosNodo.getDestinosQueremVerStream().size() > 0) {
                    for (String vizinho : dadosNodo.getDestinosQueremVerStream()) {
                        senddp = new DatagramPacket(packet_bits, packet_length, InetAddress.getByName(vizinho), RTP_dest_port);
                        queue.add(senddp);
                    }
                }

                //System.out.println("Send frame #"+imagenb);
                //print the header bitstream
                //rtp_packet.printheader();

                //update GUI
                label.setText("Send frame #" + imagenb);


            } else {

                imagenb = 0;
                //if we have reached the end of the video file, stop the timer
                sTimer.stop();

                sTimer = new Timer(FRAME_PERIOD, this); //init Timer para servidor
                sTimer.setInitialDelay(0);
                sTimer.setCoalesce(true);

                video = new VideoStream(VideoFileNames.get(counter));

                sTimer.start();
            }
        } catch (Exception ex) {
            System.out.println("Exception caught: "+ex);
            System.exit(0);
        }
    }

}//end of Class Servidor
