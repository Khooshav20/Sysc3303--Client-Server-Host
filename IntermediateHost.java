import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

/**
 * @author Bundhoo Khooshav
 * 
 * This class represents an intermediate host that acts as an intermediary between
 * a client and a server. It receives packets from a client, forwards them to
 * a server, receives response packets from the server, and forwards them back
 * to the client.
 */
public class IntermediateHost {
  DatagramSocket receiveSocket, sendReceiveSocket; // Declaration of DatagramSocket objects for receiving and sending/receiving packets
  DatagramPacket receiveClientPacket, receiveServerPacket, sendPacket; // Declaration of DatagramPacket objects for receiving and sending packets

  public IntermediateHost() {
    try {
      receiveSocket = new DatagramSocket(23); // Creating a DatagramSocket bound to port 23 for receiving packets from client
      sendReceiveSocket = new DatagramSocket(); // Creating a DatagramSocket for sending and receiving packets
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1); // Exiting the program if an exception occurs
    }
  }

  private void printPacketInfo(DatagramPacket packet, boolean isSendPacket) {
    int len = packet.getLength(); // Getting the length of the packet
    System.out.println(isSendPacket ? "Intermediate Host: Sending packet:" : "Intermediate Host: Packet received:"); // Printing message based on whether it's sending or receiving a packet
    System.out.println(isSendPacket ? "To host" : "From host: " + packet.getAddress()); // Printing destination or source host information
    System.out.println(isSendPacket ? "Destination host port" : "Host port: " + packet.getPort()); // Printing destination or source host port
    System.out.println("Length: " + len); // Printing the length of the packet
    byte[] data = packet.getData(); // Getting the data from the packet
    byte[] trimmedData = Arrays.copyOf(data, len); // Trimming the data to its actual length
    System.out.println("Containing String: " + new String(trimmedData)); // Printing the data as string
    System.out.println("Containing Bytes: " + Arrays.toString(trimmedData) + "\n");
  }

  public void start() {
    while (true) { // Infinite loop for continuously receiving packets
      byte[] data = new byte[100]; // Creating a byte array to hold received data
      receiveClientPacket = new DatagramPacket(data, data.length); // Creating a DatagramPacket for receiving data


      System.out.println("Intermediate Host: Waiting..."); // Printing a waiting message


      try {
        receiveSocket.receive(receiveClientPacket);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1); // Exiting the program if an exception occurs
      }

      printPacketInfo(receiveClientPacket, false); // Printing information about the received packet

      // Slow things down
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.exit(1); // Exiting the program if an exception occurs
      }

      sendPacket = new DatagramPacket(receiveClientPacket.getData(), receiveClientPacket.getLength(),
          receiveClientPacket.getAddress(),
          69); // Creating a DatagramPacket for sending to server on port 69


      printPacketInfo(sendPacket, true); // Printing information about the packet being sent to server

      try {
        sendReceiveSocket.send(sendPacket); // Sending the packet to server
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      System.out.println("Intermediate Host: Packet sent.\n");

      data = new byte[4]; // Creating a byte array to hold received data
      receiveServerPacket = new DatagramPacket(data, data.length); // Creating a DatagramPacket for receiving data from server

      try {
        System.out.println("Intermediate Host: Waiting..."); // Printing a waiting message
        sendReceiveSocket.receive(receiveServerPacket); // Receiving a packet from server
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      printPacketInfo(receiveServerPacket, false); // Printing information about the received packet from server


      // Slow things down
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.exit(1);
      }

      sendPacket = new DatagramPacket(receiveServerPacket.getData(), receiveServerPacket.getLength(),
          receiveClientPacket.getAddress(),
          receiveClientPacket.getPort()); // Creating a DatagramPacket for sending to client


      printPacketInfo(sendPacket, true);

      try {
        DatagramSocket sendClientSocket = new DatagramSocket();
        sendClientSocket.send(sendPacket);
        sendClientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1); // Exiting the program if an exception occurs
      }

      System.out.println("Intermediate Host: Packet sent.\n"); // Printing confirmation message
    }
  }

  public static void main(String[] args) {
    IntermediateHost host = new IntermediateHost(); // Creating an instance of IntermediateHost class
    host.start(); // Starting the intermediate host
  }
}
