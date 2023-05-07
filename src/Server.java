import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final CopyOnWriteArrayList<Channel> all = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("---Server---");
        ServerSocket server = new ServerSocket(8888);
        while (true) {
            Socket client = server.accept();
            System.out.println("A Client Connected");
            Channel channel = new Channel(client);
            all.add(channel);
            new Thread(channel).start();
            new Send(client).start();
        }
    }

    static class Send extends Thread {
        private Socket socket;

        public Send(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                this.sendMsy();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendMsy() throws IOException {
            Scanner scanner = null;
            DataOutputStream dataOutputStream = null;
            try {
                scanner = new Scanner(System.in);
                dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
                while (true) {
                    String str = scanner.nextLine();
                    dataOutputStream.writeUTF("Server Send: " + str);
                    dataOutputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class Channel implements Runnable {
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private Socket Client;
        private boolean isRunning;
        private String name;

        public Channel(Socket client) {
            this.Client = client;
            try {
                dataInputStream = new DataInputStream(Client.getInputStream());
                dataOutputStream = new DataOutputStream(Client.getOutputStream());
                isRunning = true;
                this.name = receive();
                this.send("welcome");
                sendOthers(this.name + "is in chatroom", true);
            } catch (IOException e) {
                System.out.println("Failed");
                release();
            }
        }

        private String receive() {
            String msg = "";
            try {
                msg = dataInputStream.readUTF();
            } catch (IOException e) {
                System.out.println("Failed");
                release();
            }
            return msg;
        }

        private void send(String msg) {
            try {
                dataOutputStream.writeUTF(msg);
                dataOutputStream.flush();
            } catch (IOException e) {
                System.out.println("Failed");
                release();
            }
        }

        private void sendOthers(String msg, boolean isSys) {
            boolean isPrivate = msg.startsWith("@");
            if (isPrivate) {
                int idx = msg.indexOf(":");
                String targetName = msg.substring(1, idx);
                msg = msg.substring(idx + 1);
                for (Channel other : all) {
                    if (other.name.equals(targetName)) {
                        other.send(this.name + "(private):" + msg);
                        break;
                    }
                }
            } else {
                for (Channel other : all) {
                    if (other == this) {
                        continue;
                    }
                    if (!isSys) {
                        other.send(this.name + "(public):" + msg);
                    } else {
                        other.send(msg);
                    }
                }
            }
        }

        private void release() {
            this.isRunning = false;
            CloseUtils.close(dataInputStream, dataOutputStream, Client);
            all.remove(this);
            sendOthers(this.name + "left this room", true);
        }

        @Override
        public void run() {
            while (isRunning) {
                String msg = receive();
                if (!msg.equals(""))
                    sendOthers(msg, false);
            }
        }
    }
}
