import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Send implements Runnable {
    private BufferedReader console;
    private DataOutputStream dataOutputStream;
    private Socket Client;
    private boolean isRunning;
    private String name;

    public Send(Socket client, String name) {
        this.Client = client;
        console = new BufferedReader(new InputStreamReader(System.in));
        this.isRunning = true;
        this.name = name;
        try {
            dataOutputStream = new DataOutputStream(client.getOutputStream());
            send(name);
        } catch (IOException e) {
            System.out.println("Failed");
            this.release();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            String msg = getStrFromConsole();
            if (!msg.equals("")) {
                send(msg);
            }
        }
    }

    private void send(String msg) {
        try {
            dataOutputStream.writeUTF(msg);
            dataOutputStream.flush();
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Failed");
            this.release();
        }
    }

    private void release() {
        this.isRunning = false;
        CloseUtils.close(dataOutputStream, Client);
    }

    private String getStrFromConsole() {
        try {
            return console.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
