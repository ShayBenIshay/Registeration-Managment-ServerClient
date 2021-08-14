

import java.net.*;
import java.util.*;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.File;
import java.io.FileNotFoundException;


public class reg_client{

    public static void main(String[] args) {
        reg_client r_c = new reg_client();
    }
    public reg_client() {
        Scanner sc = new Scanner(System.in);
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte buf[] = null;

        System.out.println("Enter the server IP address");
        String ip = sc.nextLine();
        System.out.println("Enter the server port number for course registration");
        int p = sc.nextInt();
        String server_response = "";
        String str = "";
        try{
//            sc = new Scanner(System.in);
            DatagramPacket dp_receive = null;
            byte[] receive;
            while(!str.equals("DONE")){
                receive = new byte[65535];
                System.out.println("Insert student ID and course number");
                str = sc.nextLine();
                while(str.equals("")) str = sc.nextLine();
                //send the server the registeration request
                buf = str.getBytes();
                DatagramPacket dp_send = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), p);
                ds.send(dp_send);
                if(str.equals("DONE")) break;
                dp_receive = new DatagramPacket(receive, receive.length);
                ds.receive(dp_receive);
                server_response = getStrinFromByte(receive);

                //print response in format: Student number <ID> successfully registered to course number <096250>
                System.out.println(server_response);
            }
        } catch (Exception e ) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    private String getStrinFromByte(byte[] receive) {
        return data(receive).toString();
    }
}