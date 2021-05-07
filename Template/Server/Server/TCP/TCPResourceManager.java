package Server.TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
//import java.rmi.RemoteException;
///import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Vector;

import Server.Common.*;
import Server.LockManager.*;


public class TCPResourceManager extends Thread{
	
	protected String m_name = "";
	protected Server.Common.RMHashMap m_data = new Server.Common.RMHashMap();
	
	public TCPResourceManager()
	{

	}
	
	
	public static void main(String args[]) {
		
		TCPResourceManager server = new TCPResourceManager();
		
		try {
			//comment this line and uncomment the next one to run in multiple threads.
			// server.runServer();
			server.runServerThread();
	    }
	    catch (IOException e)
	    {
			e.printStackTrace();
		}
	}
	
	class serverSocketThread extends Thread{
		
		Socket socket;
		serverSocketThread (Socket socket){ this.socket=socket; }
		
		
		public void run()
		  {
			String res = null;
		    try
		    {
		      BufferedReader inFromClient= new BufferedReader(new InputStreamReader(socket.getInputStream()));
		      PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
		      String message = null;
		      message = inFromClient.readLine();

		      System.out.println("message:" + message);

		    
		      String[] params =  message.split(",");
		      int len = params.length;

		      switch (params[0])
		      {
			      case "AddFlight": 
			      {
			    	  res = String.valueOf(TCPResourceManager.this.addFlight(Integer.parseInt(params[1]), Integer.parseInt(params[2]) , Integer.parseInt(params[3]), Integer.parseInt(params[4])));
			    	  break;
			      }
			      case "AddCars":
			      {
			    	  res = String.valueOf(TCPResourceManager.this.addCars(Integer.parseInt(params[1]), params[2] , Integer.parseInt(params[3]), Integer.parseInt(params[4])));
			      			    	  break;
			      }
			      case "AddRooms":
			      {
			    	  res = String.valueOf(TCPResourceManager.this.addRooms(Integer.parseInt(params[1]), params[2] , Integer.parseInt(params[3]), Integer.parseInt(params[4])));
			      			    	  break;
			      }
			      case "AddCustomer" :
			      {
			    	  res = Integer.toString(TCPResourceManager.this.newCustomer(Integer.parseInt(params[1])));
			      			    	  break;
			      }
			      case "AddCustomerID" :
			      {
			    	  res =  String.valueOf(TCPResourceManager.this.newCustomer(Integer.parseInt(params[1]), Integer.parseInt(params[2])));
			      			    	  break;
			      }
			      case "DeleteFlight":
			      {
			    	  res =  String.valueOf(TCPResourceManager.this.deleteFlight(Integer.parseInt(params[1]), Integer.parseInt(params[2])));
			      			    	  break;
			      }
			      case "DeleteCars":
			      {
			    	  res =  String.valueOf(TCPResourceManager.this.deleteCars(Integer.parseInt(params[1]), params[2]));
			      			    	  break;
			      }
			      case "DeleteRooms" :
			      {
			    	  res =  String.valueOf(TCPResourceManager.this.deleteRooms(Integer.parseInt(params[1]),params[2]));
			      			    	  break;
			      }
			      case "DeleteCustomer":
			      {
			    	  res =  String.valueOf(TCPResourceManager.this.deleteCustomer(Integer.parseInt(params[1]), Integer.parseInt(params[2])));
			      			    	  break;
			      }
			      case "QueryFlight":
			      {
			    	  res = Integer.toString(TCPResourceManager.this.queryFlight(Integer.parseInt(params[1]), Integer.parseInt(params[2])));
			      			    	  break;
			      }
			      case "QueryCars" :
			      {
			    	  res = Integer.toString(TCPResourceManager.this.queryCars(Integer.parseInt(params[1]), params[2]));
			      			    	  break;
			      }
			      case "QueryRooms":
			      {
			    	  res = Integer.toString(TCPResourceManager.this.queryRooms(Integer.parseInt(params[1]), params[2]));
			      			    	  break;
			      }
			      case "QueryCustomer":
			      {
			    	  res = TCPResourceManager.this.queryCustomerInfo(Integer.parseInt(params[1]), Integer.parseInt(params[2]));
			      			    	  break;
			      }
			      case "QueryFlightPrice":
			      {
			    	  res = Integer.toString(TCPResourceManager.this.queryFlightPrice(Integer.parseInt(params[1]), Integer.parseInt(params[2])));
			      			    	  break;
			      }
			      case "QueryCarsPrice" :
			      {
			    	  res = Integer.toString(TCPResourceManager.this.queryCarsPrice(Integer.parseInt(params[1]), params[2]));
			      			    	  break;
			      }
			      case "QueryRoomsPrice" :
			      {
			    	  res = Integer.toString(TCPResourceManager.this.queryRoomsPrice(Integer.parseInt(params[1]), params[2]));
			      			    	  break;
			      }
			      case "ReserveFlight":
			      {
			    	  res = String.valueOf(TCPResourceManager.this.reserveFlight(Integer.parseInt(params[1]), Integer.parseInt(params[2]) , Integer.parseInt(params[3])));
			      			    	  break;
			      }
			      case "ReserveCar":
			      {
			    	  res = String.valueOf(TCPResourceManager.this.reserveCar(Integer.parseInt(params[1]), Integer.parseInt(params[2]), params[3]));
			      			    	  break;
			      }
			      case "ReserveRoom":
			      {
			    	  res = String.valueOf(TCPResourceManager.this.reserveRoom(Integer.parseInt(params[1]), Integer.parseInt(params[2]), params[3]));
			      			    	  break;
			      }
				  case "Commit":
				  {

				  	break;
				  }
				  case "Abort":
				  {

				  	break;
				  }
		      }
		      outToClient.println(res);
		      socket.close();
		    }
		    catch (IOException e) {}
		  }
	}


	  public void runServerThread() throws IOException
	  {
	    ServerSocket serverSocket = new ServerSocket(4042);
	    System.out.println("Server ready...");
	    while (true)
	    {
	      Socket socket=serverSocket.accept();
	      new serverSocketThread(socket).start();
	    }
	  }
	


	// Reads a data item
		protected RMItem readData(int xid, String key)
		{
			synchronized(m_data) {
				RMItem item = m_data.get(key);
				if (item != null) {
					return (RMItem)item.clone();
				}
				return null;
			}
		}

		// Writes a data item
		protected void writeData(int xid, String key, RMItem value)
		{
			synchronized(m_data) {
				m_data.put(key, value);
			}
		}

		// Remove the item out of storage
		protected void removeData(int xid, String key)
		{
			synchronized(m_data) {
				m_data.remove(key);
			}
		}

		// Deletes the encar item
		protected boolean deleteItem(int xid, String key)
		{
			Trace.info("RM::deleteItem(" + xid + ", " + key + ") called");
			ReservableItem curObj = (ReservableItem)readData(xid, key);
			// Check if there is such an item in the storage
			if (curObj == null)
			{
				Trace.warn("RM::deleteItem(" + xid + ", " + key + ") failed--item doesn't exist");
				return false;
			}
			else
			{
				if (curObj.getReserved() == 0)
				{
					removeData(xid, curObj.getKey());
					Trace.info("RM::deleteItem(" + xid + ", " + key + ") item deleted");
					return true;
				}
				else
				{
					Trace.info("RM::deleteItem(" + xid + ", " + key + ") item can't be deleted because some customers have reserved it");
					return false;
				}
			}
		}

		// Query the number of available seats/rooms/cars
		protected int queryNum(int xid, String key)
		{
			Trace.info("RM::queryNum(" + xid + ", " + key + ") called");
			ReservableItem curObj = (ReservableItem)readData(xid, key);
			int value = 0;  
			if (curObj != null)
			{
				value = curObj.getCount();
			}
			Trace.info("RM::queryNum(" + xid + ", " + key + ") returns count=" + value);
			return value;
		}    

		// Query the price of an item
		protected int queryPrice(int xid, String key)
		{
			Trace.info("RM::queryPrice(" + xid + ", " + key + ") called");
			ReservableItem curObj = (ReservableItem)readData(xid, key);
			int value = 0; 
			if (curObj != null)
			{
				value = curObj.getPrice();
			}
			Trace.info("RM::queryPrice(" + xid + ", " + key + ") returns cost=$" + value);
			return value;        
		}

		// Reserve an item
		protected boolean reserveItem(int xid, int customerID, String key, String location)
		{
			Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );        
			// Read customer object if it exists (and read lock it)
			Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
			if (customer == null)
			{
				Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
				return false;
			} 

			// Check if the item is available
			ReservableItem item = (ReservableItem)readData(xid, key);
			if (item == null)
			{
				Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
				return false;
			}
			else if (item.getCount() == 0)
			{
				Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--No more items");
				return false;
			}
			else
			{            
				customer.reserve(key, location, item.getPrice());        
				writeData(xid, customer.getKey(), customer);

				// Decrease the number of available items in the storage
				item.setCount(item.getCount() - 1);
				item.setReserved(item.getReserved() + 1);
				writeData(xid, item.getKey(), item);

				Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
				return true;
			}        
		}

		// Create a new flight, or add seats to existing flight
		// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
		public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
		{
			Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
			Flight curObj = (Flight)readData(xid, Flight.getKey(flightNum));
			if (curObj == null)
			{
				// Doesn't exist yet, add it
				Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
				writeData(xid, newObj.getKey(), newObj);
				Trace.info("RM::addFlight(" + xid + ") created new flight " + flightNum + ", seats=" + flightSeats + ", price=$" + flightPrice);
			}
			else
			{
				// Add seats to existing flight and update the price if greater than zero
				curObj.setCount(curObj.getCount() + flightSeats);
				if (flightPrice > 0)
				{
					curObj.setPrice(flightPrice);
				}
				writeData(xid, curObj.getKey(), curObj);
				Trace.info("RM::addFlight(" + xid + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice);
			}
			return true;
		}

		// Create a new car location or add cars to an existing location
		// NOTE: if price <= 0 and the location already exists, it maintains its current price
		public boolean addCars(int xid, String location, int count, int price)
		{
			Trace.info("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
			Car curObj = (Car)readData(xid, Car.getKey(location));
			if (curObj == null)
			{
				// Car location doesn't exist yet, add it
				Car newObj = new Car(location, count, price);
				writeData(xid, newObj.getKey(), newObj);
				Trace.info("RM::addCars(" + xid + ") created new location " + location + ", count=" + count + ", price=$" + price);
			}
			else
			{
				// Add count to existing car location and update price if greater than zero
				curObj.setCount(curObj.getCount() + count);
				if (price > 0)
				{
					curObj.setPrice(price);
				}
				writeData(xid, curObj.getKey(), curObj);
				Trace.info("RM::addCars(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
			}
			return true;
		}

		// Create a new room location or add rooms to an existing location
		// NOTE: if price <= 0 and the room location already exists, it maintains its current price
		public boolean addRooms(int xid, String location, int count, int price)
		{
			Trace.info("RM::addRooms(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
			Room curObj = (Room)readData(xid, Room.getKey(location));
			if (curObj == null)
			{
				// Room location doesn't exist yet, add it
				Room newObj = new Room(location, count, price);
				writeData(xid, newObj.getKey(), newObj);
				Trace.info("RM::addRooms(" + xid + ") created new room location " + location + ", count=" + count + ", price=$" + price);
			} else {
				// Add count to existing object and update price if greater than zero
				curObj.setCount(curObj.getCount() + count);
				if (price > 0)
				{
					curObj.setPrice(price);
				}
				writeData(xid, curObj.getKey(), curObj);
				Trace.info("RM::addRooms(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
			}
			return true;
		}

		// Deletes flight
		public boolean deleteFlight(int xid, int flightNum) 
		{
			return deleteItem(xid, Flight.getKey(flightNum));
		}

		// Delete cars at a location
		public boolean deleteCars(int xid, String location)
		{
			return deleteItem(xid, Car.getKey(location));
		}

		// Delete rooms at a location
		public boolean deleteRooms(int xid, String location)
		{
			return deleteItem(xid, Room.getKey(location));
		}

		// Returns the number of empty seats in this flight
		public int queryFlight(int xid, int flightNum)
		{
			return queryNum(xid, Flight.getKey(flightNum));
		}

		// Returns the number of cars available at a location
		public int queryCars(int xid, String location)
		{
			return queryNum(xid, Car.getKey(location));
		}

		// Returns the amount of rooms available at a location
		public int queryRooms(int xid, String location) 
		{
			return queryNum(xid, Room.getKey(location));
		}

		// Returns price of a seat in this flight
		public int queryFlightPrice(int xid, int flightNum) 
		{
			return queryPrice(xid, Flight.getKey(flightNum));
		}

		// Returns price of cars at this location
		public int queryCarsPrice(int xid, String location)
		{
			return queryPrice(xid, Car.getKey(location));
		}

		// Returns room price at this location
		public int queryRoomsPrice(int xid, String location)
		{
			return queryPrice(xid, Room.getKey(location));
		}

		public String queryCustomerInfo(int xid, int customerID) 
		{
			Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ") called");
			Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
			if (customer == null)
			{
				Trace.warn("RM::queryCustomerInfo(" + xid + ", " + customerID + ") failed--customer doesn't exist");
				// NOTE: don't change this--WC counts on this value indicating a customer does not exist...
				return "";
			}
			else
			{
				Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ")");
				System.out.println(customer.getBill());
				return customer.getBill();
			}
		}

		public int newCustomer(int xid) 
		{
	        	Trace.info("RM::newCustomer(" + xid + ") called");
			// Generate a globally unique ID for the new customer
			int cid = Integer.parseInt(String.valueOf(xid) +
				String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf(Math.round(Math.random() * 100 + 1)));
			Customer customer = new Customer(cid);
			writeData(xid, customer.getKey(), customer);
			Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
			return cid;
		}

		public boolean newCustomer(int xid, int customerID) 
		{
			Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") called");
			Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
			if (customer == null)
			{
				customer = new Customer(customerID);
				writeData(xid, customer.getKey(), customer);
				Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") created a new customer");
				return true;
			}
			else
			{
				Trace.info("INFO: RM::newCustomer(" + xid + ", " + customerID + ") failed--customer already exists");
				return false;
			}
		}

		public boolean deleteCustomer(int xid, int customerID)
		{
			Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");
			Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
			if (customer == null)
			{
				Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
				return false;
			}
			else
			{            
				// Increase the reserved numbers of all reservable items which the customer reserved. 
	 			RMHashMap reservations = customer.getReservations();
				for (String reservedKey : reservations.keySet())
				{        
					ReservedItem reserveditem = customer.getReservedItem(reservedKey);
					Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
					ReservableItem item  = (ReservableItem)readData(xid, reserveditem.getKey());
					Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " which is reserved " +  item.getReserved() +  " times and is still available " + item.getCount() + " times");
					item.setReserved(item.getReserved() - reserveditem.getCount());
					item.setCount(item.getCount() + reserveditem.getCount());
					writeData(xid, item.getKey(), item);
				}

				// Remove the customer from the storage
				removeData(xid, customer.getKey());
				Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
				return true;
			}
		}

		// Adds flight reservation to this customer
		public boolean reserveFlight(int xid, int customerID, int flightNum) 
		{
			return reserveItem(xid, customerID,
					Flight.getKey(flightNum), String.valueOf(flightNum));
		}

		// Adds car reservation to this customer
		public boolean reserveCar(int xid, int customerID, String location)
		{
			return reserveItem(xid, customerID, Car.getKey(location), location);
		}

		// Adds room reservation to this customer
		public boolean reserveRoom(int xid, int customerID, String location)
		{
			return reserveItem(xid, customerID, Room.getKey(location), location);
		}

}
