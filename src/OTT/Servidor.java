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
import javax.swing.*;
import javax.swing.Timer;



public class Servidor extends JFrame implements ActionListener {

    //GUI:
    //----------------
    JLabel label;

    //RTP variables:
    //----------------
    DatagramPacket senddp; //UDP packet containing the video frames (to send)
    DatagramSocket RTPsocket; //socket to be used to send and receive UDP packet
    PacketQueue queue;
    Rota rotafluxo;

    int RTP_dest_port = 8888; //destination port for RTP packets

    static String VideoFileName; //video file to request to the server

    //Video constants:
    //------------------
    int imagenb = 0; //image number of the image currently transmitted
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
    public Servidor(DatagramSocket ds, PacketQueue pq, Rota rotaFluxo, String videoFileName) {
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
            this.rotafluxo = rotaFluxo;

            VideoFileName = videoFileName; // Video name
            video         = new VideoStream(VideoFileName); //init the VideoStream object:

            System.out.println("Servidor: vai enviar video da file " + VideoFileName);
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

        //GUI:
        label = new JLabel("Send frame #        ", JLabel.CENTER);
        getContentPane().add(label, BorderLayout.CENTER);

        sTimer.start();
    }


    //------------------------
    //Handler for timer
    //------------------------
    public void actionPerformed(ActionEvent e) {

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
                    for (String vizinho : this.rotafluxo.getDestinosVizinhos().keySet()) {
                        senddp = new DatagramPacket(packet_bits, packet_length, InetAddress.getByName(vizinho), RTP_dest_port);
                        queue.add(senddp);
                    }

                    System.out.println("Send frame #"+imagenb);
                    //print the header bitstream
                    rtp_packet.printheader();

                    //update GUI
                    label.setText("Send frame #" + imagenb);


            } else {
                RTPpacket rtp_packet = new RTPpacket(27, imagenb, imagenb * FRAME_PERIOD, new byte[0], 0);

                int packet_length = rtp_packet.getlength();

                //retrieve the packet bitstream and store it in an array of bytes
                byte[] packet_bits = new byte[packet_length];
                rtp_packet.getpacket(packet_bits);

                //send the packet as a DatagramPacket over the UDP socket
                for (String vizinho : this.rotafluxo.getDestinosVizinhos().keySet()) {
                    senddp = new DatagramPacket(packet_bits, packet_length, InetAddress.getByName(vizinho), RTP_dest_port);
                    queue.add(senddp);
                }

                imagenb = 0;
                //if we have reached the end of the video file, stop the timer
                //sTimer.stop();
            }
        } catch(Exception ex) {
            System.out.println("Exception caught: "+ex);
            System.exit(0);
        }
    }

}//end of Class Servidor
