package JcoServer;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.*;

import java.io.*;
import java.util.Properties;

import org.apache.log4j.Logger;

public class SampleAbapConnectorServer implements JCoServerErrorListener, JCoServerExceptionListener, JCoServerStateChangedListener {

  private static Logger logger = Logger.getLogger(SampleAbapConnectorServer.class);

  /**
   * The properties necessary to define the server and destination.
   */
  private Properties properties;

  public SampleAbapConnectorServer() throws IOException {
    InputStream propertiesInputStream =
        SampleAbapConnectorServer.class.getClassLoader().getResourceAsStream("jco.properties");
    properties = new Properties();
    properties.load(propertiesInputStream);

    new MyDestinationDataProvider(properties);
    new MyServerDataProvider(properties);
  }

  /**
   * Runnable to listen to the standard input stream to end the server.
   */
  private Runnable stdInListener = new Runnable() {

    @Override
    public void run() {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String line = null;
      try {
        while((line = br.readLine()) != null) {
          // Check if the server should be ended.
          if(line.equalsIgnoreCase("end")) {
            // Stop the server.
            server.stop();
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  };

  private JCoServer server;

  public void serve() {
    try {
      server = JCoServerFactory.getServer(properties.getProperty(ServerDataProvider.JCO_PROGID));
    } catch(JCoException e) {
      throw new RuntimeException("Unable to create the server " + properties.getProperty(ServerDataProvider.JCO_PROGID) + ", because of " + e.getMessage(), e);
    }

    JCoServerFunctionHandler abapCallHandler = new AbapCallHandler();
    DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
    factory.registerHandler(AbapCallHandler.FUNCTION_NAME, abapCallHandler);
    server.setCallHandlerFactory(factory);

    // Add listener for errors.
    server.addServerErrorListener(this);
    // Add listener for exceptions.
    server.addServerExceptionListener(this);
    // Add server state change listener.
    server.addServerStateChangedListener(this);

    // Add a stdIn listener.
    new Thread(stdInListener).start();

    // Start the server
    server.start();
//    logger.info("The program can be stopped typing 'END'");
  }

  @Override
  public void serverExceptionOccurred(JCoServer jcoServer, String connectionId, JCoServerContextInfo arg2, Exception exception) {
    logger.error("Exception occured on " + jcoServer.getProgramID() + " connection " + connectionId, exception);
  }

  @Override
  public void serverErrorOccurred(JCoServer jcoServer, String connectionId, JCoServerContextInfo arg2, Error error) {
    logger.error("Error occured on " + jcoServer.getProgramID() + " connection " + connectionId, error);
  }

  @Override
  public void serverStateChangeOccurred(JCoServer server, JCoServerState oldState, JCoServerState newState) {
    // Defined states are: STARTED, DEAD, ALIVE, STOPPED;
    // see JCoServerState class for details.
    // Details for connections managed by a server instance
    // are available via JCoServerMonitor
    logger.info("Server state changed from " + oldState.toString() + " to " + newState.toString() +
        " on server with program id " + server.getProgramID());
    if(newState.equals(JCoServerState.ALIVE)) {
      logger.info("Server with program ID '"+server.getProgramID()+"' is running");
    }
    if(newState.equals(JCoServerState.STOPPED)) {
      logger.info("Exit program");
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
//    if(args.length == 0) {
//      logger.error("You must specify a properties file!");
//      System.out.println("You must specify a properties file!");
//      return;
//    }
    new SampleAbapConnectorServer().serve();
  }
}
