
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


public class server {
    DatagramSocket dg_reg_socket;
    ServerSocket server_data_socket;
    int registeration_port;
    int data_request_port;
    ReentrantLock re = new ReentrantLock();
    Map<String, Course> courses = new HashMap<String, Course>();

    public static void main(String[] args) {
        server serv = null;
        try {
            serv = new server();
            serv.run_server();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public server() {
        try {
            InitializeMapCourses();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void run_server() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter course registration port number");
        registeration_port = sc.nextInt();
        System.out.println("Enter data requests port number");
        data_request_port = sc.nextInt();
        try {
            dg_reg_socket = new DatagramSocket(registeration_port);
            server_data_socket = new ServerSocket(data_request_port);
            //run 2 threads: one listen for each port
            ThreadUDPListener t_r = new ThreadUDPListener(dg_reg_socket);
            new Thread(t_r).start();
            ThreadTCPListener t_d = new ThreadTCPListener(server_data_socket);
            new Thread(t_d).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void InitializeMapCourses() throws FileNotFoundException {
        File myObj = new File("src\\courses.txt");
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String course = data.split("-", 2)[0];
            String vacancies = data.split("-", 2)[1];
            courses.put(course, new Course(Integer.parseInt(vacancies)));
        }
        myReader.close();
    }

    public class Course {
        Integer vacancies;
        List<String> confirmation_codes = new ArrayList<String>();

        public Course(Integer vac) {
            vacancies = vac;
        }

        public Course add_conf_code(String conf_code) {
            this.confirmation_codes.add(conf_code);
            this.dec_vac();
            return this;
        }

        public void dec_vac() {
            this.vacancies -= 1;
        }
    }

    public class ThreadTCPListener implements Runnable {
        ServerSocket server_socket;
        Socket s;

        public ThreadTCPListener(ServerSocket serv_socket) {
            this.server_socket = serv_socket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    this.s = server_socket.accept();
                    handleDataRequest();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                server.ThreadTCPListener t = new server.ThreadTCPListener(this.server_socket);
                new Thread(t).start();
            }
        }

        private void handleDataRequest() throws IOException, InterruptedException {
            server.Course c;
            PrintWriter write_to_client = new PrintWriter(this.s.getOutputStream());
            while (!re.tryLock()) {
//                System.out.println("data information is LOCKED right now");
            }
            for (String course_num : courses.keySet()) {
                String confirmation_codes = "";
                c = courses.get(course_num);
                for (String conf : c.confirmation_codes) {
                    confirmation_codes += conf + " ";
                }
                if (!confirmation_codes.equals("")) {
                    confirmation_codes = " " + confirmation_codes.substring(0, confirmation_codes.length() - 1);
                }
                write_to_client.println(course_num + " " + c.vacancies + confirmation_codes);
                write_to_client.flush();
            }
            write_to_client.println("DONE");
            write_to_client.flush();
            re.unlock();
        }


    }

    public class ThreadUDPListener implements Runnable {
        DatagramSocket dg_socket;

        public ThreadUDPListener(DatagramSocket dg_s) {
            this.dg_socket = dg_s;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    handleRegRequest();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                server.ThreadUDPListener t = new server.ThreadUDPListener(this.dg_socket);
                new Thread(t).start();
            }
        }

        private void handleRegRequest() throws IOException, InterruptedException {
            String user_input = "";
            DatagramPacket dp_receive;
            byte[] receive;
            while (!user_input.equals("DONE")) {
                receive = new byte[65535];
                dp_receive = new DatagramPacket(receive, receive.length);
                String server_response = "";
                this.dg_socket.receive(dp_receive);
                while (!re.tryLock()) {
//                    System.out.println("request is LOCKED right now");
                }
                user_input = getStrinFromByte(receive);
                if (user_input.equals("") | user_input.equals("\n")) continue;
                if (user_input.equals("DONE")) {
                    re.unlock();
                    break;
                }
                String id = user_input.split(":", 2)[0];
                String course_num = user_input.split(":", 2)[1];
                server.Course c = courses.get(course_num);
                if (!c.vacancies.equals(0)) {
                    //use getConfirmationCode (in sercretariat)
                    String conf_code = Secretariat.getConfirmationCode(course_num, id, c.vacancies);
                    //save confirmation code
                    courses.put(course_num, c.add_conf_code(conf_code));
                    server_response += "Student number " + id + " successfully registered to course number " + course_num;
                } else {
                    server_response += "Registration for course number " + course_num + " is closed";
                }
                //send response to registeration client
                byte[] response = server_response.getBytes();
                DatagramPacket dp_packet = new DatagramPacket(response, response.length, dp_receive.getAddress(), dp_receive.getPort());
                this.dg_socket.send(dp_packet);
                re.unlock();
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

}

