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

  DatagramPacket sendPacket, receivePacket; // Declaration of DatagramPacket objects for sending and receiving packets
  DatagramSocket receiveSocket; // Declaration of DatagramSocket object for receiving data

  /**
   * The constructor for the Server class.
   */
  public Server() {
    try {
      receiveSocket = new DatagramSocket(69); // Creating a DatagramSocket bound to port 69 for receiving packets
    } catch (SocketException e) {
      System.out.println("Socket creation failed due to an error: ");
      e.printStackTrace();
      System.exit(1); // Exiting the program if an exception occurs
    }
  }

  private void printPacketInfo(DatagramPacket packet, boolean isSendPacket) {
    int len = packet.getLength(); // Getting the length of the packet
    System.out.println(isSendPacket ? "Server: Sending packet:" : "Server: Packet received:"); // Printing message based on whether it's sending or receiving a packet
    System.out.println(isSendPacket ? "To host" : "From host: " + packet.getAddress()); // Printing destination or source host information
    System.out.println(isSendPacket ? "Destination host port" : "Host port: " + packet.getPort()); // Printing the length of the packet
    System.out.println("Length: " + len);  // Getting the data from the packet
    byte[] data = packet.getData(); // Getting the data from the packet
    byte[] trimmedData = Arrays.copyOf(data, len); // Trimming the data to its actual length
    System.out.println("Containing String: " + new String(trimmedData)); // Printing the data as string
    System.out.println("Containing Bytes: " + Arrays.toString(trimmedData) + "\n");
  }

  public boolean isPacketValid(DatagramPacket packet) {
    byte[] data = packet.getData(); // Getting the data from the packet
    int len = packet.getLength(); // Getting the length of the packet
    int i = 0;

    // last byte is 0
    if (data[len - 1] != 0) { // Checking if the last byte is 0
      return false; // Returning false if the last byte is not 0
    }

    // first byte[0] is 0
    if (data[i] != 0) { // Checking if the first byte is 0
      return false; // Returning false if the first byte is not 0
    }
    i++;

    // second byte[1] is either 1 or 2
    if (data[i] != 1 && data[i] != 2) { // Checking if the second byte is 1 or 2
      return false; // Returning false if the second byte is neither 1 nor 2
    }
    i++;

    // filename is present
    while (data[i] != 0) { // Looping until reaching a null byte which indicates end of filename
      i++;
      if (i >= len) { // Checking if the index exceeds the length of the packet
        return false; // Returning false if index exceeds the length of the packet
      }
    }
    i++;

    // mode is present
    while (data[i] != 0) { // Looping until reaching a null byte which indicates end of mode
      i++;
      if (i >= len) { // Checking if the index exceeds the length of the packet
        return false; // Returning false if index exceeds the length of the packet
      }
    }

    return true; // Returning true if all validation checks pass
  }

  public void start() {
    while (true) { // Infinite loop for continuously receiving packets
      byte[] data = new byte[100]; // Creating a byte array to hold received data
      receivePacket = new DatagramPacket(data, data.length); // Creating a DatagramPacket for receiving data

      System.out.println("Server: Waiting...");

      try {
        receiveSocket.receive(receivePacket);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }

      printPacketInfo(receivePacket, false);

      if (!isPacketValid(receivePacket)) { // Checking if the received packet is valid
        throw new RuntimeException("Invalid packet received. Exiting.");
      }

      byte[] receivedData = receivePacket.getData(); // Getting data from the received packet
      byte[] sendData;

      // Read request
      if (receivedData[1] == 1) { // Checking if it's a read request
        sendData = new byte[] { 0, 3, 0, 1 }; // Creating data for response if ReadRequest
      } else { // write request
        sendData = new byte[] { 0, 4, 0, 0 }; // Creating data for response if WriteRequest
      }

      // Slow things down
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        System.exit(1);
      }

      sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort()); // Creating a DatagramPacket for sending response

      printPacketInfo(sendPacket, true); // Printing information about the sent packet

      try {
        DatagramSocket sendIntermediateHostSocket = new DatagramSocket(); // Creating a new DatagramSocket for sending response
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
    Server server = new Server(); // Creating an instance of Server class
    server.start();
  }
}