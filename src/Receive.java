import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Receive implements Runnable {
    private DataInputStream dataInputStream;
    private Socket client;
    private boolean isRunning;

    public Receive(Socket client) {
        this.client = client;
        this.isRunning = true;
        try {
            dataInputStream = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            System.out.println("---a client left---");
            release();
        }
    }

    private String receive() {
        String msg = "";
        try {
            msg = dataInputStream.readUTF();
        } catch (IOException e) {
            System.out.println("---receive message error---");
            release();
        }
        return msg;
    }

    @Override
    public void run() {
        while (isRunning) {
            String msg = receive();
            if (!msg.equals("")) {
                System.out.println(msg);
            }
        }
    }

    private void release() {
        this.isRunning = false;
        CloseUtils.close(dataInputStream, client);
    }

}