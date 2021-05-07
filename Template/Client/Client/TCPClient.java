package Client;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

//import java.rmi.ServerException;
//import java.rmi.UnmarshalException;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;


public class TCPClient extends Thread {
	//Lab number for the Middleware
	private static String s_serverHost = "localhost";
	private int transaction_counter = 0;
	private ArrayList<Integer> active_txn = new ArrayList<>();
	
	
	public TCPClient()
	{
		super();
	}
	public static void main(String args[])
	{	
		if (args.length > 0)
		{
			s_serverHost = args[0];
		}
		TCPClient x = new TCPClient();
		x.start();
	}

	public void start()
	{
		// Prepare for reading commands
		System.out.println();
		System.out.println("Location \"help\" for list of supported commands");

		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			// Read the next command
			String command = "";
			Vector<String> arguments = new Vector<String>();
			try {
				System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
				command = stdin.readLine().trim();
			}
			catch (IOException io) {
				System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
				io.printStackTrace();
				System.exit(1);
			}

			try {
				arguments = parse(command);
				Command cmd = Command.fromString((String)arguments.elementAt(0));
				//execute command
				execute(cmd, arguments);
				
			}
			catch (IllegalArgumentException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
			}
			catch (ConnectException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mConnection to server lost");
			}
			catch (Exception e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
				e.printStackTrace();
			}
		}
		
		
	}

	public static Vector<String> parse(String command)
	{
		Vector<String> arguments = new Vector<String>();
		StringTokenizer tokenizer = new StringTokenizer(command,",");
		String argument = "";
		while (tokenizer.hasMoreTokens())
		{
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		}
		return arguments;
	}

	public void execute(Command cmd, Vector<String> arguments) throws NumberFormatException, IOException
	{
		String message = "";
		Socket socket = new Socket(s_serverHost, 4042);
        System.out.println("Connected!");
        
        
        // get the output stream from the socket.
        OutputStream outputStream = (OutputStream) socket.getOutputStream();
        // create a data output stream from the output stream so we can send data through it
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		
		switch (cmd)
		{
			case Start:{
				checkArgumentsCount(1, arguments.size());

				message = "Start";
				System.out.println("Start called!");

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);

				//Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();

				System.out.println("New transaction ID = " + serverReply + "\n");


				this.active_txn.add(Integer.parseInt(serverReply));
				System.out.print("Active transaction ids in the client: [");
				int c = 1;
				for (int i : active_txn) {
					System.out.print(i);
					if(c != active_txn.size()){
						System.out.print(",");
					}
					c++;
				}
				System.out.println("]\n");
				break;
			}
			case Commit:{
				checkArgumentsCount(2, arguments.size());

				System.out.println("Commit transaction with xid = " + arguments.elementAt(1) + "\n");

				int xid = Integer.parseInt(arguments.elementAt(1));


				message = "Commit," + xid;
				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);

				//Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();

				if (toBoolean(serverReply) == true) {
					System.out.println("Commit xid = " + xid + " successful\n");
					this.active_txn.remove(new Integer(xid));
				} else {
					this.active_txn.remove(new Integer(xid));
					System.out.println("Commit xid = " + xid + " NOT successful, xid does not exists.\n");
				}
				System.out.print("Active transaction ids in the client: [");
				int c = 1;
				for (int i : active_txn) {
					System.out.print(i);
					if(c != active_txn.size()){
						System.out.print(",");
					}
					c++;
				}
				System.out.println("]\n");
				break;
			}
			case Abort:{
				checkArgumentsCount(2, arguments.size());

				System.out.println("Abort transaction with xid = " + arguments.elementAt(1) + "\n");

				int xid = Integer.parseInt(arguments.elementAt(1));
				this.active_txn.remove(new Integer(xid));

				message = "Commit," + xid;
				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);

				System.out.println("Aborted transaction with xid = " + xid + "\n");
				System.out.println("Active transaction ids: " + active_txn.toArray() + "\n");
				break;
			}
			case Help:
			{
				if (arguments.size() == 1) {
					System.out.println(Command.description());
				} else if (arguments.size() == 2) {
					Command l_cmd = Command.fromString((String)arguments.elementAt(1));
					System.out.println(l_cmd.toString());
				} else {
					System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mImproper use of help command. Location \"help\" or \"help,<CommandName>\"");
				}
				break;
			}
			case AddFlight: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding a new flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));
				System.out.println("-Flight Seats: " + arguments.elementAt(3));
				System.out.println("-Flight Price: " + arguments.elementAt(4));

				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));
				int flightSeats = toInt(arguments.elementAt(3));
				int flightPrice = toInt(arguments.elementAt(4));

				message = "AddFlight" + "," + id + "," + flightNum + "," + flightSeats + "," + flightPrice;
				// write the message we want to send

                PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
                outServer.println(message);

		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				
				if (toBoolean(serverReply) == true) {
					System.out.println("Flight added");
				} else {
					System.out.println("Flight could not be added");
				}
				break;
			}
			case AddCars: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding new cars [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				System.out.println("-Number of Cars: " + arguments.elementAt(3));
				System.out.println("-Car Price: " + arguments.elementAt(4));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				int numCars = toInt(arguments.elementAt(3));
				int price = toInt(arguments.elementAt(4));

				
				message = "AddCars" + "," + id + "," + location + "," + numCars + "," + price;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				
				if (toBoolean(serverReply) == true) {
					System.out.println("Cars added");
				} else {
					System.out.println("Cars could not be added");
				}
				break;
			}
			case AddRooms: {
				checkArgumentsCount(5, arguments.size());

				System.out.println("Adding new rooms [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));
				System.out.println("-Number of Rooms: " + arguments.elementAt(3));
				System.out.println("-Room Price: " + arguments.elementAt(4));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				int numRooms = toInt(arguments.elementAt(3));
				int price = toInt(arguments.elementAt(4));

				message = "AddRooms" + "," + id + "," + location + "," + numRooms + "," + price;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				
				if (toBoolean(serverReply) == true) {
					System.out.println("Rooms added");
				} else {
					System.out.println("Rooms could not be added");
				}

				break;
			}
			case AddCustomer: {
				checkArgumentsCount(2, arguments.size());

				System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");

				int id = toInt(arguments.elementAt(1));
				
				message = "AddCustomer" + "," + id;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();

				System.out.println("Add customer ID: " + serverReply);
				break;
			}
			case AddCustomerID: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));

				message = "AddCustomerID" + "," + id + "," + customerID;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				
				if (toBoolean(serverReply) == true) {
					System.out.println("Add customer ID: " + customerID);
				} else {
					System.out.println("Customer could not be added");
				}
				break;
			}
			case DeleteFlight: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));


				message = "DeleteFlight" + "," + id + "," + flightNum;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				
				if (toBoolean(serverReply) == true) {
					System.out.println("Flight Deleted");
				} else {
					System.out.println("Flight could not be deleted");
				}
				
				break;
			}
			case DeleteCars: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting all cars at a particular location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);


				message = "DeleteCars" + "," + id + "," + location;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				if (toBoolean(serverReply) == true) {
					System.out.println("Cars Deleted");
				} else {
					System.out.println("Cars could not be deleted");
				}

				break;
			}
			case DeleteRooms: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting all rooms at a particular location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);


				message = "DeleteRooms" + "," + id + "," + location;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				
				if (toBoolean(serverReply) == true) {
					System.out.println("Rooms Deleted");
				} else {
					System.out.println("Rooms could not be deleted");
				}
				break;
			}
			case DeleteCustomer: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Deleting a customer from the database [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));


				message = "DeleteCustomer" + "," + id + "," + customerID;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();

				if (toBoolean(serverReply) == true) {
					System.out.println("Customer Deleted");
				} else {
					System.out.println("Customer could not be deleted");
				}
				break;
			}
			case QueryFlight: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));
				
				message = "QueryFlight" + "," + id + "," + flightNum;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				if(!serverReply.equals("deadlock")) {
					System.out.println("Number of seats available: " + serverReply);
				}
				else{
					System.out.println("Cannot read due to deadlock");
				}
				break;
			}
			case QueryCars: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying cars location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				
				message = "QueryCars" + "," + id + "," + location;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				System.out.println("Number of cars at this location: " + serverReply);
				break;
			}
			case QueryRooms: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying rooms location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);
				
				message = "QueryRooms" + "," + id + "," + location;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				System.out.println("Number of rooms at this location: " + serverReply);
				break;
			}
			case QueryCustomer: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying customer information [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
			
				message = "QueryCustomer" + "," + id + "," + customerID;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				while(serverReply != null){
					System.out.println(serverReply);
					serverReply = inFromServer.readLine();
				}
				break;               
			}
			case QueryFlightPrice: {
				checkArgumentsCount(3, arguments.size());
				
				System.out.println("Querying a flight price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));

				message = "QueryFlightPrice" + "," + id + "," + flightNum;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				if(!serverReply.equals("deadlock")) {
					System.out.println("Price of a seat: " + serverReply);
				}
				else{
					System.out.println("Cannot read due to deadlock");
				}

				break;
			}
			case QueryCarsPrice: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying cars price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);


				message = "QueryCarsPrice" + "," + id + "," + location;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				System.out.println("Price of cars at this location: " + serverReply);
				break;
			}
			case QueryRoomsPrice: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying rooms price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);


				message = "QueryRoomsPrice" + "," + id + "," + location;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				System.out.println("Price of rooms at this location: " + serverReply);
				break;
			}
			case ReserveFlight: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving seat in a flight [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Flight Number: " + arguments.elementAt(3));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
				int flightNum = toInt(arguments.elementAt(3));


				message = "ReserveFlight" + "," + id + "," + customerID + "," + flightNum;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				
				if (toBoolean(serverReply) == true) {
					System.out.println("Flight Reserved");
				} else {
					System.out.println("Flight could not be reserved");
				}
				break;
			}
			case ReserveCar: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving a car at a location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Car Location: " + arguments.elementAt(3));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
				String location = arguments.elementAt(3);


				message = "ReserveCar" + "," + id + "," + customerID + "," + location;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				
				if (toBoolean(serverReply) == true) {
					System.out.println("Car Reserved");
				} else {
					System.out.println("Car could not be reserved");
				}
				break;
			}
			case ReserveRoom: {
				checkArgumentsCount(4, arguments.size());

				System.out.println("Reserving a room at a location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				System.out.println("-Room Location: " + arguments.elementAt(3));
				
				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
				String location = arguments.elementAt(3);


				message = "ReserveRoom" + "," + id + "," + customerID + "," + location;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				if (toBoolean(serverReply) == true) {
					System.out.println("Room Reserved");
				} else {
					System.out.println("Room could not be reserved");
				}
				break;
			}
			case Bundle: {
				if (arguments.size() < 7) {
					System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mBundle command expects at least 7 arguments. Location \"help\" or \"help,<CommandName>\"");
					break;
				}

				System.out.println("Reserving an bundle [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));
				for (int i = 0; i < arguments.size() - 6; ++i)
				{
					System.out.println("-Flight Number: " + arguments.elementAt(3+i));
				}
				System.out.println("-Location for Car/Room: " + arguments.elementAt(arguments.size()-3));
				System.out.println("-Book Car: " + arguments.elementAt(arguments.size()-2));
				System.out.println("-Book Room: " + arguments.elementAt(arguments.size()-1));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));
				Vector<String> flightNumbers = new Vector<String>();
				for (int i = 0; i < arguments.size() - 6; ++i)
				{
					flightNumbers.addElement(arguments.elementAt(3+i));
				}
				String location = arguments.elementAt(arguments.size()-3);
				boolean car = toBoolean(arguments.elementAt(arguments.size()-2));
				boolean room = toBoolean(arguments.elementAt(arguments.size()-1));
				message = "Bundle" + "," + id + "," + customerID;
				for(String flight : flightNumbers) {
					message = message +","+ flight;
				}
				message = message + "," + location + "," + car + "," + room ;

				PrintWriter outServer = new PrintWriter(socket.getOutputStream(),true);
				outServer.println(message);
		        
		        //Unlock
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String serverReply = inFromServer.readLine();
				if (toBoolean(serverReply) == true) {
					System.out.println("Bundle Reserved");
				} else {
					System.out.println("Bundle could not be reserved");
				}

				break;
			}
			case Quit:
				checkArgumentsCount(1, arguments.size());

				System.out.println("Quitting client");
		        dataOutputStream.close();
				System.exit(0);
				
		}
	}

	public static void checkArgumentsCount(Integer expected, Integer actual) throws IllegalArgumentException
	{
		if (expected != actual)
		{
			throw new IllegalArgumentException("Invalid number of arguments. Expected " + (expected - 1) + ", received " + (actual - 1) + ". Location \"help,<CommandName>\" to check usage of this command");
		}
	}

	public static int toInt(String string) throws NumberFormatException
	{
		return (Integer.valueOf(string)).intValue();
	}

	public static boolean toBoolean(String string)// throws Exception
	{
		return (Boolean.valueOf(string)).booleanValue();
	}
	
	
}
