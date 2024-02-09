import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * @author Bundhoo Khooshav
 *
 *         This client represents a server. A user will try to send information
 *         to
 *         this server from a client, and when a packet is received the server
 *         will send a response back through the intermediate host.
 */
public class Server {

  DatagramPacket sendPacket, receivePacket;
  DatagramSocket receiveSocket;

  /**
   * The constructor for the Server class.
   */
  public Server() {
    try {
      receiveSocket = new DatagramSocket(69);
    } catch (SocketException e) {
      System.out.println("Socket creation failed due to an error: ");
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void printPacketInfo(DatagramPacket packet, boolean isSendPacket) {
    int len = packet.getLength();
    System.out.println(isSendPacket ? "Server: Sending packet:" : "Server: Packet received:");
    System.out.println(isSendPacket ? "To host" : "From host: " + packet.getAddress());
    System.out.println(isSendPacket ? "Destination host port" : "Host port: " + packet.getPort());
    System.out.println("Length: " + len);
    byte[] data = packet.getData();
    byte[] trimmedData = Arrays.copyOf(data, len);
    System.out.println("Containing String: " + new String(trimmedData));
    System.out.println("Containing Bytes: " + Arrays.toString(trimmedData) + "\n");
  }

  public boolean isPacketValid(DatagramPacket packet) {
    byte[] data = packet.getData();
    int len = packet.getLength();
    int i = 0;

    // last byte is 0
    if (data[len - 1] != 0) {
      return false;
    }

    // first byte[0] is 0
    if (data[i] != 0) {
      return false;
    }
    i++;

    // second byte[1] is either 1 or 2
    if (data[i] != 1 && data[i] != 2) {
      return false;
    }
    i++;

    // filename is present
    while (data[i] != 0) {
      i++;
      if (i >= len) {
        return false;
      }
    }
    i++;

    // mode is present
    while (data[i] != 0) {
      i++;
      if (i >= len) {
        return false;
      }
    }

    return true;
  }

  public void start() {
    while (true) {
      byte[] data = new byte[100];
      receivePacket = new DatagramPacket(data, data.length);

      System.out.println("Server: Waiting...");

      try {
        receiveSocket.receive(receivePacket);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      printPacketInfo(receivePacket, false);

      if (!isPacketValid(receivePacket)) {
        throw new RuntimeException("Invalid packet received. Exiting.");
      }

      byte[] receivedData = receivePacket.getData();
      byte[] sendData;

      // Read request
      if (receivedData[1] == 1) {
        sendData = new byte[] { 0, 3, 0, 1 };
      } else { // write request
        sendData = new byte[] { 0, 4, 0, 0 };
      }

      // Slow things down
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.exit(1);
      }

      sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());

      printPacketInfo(sendPacket, true);

      try {
        DatagramSocket sendIntermediateHostSocket = new DatagramSocket();
        sendIntermediateHostSocket.send(sendPacket);
        sendIntermediateHostSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      System.out.println("Server: Packet sent.\n");
    }
  }

  public static void main(String[] args) {
    Server server = new Server();
    server.start();
  }
}