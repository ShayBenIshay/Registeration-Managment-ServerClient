
import java.io.*;
import java.util.*;

import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;


public class data_client {

    public static void main(String[] args) {
        try {
            data_client d_c = new data_client();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public data_client() throws IOException {
        Scanner sc = new Scanner(System.in);
        Socket s;
        System.out.println("Enter the server IP address");
        String str = sc.nextLine();
        System.out.println("Enter the server port number for data requests");
        int p = sc.nextInt();
        try {
            s = new Socket(str, p);
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return;
        }
        String server_response = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        while (!server_response.equals("DONE")) {
            server_response = in.readLine();
            if (server_response.equals("DONE")) break;
            System.out.println(server_response);
        }
        s.close();

    }

}