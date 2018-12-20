import com.sap.conn.idoc.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

import com.sap.conn.jco.server.*;
import com.sap.conn.idoc.jco.*;

public class IDocServerExample {
  public static void main(String[] a) {
    try {
      // see examples of configuration files MYSERVER.jcoServer and  BCE.jcoDestination provided in the installation directory.
      JCoIDocServer server = JCoIDoc.getServer("MYSERVER");
      server.setIDocHandlerFactory(new MyIDocHandlerFactory());
      server.setTIDHandler(new MyTidHandler());

      MyThrowableListener listener = new MyThrowableListener();
      server.addServerErrorListener(listener);
      server.addServerExceptionListener(listener);
      server.setConnectionCount(1);
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static class MyIDocHandler implements JCoIDocHandler {
    public void handleRequest(JCoServerContext serverCtx, IDocDocumentList idocList) {

      FileOutputStream fos = null;
      OutputStreamWriter osw = null;
      try {
        IDocXMLProcessor xmlProcessor =
            JCoIDoc.getIDocFactory().getIDocXMLProcessor();
        fos = new FileOutputStream(serverCtx.getTID() + "_idoc.xml");
        osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        xmlProcessor.render(idocList, osw,
            IDocXMLProcessor.RENDER_WITH_TABS_AND_CRLF);
        osw.flush();
      } catch (Throwable thr) {
        thr.printStackTrace();
      } finally {
        try {
          if (osw != null)
            osw.close();
          if (fos != null)
            fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  static class MyIDocHandlerFactory implements JCoIDocHandlerFactory {
    private JCoIDocHandler handler = new MyIDocHandler();

    public JCoIDocHandler getIDocHandler(JCoIDocServerContext serverCtx) {
      return handler;
    }
  }

  static class MyThrowableListener implements JCoServerErrorListener, JCoServerExceptionListener {

    @Override
    public void serverErrorOccurred(JCoServer jCoServer, String s, JCoServerContextInfo jCoServerContextInfo, Error error) {
      System.out.println(">>> Error occured on " + jCoServer.getProgramID() + " connection " + jCoServerContextInfo.getConnectionID());
      error.printStackTrace();
    }

    @Override
    public void serverExceptionOccurred(JCoServer jCoServer, String s, JCoServerContextInfo jCoServerContextInfo, Exception error) {
      System.out.println(">>> Error occured on " + jCoServer.getProgramID() + " connection " + jCoServerContextInfo.getConnectionID());
      error.printStackTrace();
    }
  }

  static class MyTidHandler implements JCoServerTIDHandler {
    public boolean checkTID(JCoServerContext serverCtx, String tid) {
      System.out.println("checkTID called for TID=" + tid);
      return true;
    }

    public void confirmTID(JCoServerContext serverCtx, String tid) {
      System.out.println("confirmTID called for TID=" + tid);
    }

    public void commit(JCoServerContext serverCtx, String tid) {
      System.out.println("commit called for TID=" + tid);
    }

    public void rollback(JCoServerContext serverCtx, String tid) {
      System.out.print("rollback called for TID=" + tid);
    }
  }
}