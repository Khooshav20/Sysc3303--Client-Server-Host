import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class IntermediateHost {
  DatagramSocket receiveSocket, sendReceiveSocket;
  DatagramPacket receiveClientPacket, receiveServerPacket, sendPacket;

  public IntermediateHost() {
    try {
      receiveSocket = new DatagramSocket(23);
      sendReceiveSocket = new DatagramSocket();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void printPacketInfo(DatagramPacket packet, boolean isSendPacket) {
    int len = packet.getLength();
    System.out.println(isSendPacket ? "Intermediate Host: Sending packet:" : "Intermediate Host: Packet received:");
    System.out.println(isSendPacket ? "To host" : "From host: " + packet.getAddress());
    System.out.println(isSendPacket ? "Destination host port" : "Host port: " + packet.getPort());
    System.out.println("Length: " + len);
    byte[] data = packet.getData();
    byte[] trimmedData = Arrays.copyOf(data, len);
    System.out.println("Containing String: " + new String(trimmedData));
    System.out.println("Containing Bytes: " + Arrays.toString(trimmedData) + "\n");
  }

  public void start() {
    while (true) {
      byte[] data = new byte[100];
      receiveClientPacket = new DatagramPacket(data, data.length);

      System.out.println("Intermediate Host: Waiting...");

      try {
        receiveSocket.receive(receiveClientPacket);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      printPacketInfo(receiveClientPacket, false);

      // Slow things down
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.exit(1);
      }

      sendPacket = new DatagramPacket(receiveClientPacket.getData(), receiveClientPacket.getLength(),
          receiveClientPacket.getAddress(),
          69);

      printPacketInfo(sendPacket, true);

      try {
        sendReceiveSocket.send(sendPacket);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      System.out.println("Intermediate Host: Packet sent.\n");

      data = new byte[4];
      receiveServerPacket = new DatagramPacket(data, data.length);

      try {
        System.out.println("Intermediate Host: Waiting...");
        sendReceiveSocket.receive(receiveServerPacket);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      printPacketInfo(receiveServerPacket, false);

      // Slow things down
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.exit(1);
      }

      sendPacket = new DatagramPacket(receiveServerPacket.getData(), receiveServerPacket.getLength(),
          receiveClientPacket.getAddress(),
          receiveClientPacket.getPort());

      printPacketInfo(sendPacket, true);

      try {
        DatagramSocket sendClientSocket = new DatagramSocket();
        sendClientSocket.send(sendPacket);
        sendClientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      System.out.println("Intermediate Host: Packet sent.\n");
    }
  }

  public static void main(String[] args) {
    IntermediateHost host = new IntermediateHost();
    host.start();
  }
}
