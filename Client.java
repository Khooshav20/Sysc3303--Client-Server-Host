import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Bundhoo Khooshav
 *
 *         This class represents a client that a user could use to interface
 *         with a server. It takes files and sends them to a host which interfaces with
 *         server using datagram sockets.
 */
public class Client {

	DatagramPacket sendPacket, receivePacket; // Declaration of DatagramPacket objects for sending and receiving packets
	DatagramSocket sendReceiveSocket; // Declaration of DatagramSocket object for sending and receiving data

	/**
	 * The constructor for the Client class.
	 */
	public Client() {
		try {
			sendReceiveSocket = new DatagramSocket(); // Initialize the DatagramSocket for sending and receiving datagrams
		} catch (SocketException e) {
			System.out.println("Socket creation failed due to an error: ");
			e.printStackTrace();
			System.exit(1); // Exiting the program if an exception occurs
		}
	}

	/**
	 * method to print information about a DatagramPacket.
	 *
	 * @param packet      DatagramPacket to print information about.
	 * @param isSendPacket True if the packet is being sent, false if it's received.
	 */

	private void printPacketInfo(DatagramPacket packet, boolean isSendPacket) {
		// Print packet information including source/destination address, port, length, and content
		int len = packet.getLength(); // Getting the length of the packet
		System.out.println(isSendPacket ? "Client: Sending packet:" : "Client: Packet received:"); // Printing message depending on whether it's sending or receiving a packet
		System.out.println(isSendPacket ? "To host" : "From host: " + packet.getAddress()); // Printing destination or source host information
		System.out.println(isSendPacket ? "Destination host port" : "Host port: " + packet.getPort()); // Printing destination or source host port
		System.out.println("Length: " + len); // Printing the length of the packet
		System.out.println("Containing String: " + new String(packet.getData(), 0, len));
		System.out.println("Containing Bytes: " + Arrays.toString(packet.getData()) + "\n");
	}

	/**
	 * Create packet data to be sent in a datagram.
	 *
	 * @param filename     Name of the file to include in the packet.
	 * @param mode         Mode to include in the packet.
	 * @param isReadRequest True if it's a read request, false if it's a write request.
	 * @return Byte array representing the packet data.
	 */
	private byte[] createPacketData(String filename, String mode, boolean isReadRequest) {
		byte[] filenameBytes = filename.getBytes(); // Getting bytes of filename
		byte[] modeBytes = mode.getBytes(); // Getting bytes of mode
		byte[] packetData = new byte[2 + filenameBytes.length + 1 + modeBytes.length + 1]; // Creating byte array to hold packet data
		packetData[0] = 0;
		packetData[1] = (byte) (isReadRequest ? 1 : 2); // Second byte indicating read (1) or write (2) request

		for (int i = 0; i < filenameBytes.length; i++) {
			packetData[i + 2] = filenameBytes[i]; // Setting filename bytes in packet data
		}

		packetData[filenameBytes.length + 2] = 0; // Setting null byte after filename

		for (int i = 0; i < modeBytes.length; i++) {
			packetData[i + filenameBytes.length + 3] = modeBytes[i]; // Setting mode bytes in packet data
		}

		packetData[filenameBytes.length + modeBytes.length + 3] = 0; // Setting null byte after mode

		return packetData; // Returning the packet data
	}

	private void sendAndReceivePacket(byte[] packetData) {
		try {
			sendPacket = new DatagramPacket(packetData, packetData.length, InetAddress.getLocalHost(), 23);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		printPacketInfo(sendPacket, true);

		try {
			sendReceiveSocket.send(sendPacket); // Sending the packet
		} catch (IOException e) {
			System.out.println("Send socket timed out: ");
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Client: Packet sent.\n");

		byte data[] = new byte[4]; // Creating byte array to hold received data
		receivePacket = new DatagramPacket(data, data.length); // Creating DatagramPacket for receiving data

		System.out.println("Client: Waiting..."); // Printing a waiting message

		// Slow things down
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			sendReceiveSocket.receive(receivePacket); // Receiving a packet
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Process the received datagram.
		printPacketInfo(receivePacket, false);

		// Slow things down
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Start the client .
	 */
	public void start() {
		Random random = new Random(); // Creating an instance of Random class for generating random modes


		for (int i = 1; i <= 10; i++) { // Loop for sending multiple packets
			boolean isReadRequest = i % 2 != 0; // Alternating between read and write requests
			String mode = random.nextBoolean() ? "netascii" : "octet"; // Generating mode randomly
			String filename = "test" + i + ".txt"; // Creating filename

			byte[] packetData = createPacketData(filename, mode, isReadRequest); // Creating packet data
			sendAndReceivePacket(packetData); // Sending and receiving packet
		}

		byte[] packetData = createPacketData("test.txt", "netascii", true);
		packetData[0] = 1; // force an invalid request
		sendAndReceivePacket(packetData);

		sendReceiveSocket.close();
	}

	/**
	 * Main method to run the Client.
	 *
	 */
	public static void main(String args[]) {
		Client c = new Client();  // Creating an instance of Client class
		c.start(); // Starting the client
	}
}