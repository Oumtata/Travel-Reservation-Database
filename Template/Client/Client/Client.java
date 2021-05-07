package Client;

import Server.Interface.*;

import java.util.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.ConnectException;
import java.rmi.ServerException;
import java.rmi.UnmarshalException;

public abstract class Client
{
	IResourceManager m_resourceManager = null;
	private ArrayList<Integer> active_txn = new ArrayList<>();

	public Client()
	{
		super();
	}

	public abstract void connectServer();

	public void start()
	{
		//UNCOMMENT FOR TESTING THE PERFORMANCE PART 1
//		try {
//			File f = new File("addcustomer.txt");
//			f.createNewFile();
//
//			BufferedWriter fr = new BufferedWriter(new FileWriter("addcustomer.txt"));
//
//
//			// Prepare for reading commands
//			System.out.println();
//			System.out.println("Location \"help\" for list of supported commands");
//
//				// Read the next command
//				ArrayList<String> allcommands = new ArrayList<>();
//				System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
//
//				allcommands.add("start");
//
//				for (int i = 0; i < 100; i++) {
//					//String input = "addflight,1," + i + ",1,1";
//					String input = "addcustomerid,1," + i;
//					allcommands.add(input);
//				}
//
//				for (String command : allcommands) {
//					try {
//						Vector<String> arguments = new Vector<>();
//						long StartTime = System.nanoTime();
//						arguments = parse(command);
//						Command cmd = Command.fromString((String) arguments.elementAt(0));
//						try {
//							execute(cmd, arguments);
//						} catch (ConnectException e) {
//							connectServer();
//							execute(cmd, arguments);
//						}
//						long EndTime = System.nanoTime();
//						long ellapsedTime = EndTime - StartTime;
//						fr.write(Long.toString(ellapsedTime));
//						fr.newLine();
//						System.out.println("ellapsed time = " + ellapsedTime + "\n\n");
//					} catch (IllegalArgumentException | ServerException e) {
//						System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27 + "[0m" + e.getLocalizedMessage());
//					} catch (ConnectException | UnmarshalException e) {
//						System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27 + "[0mConnection to server lost");
//					} catch (Exception e) {
//						System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27 + "[0mUncaught exception");
//						e.printStackTrace();
//					}
//				}
//			fr.close();
//		}
//		catch (IOException e){
//			e.printStackTrace();
//		}

//		System.out.println();
//		System.out.println("Location \"help\" for list of supported commands");
//
//		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
//		String command = "";
//		try {
//			System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
//			command = stdin.readLine().trim();
//		}
//		catch (IOException io) {
//			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
//			io.printStackTrace();
//			System.exit(1);
//		}
//		int commandInt = Integer.parseInt(command);
//		ArrayList<String> allcommands = new ArrayList<>();
//		allcommands.add("start");
//
//		File f;
//
//		switch(commandInt){
//			case 1:
//			{
//				f = new File("client1_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,1," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,1");
//				break;
//			}
//			case 2:
//			{
//				f = new File("client2_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,2," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,2");
//				break;
//			}
//			case 3:
//			{
//				f = new File("client3_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,3," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,3");
//				break;
//			}
//			case 4:
//			{
//				f = new File("client4_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,4," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,4");
//				break;
//			}
//			case 5:
//			{
//				f = new File("client5_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,5," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,5");
//				break;
//			}
//			case 6:
//			{
//				f = new File("client6_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,6," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,6");
//				break;
//			}
//			case 7:
//			{
//				f = new File("client7_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,7," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,7");
//				break;
//			}
//			case 8:
//			{
//				f = new File("client8_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,8," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,8");
//				break;
//			}
//			case 9:
//			{
//				f = new File("client9_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,9," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,9");
//				break;
//			}
//			case 10:
//			{
//				f = new File("client10_1pers.txt");
//				for (int i = 0; i < 500; i++) {
//					String input = "addflight,10," + i + ",1,1";
//					allcommands.add(input);
//				}
//				allcommands.add("Commit,10");
//				break;
//			}
//			default:
//			{
//				f = new File("unreachable.txt");
//			}
//		}
//		try {
//			BufferedWriter fr = new BufferedWriter(new FileWriter(f));
//
//			//Loop 50 times for 50 experiments
//			for (int i = 0; i < 20; i++) {
//				long startTime = System.currentTimeMillis();
//				for (String command1 : allcommands) {
//					try {
//						Vector<String> arguments;
//						arguments = parse(command1);
//						Command cmd = Command.fromString(arguments.elementAt(0));
//						try {
//							execute(cmd, arguments);
//						} catch (ConnectException e) {
//							connectServer();
//							execute(cmd, arguments);
//						}
//					} catch (IllegalArgumentException | ServerException e) {
//						System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27 + "[0m" + e.getLocalizedMessage());
//					} catch (ConnectException | UnmarshalException e) {
//						System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27 + "[0mConnection to server lost");
//					} catch (Exception e) {
//						System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27 + "[0mUncaught exception");
//						e.printStackTrace();
//					}
//				}
//				long endTime = System.currentTimeMillis();
//				long ellapsedTime = endTime - startTime;
//				fr.write(Long.toString(ellapsedTime));
//				fr.newLine();
//				if(ellapsedTime<1000){
//					try{
//						Thread.sleep(2000-ellapsedTime);
//					}
//					catch(InterruptedException e){
//						e.printStackTrace();
//					}
//				}
//			}
//			fr.close();
//		}
//		catch (IOException e){
//			e.printStackTrace();
//		}

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
				try {
					execute(cmd, arguments);
				}
				catch (ConnectException e) {
					connectServer();
					execute(cmd, arguments);
				}
			}
			catch (IllegalArgumentException|ServerException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
			}
			catch (ConnectException|UnmarshalException e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mConnection to server lost");
			}
			catch (Exception e) {
				System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
				e.printStackTrace();
			}
		}
	}

	public void execute(Command cmd, Vector<String> arguments) throws RemoteException, NumberFormatException
	{
		switch (cmd)
		{
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
			case Start:
			{
				checkArgumentsCount(1, arguments.size());
				System.out.println("Start called!");

				int xid = m_resourceManager.start();
				System.out.println("New transaction ID = " + xid + "\n");
				this.active_txn.add(xid);
//				System.out.print("Active transaction ids in the client: [");
//				int c = 1;
//				for (int i : active_txn) {
//					System.out.print(i);
//					if(c != active_txn.size()){
//						System.out.print(",");
//					}
//					c++;
//				}
//				System.out.println("]\n");

				break;
			}
			case Commit:
			{
				checkArgumentsCount(2,arguments.size());
				System.out.println("Commit called with xid = " + arguments.elementAt(1));

				int xid = Integer.parseInt(arguments.elementAt(1));
				boolean result = m_resourceManager.commit(xid);

				if (result) {
					System.out.println("Commit xid = " + xid + " successful\n");
					this.active_txn.remove(new Integer(xid));
				} else {
					this.active_txn.remove(new Integer(xid));
					System.out.println("Commit xid = " + xid + " NOT successful, xid does not exists.\n");
				}
//				System.out.print("Active transaction ids in the client: [");
//				int c = 1;
//				for (int i : active_txn) {
//					System.out.print(i);
//					if(c != active_txn.size()){
//						System.out.print(",");
//					}
//					c++;
//				}
//				System.out.println("]\n");
				break;
			}
			case Abort:
			{
				checkArgumentsCount(2,arguments.size());
				System.out.println("Abort called with xid = " + arguments.elementAt(1));

				int xid = Integer.parseInt(arguments.elementAt(1));
				m_resourceManager.abort(xid);

//				System.out.print("Active transaction ids in the client: [");
//				int c = 1;
//				for (int i : active_txn) {
//					System.out.print(i);
//					if(c != active_txn.size()){
//						System.out.print(",");
//					}
//					c++;
//				}
//				System.out.println("]\n");
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

				if (m_resourceManager.addFlight(id, flightNum, flightSeats, flightPrice)) {
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

				if (m_resourceManager.addCars(id, location, numCars, price)) {
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

				if (m_resourceManager.addRooms(id, location, numRooms, price)) {
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
				int customer = m_resourceManager.newCustomer(id);

				if(customer != -1 && customer != -99) {
					System.out.println("Add customer ID: " + customer);
				}
				else if(customer == -99){
					System.out.println("DEADLOCK! Transaction " + id + " aborted");
				}
				else{
					System.out.println("Could not add customer");
				}
				break;
			}
			case AddCustomerID: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));

				if (m_resourceManager.newCustomer(id, customerID)) {
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

				if (m_resourceManager.deleteFlight(id, flightNum)) {
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

				if (m_resourceManager.deleteCars(id, location)) {
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

				if (m_resourceManager.deleteRooms(id, location)) {
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

				if (m_resourceManager.deleteCustomer(id, customerID)) {
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

				int seats = m_resourceManager.queryFlight(id, flightNum);
				if(seats != -1 && seats != -99) {
					System.out.println("Number of seats available: " + seats);
				}
				else if(seats == -99){
					System.out.println("DEADLOCK! Transaction " + id + " aborted");
				}
				else{
					System.out.println("Could not read data");
				}
				break;
			}
			case QueryCars: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying cars location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				int numCars = m_resourceManager.queryCars(id, location);
				if(numCars != -1 && numCars!=-99) {
					System.out.println("Number of cars at this location: " + numCars);
				}
				else if(numCars == -99){
					System.out.println("DEADLOCK! Transaction " + id + " aborted");
				}
				else{
					System.out.println("Could not read data");
				}
				break;
			}
			case QueryRooms: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying rooms location [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));
				
				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				int numRoom = m_resourceManager.queryRooms(id, location);
				if(numRoom != -1 && numRoom!=-99) {
					System.out.println("Number of rooms at this location: " + numRoom);
				}
				else if(numRoom == -99){
					System.out.println("DEADLOCK! Transaction " + id + " aborted");
				}
				else{
					System.out.println("Could not read data");
				}
				break;
			}
			case QueryCustomer: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying customer information [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Customer ID: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int customerID = toInt(arguments.elementAt(2));

				String bill = m_resourceManager.queryCustomerInfo(id, customerID);
				if(!bill.equals("error") && !bill.equals("deadlock")) {
					System.out.print(bill);
				}
				else if(bill.equals("deadlock")){
					System.out.println("DEADLOCK! Transaction " + id + " aborted");
				}
				else{
					System.out.println("Could not read the bill");
				}
				break;               
			}
			case QueryFlightPrice: {
				checkArgumentsCount(3, arguments.size());
				
				System.out.println("Querying a flight price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Flight Number: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				int flightNum = toInt(arguments.elementAt(2));

				int price = m_resourceManager.queryFlightPrice(id, flightNum);
				if(price != -1 && price != -99) {
					System.out.println("Price of a seat: " + price);
				}
				else if(price == -99){
					System.out.println("DEADLOCK! Transaction " + id + " aborted");
				}
				else{
					System.out.println("could not read data");
				}
				break;
			}
			case QueryCarsPrice: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying cars price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Car Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				int price = m_resourceManager.queryCarsPrice(id, location);
				if(price != -1 && price != -99) {
					System.out.println("Price of cars at this location: " + price);
				}
				else if(price == -99){
					System.out.println("DEADLOCK! Transaction " + id + " aborted");
				}
				else{
					System.out.println("could not read data");
				}
				break;
			}
			case QueryRoomsPrice: {
				checkArgumentsCount(3, arguments.size());

				System.out.println("Querying rooms price [xid=" + arguments.elementAt(1) + "]");
				System.out.println("-Room Location: " + arguments.elementAt(2));

				int id = toInt(arguments.elementAt(1));
				String location = arguments.elementAt(2);

				int price = m_resourceManager.queryRoomsPrice(id, location);
				if(price != -1 && price != -99) {
					System.out.println("Price of rooms at this location: " + price);
				}
				else if(price == -99){
					System.out.println("DEADLOCK! Transaction " + id + " aborted");
				}
				else{
					System.out.println("could not read data");
				}
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

				if (m_resourceManager.reserveFlight(id, customerID, flightNum)) {
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

				if (m_resourceManager.reserveCar(id, customerID, location)) {
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

				if (m_resourceManager.reserveRoom(id, customerID, location)) {
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

				if (m_resourceManager.bundle(id, customerID, flightNumbers, location, car, room)) {
					System.out.println("Bundle Reserved");
				} else {
					System.out.println("Bundle could not be reserved");
				}
				break;
			}
			case Shutdown:
			{
				checkArgumentsCount(1, arguments.size());
				System.out.println("Shutdown called!");

				boolean result = m_resourceManager.shutdown();
				if (result){
					System.out.println("Shutdown successful!");
				}
				else{
					System.out.println("Shutdown failed");
				}
				break;
			}
			case Quit:
				checkArgumentsCount(1, arguments.size());

				System.out.println("Quitting client");
				System.exit(0);
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
