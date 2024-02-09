import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Bundhoo Khooshav
 *
 *         This class represents a client that a user could use to interface
 *         with a
 *         server. It takes files and sends them to a host which interfaces with
 *         a
 *         server using datagram sockets.
 */
public class Client {

	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendReceiveSocket;

	/**
	 * The constructor for the Client class.
	 */
	public Client() {
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Socket creation failed due to an error: ");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void printPacketInfo(DatagramPacket packet, boolean isSendPacket) {
		int len = packet.getLength();
		System.out.println(isSendPacket ? "Client: Sending packet:" : "Client: Packet received:");
		System.out.println(isSendPacket ? "To host" : "From host: " + packet.getAddress());
		System.out.println(isSendPacket ? "Destination host port" : "Host port: " + packet.getPort());
		System.out.println("Length: " + len);
		System.out.println("Containing String: " + new String(packet.getData(), 0, len));
		System.out.println("Containing Bytes: " + Arrays.toString(packet.getData()) + "\n");
	}

	private byte[] createPacketData(String filename, String mode, boolean isReadRequest) {
		byte[] filenameBytes = filename.getBytes();
		byte[] modeBytes = mode.getBytes();

		byte[] packetData = new byte[2 + filenameBytes.length + 1 + modeBytes.length + 1];
		packetData[0] = 0;
		packetData[1] = (byte) (isReadRequest ? 1 : 2);

		for (int i = 0; i < filenameBytes.length; i++) {
			packetData[i + 2] = filenameBytes[i];
		}

		packetData[filenameBytes.length + 2] = 0;

		for (int i = 0; i < modeBytes.length; i++) {
			packetData[i + filenameBytes.length + 3] = modeBytes[i];
		}

		packetData[filenameBytes.length + modeBytes.length + 3] = 0;

		return packetData;
	}

	/**
	 * This function is used to create and send a packet from the client to the
	 * host,
	 * containing information for a .txt file.
	 * 
	 * @param filename    String, the name of the file that will be shared as a
	 *                    packet.
	 * @param mode        String, the mode that will also be included in the packet.
	 * @param requestType String, the type of request; read or write. are the two
	 *                    valid types.
	 */
	private void sendAndReceivePacket(byte[] packetData) {
		try {
			sendPacket = new DatagramPacket(packetData, packetData.length, InetAddress.getLocalHost(), 23);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		printPacketInfo(sendPacket, true);

		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("Send socket timed out: ");
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Client: Packet sent.\n");

		byte data[] = new byte[4];
		receivePacket = new DatagramPacket(data, data.length);

		System.out.println("Client: Waiting...");

		// Slow things down
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			sendReceiveSocket.receive(receivePacket);
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

	public void start() {
		Random random = new Random();

		for (int i = 1; i <= 10; i++) {
			boolean isReadRequest = i % 2 != 0;
			String mode = random.nextBoolean() ? "netascii" : "octet";
			String filename = "test" + i + ".txt";

			byte[] packetData = createPacketData(filename, mode, isReadRequest);
			sendAndReceivePacket(packetData);
		}

		byte[] packetData = createPacketData("test.txt", "netascii", true);
		packetData[0] = 1; // force an invalid request
		sendAndReceivePacket(packetData);

		sendReceiveSocket.close();
	}

	public static void main(String args[]) {
		Client c = new Client();
		c.start();
	}
}