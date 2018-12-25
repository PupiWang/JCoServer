package WebsocketClient;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import javax.net.ssl.SSLContext;

public class HelloWSS {
  public static void main(String[] args) throws Exception {
    String endpoint = System.getenv("ENDPOINT");
    String user = System.getenv("USER");
    String password = System.getenv("PASSWORD");

    System.out.println("endpoint:" + endpoint);
    System.out.println("user:" + user);
    System.out.println("password:" + password);

    SSLContext context = NaiveSSLContext.getInstance("TLS");

    new WebSocketFactory()
        .setVerifyHostname(false)
        .setSSLContext(context)
        .createSocket(endpoint)
        .addListener(new WebSocketAdapter() {
          @Override
          public void onTextMessage(WebSocket ws, String message) {
            System.out.println("Received:" + message);
          }
        })
        .setUserInfo(user, password)
        .connect()
        .sendText("Hello.");
  }
}
