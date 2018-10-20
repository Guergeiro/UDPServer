import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class UDPServer {
	static private HashMap<Integer, String> temp = new HashMap<Integer, String>();
	static private HashMap<Integer, String> entregues = new HashMap<Integer, String>();
	static private int nMessage = 1;
	
	public static void main(String args[]) {

		DatagramSocket aSocket = null;

		try {

			aSocket = new DatagramSocket(6789);

			byte[] buffer = new byte[1000];
			
			while (true) {

				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				aSocket.receive(request);
				
				// Divide a mensagem em apenas 2 partes (divide na primeira instancia da ",")
				String message[] = (new String(request.getData())).split(",",2);
				
				byte m[];
				
				if (Integer.parseInt(message[0]) == nMessage) {
					// Apenas é necessário processar as mensagens em temp qnd finalmente vem a mensagem que esperamos
					entregues.put(nMessage++, message[1]);
					processMessages();
				} else {
					// Mensagem nao é a que esperamos
					temp.put(Integer.parseInt(message[0]), message[1]);
				}
				
				if (temp.size() == 0) {
					// Ja nao existem mensagens no temp, devolvemos a ultima mensagem (em entregues) correta
					m = (entregues.get(nMessage - 1)).getBytes();
				} else {
					// Ainda estamos à espera da mensagem correta
					m = ("waitingfor,"+nMessage).getBytes();
				}
				
				DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
				aSocket.send(reply);
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());

		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());

		} finally {
			if (aSocket != null)
				aSocket.close();
		}		
	}
	
	static private void processMessages() {
		while (temp.size() > 0) {
			if (!temp.containsKey(nMessage)) {
				// Nao contem a proxima mensagem
				break;
			}
			entregues.put(nMessage, temp.get(nMessage));
			temp.remove(nMessage++);
		}
	}
}