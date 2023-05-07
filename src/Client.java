import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws UnknownHostException, IOException {
        System.out.println("---Client---");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("input username");
        String name = bufferedReader.readLine();
        Socket client = new Socket("localhost", 8888);
        new Thread(new Send(client, name)).start();
        new Thread(new Receive(client)).start();
    }
}
