package Server.TCP;
import java.net.*;
import java.io.*;
import java.util.*;
import Server.LockManager.*;

//TCPMiddleware will re-route the information in the socket to the appropriate RM depending
//on which type of 
public class TCPMiddleware extends Thread{
	private Map<Integer,ArrayList<String>> all_txn = Collections.synchronizedMap(new HashMap<Integer,ArrayList<String>>());
	private LockManager lockManager = new LockManager();

	public static void main(String args[]){

		TCPMiddleware server = new TCPMiddleware();
		try{
			server.runServerThread();
		}catch(Exception e){
			System.out.println("ERROR IN TCP MIDDLEWARE!!!");
			e.printStackTrace();
		}
	}

	class ServerSocketThread extends Thread {
		Socket socket;

		ServerSocketThread(Socket socket){
			this.socket = socket;
		}

		public void run() {

			try {
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply;

				Socket resource_socket;

				String message = inFromClient.readLine();
				if(message == null){return;}
				System.out.println("MESSAGE RECEIVED = " + message);

				synchronized (all_txn) {
					synchronized (lockManager) {
						//Store the message in the hashmap of all transactions
						String[] params1 = message.split(",");
						int xid = -1;
						if (!params1[0].equals("Start") && !params1[0].equals("Commit") && !params1[0].equals("Abort")) {
							xid = Integer.parseInt(params1[1]);
							if (all_txn.containsKey(xid)) {
								ArrayList<String> temp = all_txn.get(xid);
								temp.add(message);
								all_txn.put(xid, temp);
							} else {
								ArrayList<String> temp = new ArrayList<>();
								temp.add(message);
								all_txn.put(xid, temp);
							}
						}


						if (message.contains("Start")) {
							int ret = 1;
							if (!all_txn.keySet().isEmpty()) {
								int max = Collections.max(all_txn.keySet());
								ret = max + 1;
							}
							ArrayList<String> temp = new ArrayList<>();
							temp.add(message);
							all_txn.put(ret, temp);

							serverReply = Integer.toString(ret);
							System.out.print("Active transaction ids in the middleware: [");
							int c = 1;
							for (int i : all_txn.keySet()) {
								System.out.print(i);
								if (c != all_txn.keySet().size()) {
									System.out.print(",");
								}
								c++;
							}
							System.out.println("]\n");

							PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
							outClient.println(serverReply);
						} else if (message.contains("Commit")) {
							xid = Integer.parseInt(params1[1]);
							//If the xid is not contained in the list of active transaction xid, then return false message

							if (!all_txn.containsKey(xid)) {
								System.out.println("HASHMAP DOES NOT CONTAIN XID");
								System.out.print("Active transaction ids in the middleware: [");
								//printing the list of active transaction xid
								int c = 1;
								for (int i : all_txn.keySet()) {
									System.out.print(i);
									if (c != all_txn.keySet().size()) {
										System.out.print(",");
									}
									c++;
								}
								System.out.println("]\n");

								//response to client
								serverReply = "false";
								PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
								outClient.println(serverReply);
							} else {
								System.out.println("HASHMAP DOES CONTAIN XID");
								//Unlock all the locks owned by transaction xid
								lockManager.UnlockAll(xid);
								//Remove xid from the list of active transaction xids
								all_txn.remove(xid);

								System.out.print("Active transaction ids in the middleware: [");
								int c = 1;
								for (int i : all_txn.keySet()) {
									System.out.print(i);
									if (c != all_txn.keySet().size()) {
										System.out.print(",");
									}
									c++;
								}
								System.out.println("]\n");

								serverReply = "true";
								PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
								outClient.println(serverReply);
							}
						} else if (message.contains("Abort")) {
							xid = Integer.parseInt(params1[1]);
							abort(xid);
						}

						///ISMA SPAGHETTI CODE SECTION
						if(message.contains("Delete"))
						{
							String deleteundo = params1[0] + "Undo" + "," + params1[1] + "," + params1[2] + ",";

							String serverconnection = "";
							String querySeats = "";
							String queryPrice = "";
							if(message.contains("Flight"))
							{
								serverconnection = "lab2-30";
								querySeats = "QueryFlight," + params1[1] + "," + params1[2];
								queryPrice = "QueryFlightPrice," + params1[1] + "," + params1[2];
							}

							if(message.contains("Cars")){
								serverconnection = "lab2-31";
								querySeats = "QueryCars," + params1[1] + "," + params1[2];
								queryPrice = "QueryCarsPrice," + params1[1] + "," + params1[2];
							}
							if(message.contains("Rooms")){
								serverconnection = "lab2-32";
								querySeats = "QueryRooms," + params1[1] + "," + params1[2];
								queryPrice = "QueryRoomsPrice," + params1[1] + "," + params1[2];
							}

							Socket toServer1 = new Socket(serverconnection,4042);
							PrintWriter out1 = new PrintWriter(toServer1.getOutputStream(),true);
							out1.println(querySeats);
							BufferedReader wat1 = new BufferedReader(new InputStreamReader(toServer1.getInputStream()));
							String seats = wat1.readLine();
							toServer1.close();

							Socket toServer = new Socket(serverconnection,4042);
							PrintWriter out = new PrintWriter(toServer.getOutputStream(),true);
							out.println(queryPrice);
							BufferedReader wat = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
							String price = wat.readLine();
							toServer.close();

							deleteundo = deleteundo+ seats + "," + price;
							int pos = Integer.parseInt(params1[1]);
							ArrayList<String> som = all_txn.get(pos);
							som.add(deleteundo);
							all_txn.put(pos, som);

						}

						//Now we need to parse the string and see if its for flight, room, car or customer
						//message is the string to parse


						if (message.contains("AddFlight") || message.contains("DeleteFlight") || message.contains("QueryFlight") || message.contains("QueryFlightPrice")) {

							//lab2-30 is flight
							//lab2-31 is car
							//lab2-32 is room
							//lab2-33 is customer
							//lab2-50 is middleware
							//lab2-51 is client

							boolean lockSuccess = false;
							System.out.println("Inside Flight " + params1[1]);

							try {
								if (message.contains("AddFlight") || message.contains("DeleteFlight")) {
									lockSuccess = lockManager.Lock(xid, "Flight", TransactionLockObject.LockType.LOCK_WRITE);
								} else {
									lockSuccess = lockManager.Lock(xid, "Flight", TransactionLockObject.LockType.LOCK_READ);
								}

								if (lockSuccess == true) {
									resource_socket = new Socket("lab2-30", 4042);

									PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
									outServer.println(message);

									BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
									serverReply = inFromServer.readLine();
									resource_socket.close();

									PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
									outClient.println(serverReply);
								} else {
									System.out.println("Error at LOCK ");
									if (message.contains("AddFlight") || message.contains("DeleteFlight")) {
										serverReply = "false";
									} else {
										serverReply = "deadlock";
									}
									PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
									outClient.println(serverReply);
								}
							} catch (DeadlockException e) {
								System.out.println("Deadlock exception!");
								if (message.contains("AddFlight") || message.contains("DeleteFlight")) {
									serverReply = "false";
								} else {
									serverReply = "deadlock";
								}
								PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
								outClient.println(serverReply);
							}

//					resource_socket = new Socket("lab2-30", 4042);
//
//					PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
//					outServer.println(message);
//
//					BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
//					serverReply = inFromServer.readLine();
//					resource_socket.close();
//
//					PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
//					outClient.println(serverReply);


						} else if (message.contains("AddCars") || message.contains("DeleteCars") || message.contains("QueryCars") || message.contains("QueryCarsPrice")) {
							resource_socket = new Socket("lab2-31", 4042);
							PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
							outServer.println(message);

							BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
							serverReply = inFromServer.readLine();
							resource_socket.close();

							PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
							outClient.println(serverReply);


						} else if (message.contains("AddRooms") || message.contains("DeleteRooms") || message.contains("QueryRooms") || message.contains("QueryRoomsPrice")) {
							resource_socket = new Socket("lab2-32", 4042);
							PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
							outServer.println(message);
							BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
							serverReply = inFromServer.readLine();
							resource_socket.close();
							PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
							outClient.println(serverReply);

						} else if (message.contains("QueryCustomer")) {
							String reply = "a";
							resource_socket = new Socket("lab2-30", 4042);
							PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
							outServer.println(message);
							BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
							serverReply = inFromServer.readLine();
							while ((serverReply != null)) {
								reply += serverReply;
								reply += "\n";
								serverReply = inFromServer.readLine();
							}
							resource_socket.close();
							System.out.println("RESPONSE FROM FLIGHT SERVER = ");
							System.out.println(reply);

							Socket secondsocket = new Socket("lab2-31", 4042);
							PrintWriter outServer2 = new PrintWriter(secondsocket.getOutputStream(), true);
							outServer2.println(message);
							BufferedReader inFromServer2 = new BufferedReader(new InputStreamReader(secondsocket.getInputStream()));
							String serverReply2 = inFromServer2.readLine();
							while ((serverReply2 != null)) {
								reply += serverReply2;
								reply += "\n";
								serverReply2 = inFromServer2.readLine();
							}
							System.out.println("RESPONSE + CAR SERVER = ");
							System.out.println(reply);
							secondsocket.close();
							Socket thirdsocket = new Socket("lab2-32", 4042);
							PrintWriter outServer3 = new PrintWriter(thirdsocket.getOutputStream(), true);
							outServer3.println(message);
							BufferedReader inFromServer3 = new BufferedReader(new InputStreamReader(thirdsocket.getInputStream()));
							String serverReply3 = inFromServer3.readLine();
							while ((serverReply3 != null)) {
								reply += serverReply3;
								reply += "\n";
								serverReply3 = inFromServer3.readLine();
							}
							System.out.println("RESPONSE + ROOM SERVER = ");
							System.out.println(reply);
							thirdsocket.close();

							PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
							outClient.println(reply);
							socket.close();

							//this part will deal with both AddCustomer and AddCustomerID
						} else if (message.contains("AddCustomer") || message.contains("DeleteCustomer")) {
							resource_socket = new Socket("lab2-33", 4042);
							PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
							outServer.println(message);
							BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
							serverReply = inFromServer.readLine();
							resource_socket.close();

							//so the message only contains AddCustomer
							if (!message.contains("AddCustomerID") && message.contains("AddCustomer")) {
								message = message + "," + serverReply;
								String[] params = message.split(",");
								params[0] = "AddCustomerID";
								message = "";
								for (int i = 0; i < params.length; i++) {
									message += params[i];
									if (i + 1 != params.length) message += ",";
								}
							}

							//add customer to flight
							Socket secondsocket = new Socket("lab2-30", 4042);
							String serverReply2 = null;
							PrintWriter outServer2 = new PrintWriter(secondsocket.getOutputStream(), true);
							outServer2.println(message);
							BufferedReader secondin = new BufferedReader(new InputStreamReader(secondsocket.getInputStream()));
							serverReply2 = secondin.readLine();
							secondsocket.close();

							//add customer to cars
							Socket thirdsocket = new Socket("lab2-31", 4042);
							String serverReply3 = null;
							PrintWriter outServer3 = new PrintWriter(thirdsocket.getOutputStream(), true);
							outServer3.println(message);
							BufferedReader thirdin = new BufferedReader(new InputStreamReader(thirdsocket.getInputStream()));
							serverReply3 = thirdin.readLine();
							thirdsocket.close();

							//add customer to rooms
							Socket fourthsocket = new Socket("lab2-32", 4042);
							String serverReply4 = null;
							PrintWriter outServer4 = new PrintWriter(fourthsocket.getOutputStream(), true);
							outServer4.println(message);
							BufferedReader fourthin = new BufferedReader(new InputStreamReader(fourthsocket.getInputStream()));
							serverReply4 = fourthin.readLine();

							//all resourcemanagers successfully added the customer to their "database"
							if (serverReply2.equals(serverReply3) && serverReply2.equals(serverReply4)) {
								PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
								outClient.println(serverReply);
							} else {
								//there was an issue with adding the customer in one of the ressource managers
								String reply = "false";
								PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
								outClient.println(reply);
							}
						} else if (message.contains("ReserveCar")) {
							resource_socket = new Socket("lab2-31", 4042);
							PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
							outServer.println(message);
							BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
							serverReply = inFromServer.readLine();
							resource_socket.close();
							PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
							outClient.println(serverReply);

						} else if (message.contains("ReserveRoom")) {
							resource_socket = new Socket("lab2-32", 4042);
							PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
							outServer.println(message);
							BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
							serverReply = inFromServer.readLine();
							resource_socket.close();
							PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
							outClient.println(serverReply);


						} else if (message.contains("ReserveFlight")) {
							resource_socket = new Socket("lab2-30", 4042);
							PrintWriter outServer = new PrintWriter(resource_socket.getOutputStream(), true);
							outServer.println(message);
							BufferedReader inFromServer = new BufferedReader(new InputStreamReader(resource_socket.getInputStream()));
							serverReply = inFromServer.readLine();
							resource_socket.close();
							PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
							outClient.println(serverReply);

						} else if (message.contains("Bundle")) {
							String[] params = message.split(",");
							int len = params.length;
							int numFlights = params.length - 6;
							String m1, m2, m3;
							boolean car = Boolean.valueOf(params[len - 2]);
							boolean room = Boolean.valueOf(params[len - 1]);
							ArrayList<String> succ = new ArrayList<String>();
							;

							for (int i = 0; i < numFlights; i++) {
								m1 = "ReserveFlight" + "," + params[1] + "," + params[2] + "," + params[i + 3];
								Socket secondsocket = new Socket("lab2-30", 4042);
								String serverReply2 = null;
								PrintWriter outServer2 = new PrintWriter(secondsocket.getOutputStream(), true);
								outServer2.println(m1);
								BufferedReader secondin = new BufferedReader(new InputStreamReader(secondsocket.getInputStream()));
								serverReply2 = secondin.readLine();
								succ.add(serverReply2);
								secondsocket.close();
							}

							//reserve all three in the flight
							if (car) {
								m2 = "ReserveCar" + "," + params[1] + "," + params[2] + "," + params[len - 3];
								//reserve all three if any in the car
								Socket thirdsocket = new Socket("lab2-31", 4042);
								String serverReply3 = null;
								PrintWriter outServer3 = new PrintWriter(thirdsocket.getOutputStream(), true);
								outServer3.println(m2);
								BufferedReader thirdin = new BufferedReader(new InputStreamReader(thirdsocket.getInputStream()));
								serverReply3 = thirdin.readLine();
								succ.add(serverReply3);
								thirdsocket.close();
							}
							if (room) {
								m3 = "ReserveRoom" + "," + params[1] + "," + params[2] + "," + params[len - 3];
								//reserve all three in rooms
								Socket fourthsocket = new Socket("lab2-32", 4042);
								String serverReply4 = null;
								PrintWriter outServer4 = new PrintWriter(fourthsocket.getOutputStream(), true);
								outServer4.println(m3);
								BufferedReader fourthin = new BufferedReader(new InputStreamReader(fourthsocket.getInputStream()));
								serverReply4 = fourthin.readLine();
								succ.add(serverReply4);
								fourthsocket.close();
							}
							//all resourcemanagers successfully added the customer to their "database"
							String reply = "false";
							for (String var : succ) {
								if (!Boolean.valueOf(var)) {
									PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
									outClient.println(reply);
								}
							}
							PrintWriter outClient = new PrintWriter(socket.getOutputStream(), true);
							reply = "true";
							outClient.println(reply);
						}
					}
				}
			}
			catch(IOException e){
					System.out.println("ERROR IN SERVERSOCKETTHREAD");
					e.printStackTrace();
				}

			}

		public void abort(int xid)
		{
			ArrayList<String> undo = all_txn.get(xid);
			String server = "";
			for(String command : undo)
			{
				try {
					String[] separated = command.split(",");
					int size = separated.length;

					//we are not interested in undoing queries
					//and reservations for now
					if (!separated[0].contains("Query") && !separated[0].contains("Reserve")) {
						//for adds, we undo by doing it with negative value
						if (separated[0].contains("Add")) {
							int val = Integer.parseInt(separated[3]);
							//reverse the value which was at numberofX
							val = val * -1;
							separated[3] = Integer.toString(val);
							String new_response = String.join(",", separated);

							if (separated[0].contains("Flight")) {
								server = "lab2-30";
							}
							if (separated[0].contains("Cars")) {
								server = "lab2-31";
							}
							if (separated[0].contains("Rooms")) {
								server = "lab2-32";
							}

							Socket thesocket1 = new Socket(server, 4042);
							PrintWriter outToServer = new PrintWriter(thesocket1.getOutputStream(), true);
							outToServer.println(new_response);
							BufferedReader newin = new BufferedReader(new InputStreamReader(thesocket1.getInputStream()));
							String sreply = newin.readLine();
						}
					} else if (separated[0].contains("Delete") && separated[0].contains("Undo") && !separated[0].contains("Customer")) {
						//we need to transform the DeleteXUndo into AddX
						if (separated[0].contains("Flight")) {
							separated[0] = "AddFlight";
							server = "lab2-30";
						}
						if (separated[0].contains("Cars")) {
							separated[0] = "AddCars";
							server = "lab2-31";
						}
						if (separated[0].contains("Rooms")) {
							separated[0] = "AddRooms";
							server = "lab2-32";
						}
						String response = String.join(",", separated);
						Socket thesocket = new Socket(server, 4042);
						PrintWriter outToServer = new PrintWriter(thesocket.getOutputStream(), true);
						outToServer.println(response);
						BufferedReader newin = new BufferedReader(new InputStreamReader(thesocket.getInputStream()));
						String sreply = newin.readLine();

						//need to considerboth reserves and customers now
					} else {

					}
				}
				catch(IOException e){

				}
			}
		}

	}

	public void runServerThread() throws IOException{
  
    	ServerSocket serverSocket = new ServerSocket(4042);
    	while (true){
      		Socket socket = serverSocket.accept();
			new ServerSocketThread(socket).start();
    	}
  	}
}