package Server.RMI;

import Server.Interface.*;
import Server.Common.*;
import Server.LockManager.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.util.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIMiddleware implements IResourceManager{

    private static String s_serverHostCar = "localhost";
    private static String s_serverHostFlight = "localhost";
    private static String s_serverHostRoom = "localhost";
    private static String s_serverHostCustomer = "localhost";
    private static int s_serverPort = 4042;

//    private static String s_serverName = "Server";
    private static String s_rmiPrefix = "group_42_";

    static IResourceManager flight_resourceManager = null;
    static IResourceManager car_resourceManager = null;
    static IResourceManager room_resourceManager = null;
    static IResourceManager customer_resourceManager = null;

    protected String m_name = "Middleware";

    LockManager lockManager = new LockManager();
    private Map<Integer,ArrayList<String>> all_txn = Collections.synchronizedMap(new HashMap<Integer,ArrayList<String>>());
    private Map<Integer,Long> time_last = Collections.synchronizedMap(new HashMap<Integer,Long>());

    public class TimedExit {
        Timer timer = new Timer();
        TimerTask exitApp = new TimerTask() {
            public void run() {
                System.exit(0);
            }
        };
        public TimedExit() {
            timer.schedule(exitApp, new Date(System.currentTimeMillis()+5*1000));
        }
    }

    public class checkTime{
        Timer timer = new Timer();
        TimerTask wat = new TimerTask() {
            public void run() {
                if(!time_last.isEmpty()) {
                    for(Map.Entry<Integer, Long> entry : time_last.entrySet()) {
                        long live = System.currentTimeMillis();
                        long previous = entry.getValue();
                        long elapsed = live-previous;
                        if(elapsed > 90000) {
                            try {
                                abort(entry.getKey());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        public checkTime() {
            timer.schedule(wat,1000,1000);
        }
    }

    public static void main(String args[]){
        //the args of the main method are the host names, so lab32, lab34 etc... of Flight,Cars and Rooms respectively
        if(args.length > 0) s_serverHostFlight = args[0];
        if(args.length > 1) s_serverHostCar = args[1];
        if(args.length > 2) s_serverHostRoom = args[2];
        if(args.length > 3) s_serverHostCustomer = args[3];
        System.out.println("SERVER HOST NAMES = " + s_serverHostFlight + ", " + s_serverHostCar + ", " + s_serverHostRoom + ", " + s_serverHostCustomer);

        //1 big try catch block that surrounds most things in the main method
        try {
            // Create a new Server object
            RMIMiddleware server = new RMIMiddleware("Middleware");

            // Get a reference to the registry in localhost
            Registry l_registry;
            try {
                l_registry = LocateRegistry.createRegistry(4042);
            } catch (RemoteException e) {
                l_registry = LocateRegistry.getRegistry(4042);
            }
            final Registry registry = l_registry;

            //We want to get a reference to all 3 RM / connect to all 3 RM
            while (true) {
                boolean first1 = true;
                boolean first2 = true;
                boolean first3 = true;
                boolean first4 = true;

                //Boolean to keep track of if all 3 conections were successful
                boolean conn1 = false;
                boolean conn2 = false;
                boolean conn3 = false;
                boolean conn4 = false;

                //Get a reference to the Car RM
                try {
                    Registry registryCar = LocateRegistry.getRegistry(s_serverHostCar, s_serverPort);
                    car_resourceManager = (IResourceManager)registryCar.lookup(s_rmiPrefix + "Car");
                    System.out.println("Connected to Car server [" + s_serverHostCar + ":" + s_serverPort + "/" + s_rmiPrefix + "Car ]");
                    conn1 = true;
                } catch (NotBoundException | RemoteException e) {
                    if (first1) {
                        System.out.println("Waiting for Car server [" + s_serverHostCar + ":" + s_serverPort + "/" + s_rmiPrefix + "Car ]");
                        first1 = false;
                    }
                }
                Thread.sleep(500);
                //Get a reference to the Flight RM
                try {
                    Registry registryFlight = LocateRegistry.getRegistry(s_serverHostFlight, s_serverPort);
                    flight_resourceManager = (IResourceManager)registryFlight.lookup(s_rmiPrefix + "Flight");
                    System.out.println("Connected to Flight server [" + s_serverHostFlight + ":" + s_serverPort + "/" + s_rmiPrefix + "Flight ]");
                    conn2 = true;
                } catch (NotBoundException | RemoteException e) {
                    if (first2) {
                        System.out.println("Waiting for Flight server [" + s_serverHostFlight + ":" + s_serverPort + "/" + s_rmiPrefix + "Flight ]");
                        first2 = false;
                    }
                }
                Thread.sleep(500);
                //Get a reference to the Room RM
                try {
                    Registry registryRoom = LocateRegistry.getRegistry(s_serverHostRoom, s_serverPort);
                    room_resourceManager = (IResourceManager)registryRoom.lookup(s_rmiPrefix + "Room");
                    System.out.println("Connected to Room server [" + s_serverHostRoom + ":" + s_serverPort + "/" + s_rmiPrefix + "Room ]");
                    conn3 = true;
                } catch (NotBoundException | RemoteException e) {
                    if (first3) {
                        System.out.println("Waiting for Room server [" + s_serverHostRoom + ":" + s_serverPort + "/" + s_rmiPrefix + "Room ]");
                        first3 = false;
                    }
                }
                Thread.sleep(500);
                //Get a reference to the Customer RM
                try {
                    Registry registryCustomer = LocateRegistry.getRegistry(s_serverHostCustomer, s_serverPort);
                    customer_resourceManager = (IResourceManager)registryCustomer.lookup(s_rmiPrefix + "Customer");
                    System.out.println("Connected to Customer server [" + s_serverHostCustomer + ":" + s_serverPort + "/" + s_rmiPrefix + "Customer ]");
                    conn4 = true;
                } catch (NotBoundException | RemoteException e) {
                    if (first1) {
                        System.out.println("Waiting for Customer server [" + s_serverHostCustomer + ":" + s_serverPort + "/" + s_rmiPrefix + "Customer ]");
                        first4 = false;
                    }
                }
                Thread.sleep(500);

                //If all 3 servers are connected
                if (conn1 && conn2 && conn3 && conn4) {
                    break;
                }
            }

            //Dynamically generate the stub (client proxy)
            IResourceManager rmMiddleware = (IResourceManager)UnicastRemoteObject.exportObject(server, 0);

            //Rebind the name and store in the registry
            registry.rebind(s_rmiPrefix + "Middleware", rmMiddleware);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        registry.unbind(s_rmiPrefix + "Middleware");
                        System.out.println("Middleware resource manager unbound");
                    }
                    catch(Exception e) {
                        System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }
            });
        }
        catch(Exception e){
            System.out.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }
    }

    //Constructor
    public RMIMiddleware(String name)
    {
        this.m_name = name;
    }

    public int start() throws RemoteException{
        checkTime ct = new checkTime();
        int ret = -1;
        if (!all_txn.keySet().isEmpty()) {
            for(int i = 1; i<=all_txn.size(); i++){
                if(!all_txn.keySet().contains(i)){
                    ret = i;
                }
            }
            if(ret == -1) {
                int max = Collections.max(all_txn.keySet());
                ret = max + 1;
            }
        }
        else
            ret = 1;

        long current = System.currentTimeMillis();
        time_last.put(ret, current);

        ArrayList<String> temp = new ArrayList<>();
        temp.add("Start");
        all_txn.put(ret, temp);

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

        return ret;
    }

    public boolean commit(int xid) throws RemoteException{
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

            return false;
        }
        else {
            System.out.println("HASHMAP DOES CONTAIN XID");
            //Unlock all the locks owned by transaction xid
            lockManager.UnlockAll(xid);
            //Remove xid from the list of active transaction xids
            all_txn.remove(xid);
            time_last.remove(xid);

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

            return true;
        }
    }

    public void abort(int xid) throws RemoteException{
        if(!all_txn.containsKey(xid))
            return;
        try {
            lockManager.UnlockAll(xid);
            ArrayList<String> command = all_txn.get(xid);
            for(int i = command.size()-1; i>=0; i--)
            {
                String cur = command.get(i);
                if(cur.contains("addflight")) {
                    String[] parts = cur.split(",");
                    parts[3] = Integer.toString((Integer.parseInt(parts[3]))*-1);
                    flight_resourceManager.addFlight(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),Integer.parseInt(parts[4]));
                }
                else if(cur.contains("addcars")) {
                    String[] parts = cur.split(",");
                    parts[3] = Integer.toString((Integer.parseInt(parts[3]))*-1);
                    car_resourceManager.addCars(Integer.parseInt(parts[1]),parts[2],Integer.parseInt(parts[3]),Integer.parseInt(parts[4]));
                }
                else if(cur.contains("addrooms")) {
                    String[] parts = cur.split(",");
                    parts[3] = Integer.toString((Integer.parseInt(parts[3]))*-1);
                    room_resourceManager.addRooms(Integer.parseInt(parts[1]),parts[2],Integer.parseInt(parts[3]),Integer.parseInt(parts[4]));
                }
                else if(cur.contains("addcustomerid") || cur.contains("addcustomerundo")){
                    String[] parts = cur.split(",");
                    flight_resourceManager.deleteCustomer(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    room_resourceManager.deleteCustomer(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    car_resourceManager.deleteCustomer(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    customer_resourceManager.deleteCustomer(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                }
                else if(cur.contains("deleteflightundo")) {
                    String[] parts = cur.split(",");
                    flight_resourceManager.addFlight(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]),Integer.parseInt(parts[4]));
                }
                else if(cur.contains("deletecarsundo")) {
                    String[] parts = cur.split(",");
                    car_resourceManager.addCars(Integer.parseInt(parts[1]),parts[2],Integer.parseInt(parts[3]),Integer.parseInt(parts[4]));
                }
                else if(cur.contains("deleteroomsundo")) {
                    String[] parts = cur.split(",");
                    room_resourceManager.addRooms(Integer.parseInt(parts[1]),parts[2],Integer.parseInt(parts[3]),Integer.parseInt(parts[4]));
                }
                else if(cur.contains("reserveflight")){
                    String[] parts = cur.split(",");
                    flight_resourceManager.unReserveFlight(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));
                }
                else if(cur.contains("reservecar")){
                    String[] parts = cur.split(",");
                    car_resourceManager.unReserveCar(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),parts[3]);
                }
                else if(cur.contains("reserveroom")){
                    String[] parts = cur.split(",");
                    room_resourceManager.unReserveRoom(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),parts[3]);
                }
                else if(cur.contains("deletecustomerundo")){
                    String[] parts = cur.split(",");
                    flight_resourceManager.unDeleteCustomer(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    room_resourceManager.unDeleteCustomer(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    customer_resourceManager.unDeleteCustomer(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                    car_resourceManager.unDeleteCustomer(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
                }
                all_txn.remove(xid);
            }
            time_last.remove(xid);
            System.out.println("TRANSACTION " + xid + " ABORTED!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException{
//        long startTime = System.nanoTime();
//        if(!all_txn.containsKey(xid))
//            return false;
//        try {
//            String objectID = "Flight-" + flightNum;
//            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
//            if (lockGranted) {
//                boolean bool = flight_resourceManager.addFlight(xid, flightNum, flightSeats, flightPrice);
//                if (!bool)
//                    return false;
//                String command = "addflight," + xid + "," + flightNum + "," + flightSeats + "," + flightPrice;
//                ArrayList<String> temp = all_txn.get(xid);
//                temp.add(command);
//                all_txn.put(xid, temp);
//                long endTime = System.nanoTime();
//                long ellapsedTime = endTime - startTime;
//                System.out.println("ELLAPSED TIME = " + ellapsedTime);
//                return true;
//            } else
//                return false;
//        }
//        catch(DeadlockException e) {
//            System.out.println("Deadlock Exception");
//            abort(xid);
//            return false;
//        }



        if(!all_txn.containsKey(xid))
            return false;

        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Flight-" + flightNum;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                boolean bool = flight_resourceManager.addFlight(xid, flightNum, flightSeats, flightPrice);
                if(!bool)
                    return false;
                String command = "addflight," + xid + "," + flightNum + "," + flightSeats + "," + flightPrice;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return true;
            }
            else
                return false;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    public boolean addCars(int xid, String location, int count, int price) throws RemoteException{
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Car-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                boolean bool = car_resourceManager.addCars(xid, location, count, price);
                if (!bool)
                    return false;
                String command = "addcars," + xid + "," + location + "," + count + "," + price;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return true;
            }
            else
                return false;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    public boolean addRooms(int xid, String location, int count, int price) throws RemoteException{
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Room-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                boolean bool = room_resourceManager.addRooms(xid, location, count, price);
                if(!bool)
                    return false;
                String command = "addrooms," + xid + "," + location + "," + count + "," + price;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return true;
            }
            else
                return false;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    public boolean deleteFlight(int xid, int flightNum) throws RemoteException {
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Flight-" + flightNum;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                //Query info for undo operations
                int seats = flight_resourceManager.queryFlight(xid,flightNum);
                int price = flight_resourceManager.queryFlightPrice(xid, flightNum);

                boolean bool = flight_resourceManager.deleteFlight(xid, flightNum);

                if(!bool)
                    return false;
                String command = "deleteflight," + xid + "," + flightNum;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);

                String undo = "deleteflightundo," + xid + "," + flightNum + "," + seats + "," + price;
                temp.add(undo);
                all_txn.put(xid,temp);

                printhashmap();
                return true;
            }
            else
                return false;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    public boolean deleteCars(int xid, String location) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Car-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                //Query info for undo operations
                int seats = car_resourceManager.queryCars(xid,location);
                int price = car_resourceManager.queryCarsPrice(xid, location);

                boolean bool = car_resourceManager.deleteCars(xid, location);
                if(!bool)
                    return false;

                String command = "deletecars," + xid + "," + location;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);

                String undo = "deletecarsundo," + xid + "," + location + "," + seats + "," + price;
                temp.add(undo);
                all_txn.put(xid,temp);

                printhashmap();
                return true;
            }
            else
                return false;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    public boolean deleteRooms(int xid, String location) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Room-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                //Query info for undo operations
                int seats = room_resourceManager.queryRooms(xid,location);
                int price = room_resourceManager.queryRoomsPrice(xid, location);

                boolean bool = room_resourceManager.deleteRooms(xid, location);
                if(!bool)
                    return false;

                String command = "deleterooms," + xid + "," + location;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                String undo = "deleteroomsundo," + xid + "," + location + "," + seats + "," + price;
                temp.add(undo);
                all_txn.put(xid,temp);

                printhashmap();
                return true;
            }
            else
                return false;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    public int queryFlight(int xid, int flightNum) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return -1;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Flight-" + flightNum;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_READ);
            if (lockGranted) {
                String command = "queryflight," + xid + "," + flightNum;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return flight_resourceManager.queryFlight(xid, flightNum);
            }
            else
                return -1;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return -99;
        }
    }

    public int queryCars(int xid, String location) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return -1;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Car-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_READ);
            if (lockGranted) {
                String command = "querycars," + xid + "," + location;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return car_resourceManager.queryCars(xid, location);
            }
            else
                return -1;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return -99;
        }
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return -1;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Room-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_READ);
            if (lockGranted) {
                String command = "queryrooms," + xid + "," + location;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return room_resourceManager.queryRooms(xid, location);
            }
            else
                return -1;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return -99;
        }
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return -1;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Flight-" + flightNum;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_READ);
            if (lockGranted) {
                String command = "queryflightprice," + xid + "," + flightNum;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return flight_resourceManager.queryFlightPrice(xid, flightNum);
            }
            else
                return -1;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return -99;
        }
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return -1;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Car-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_READ);
            if (lockGranted) {
                String command = "querycarsprice," + xid + "," + location;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return car_resourceManager.queryCarsPrice(xid, location);
            }
            else
                return -1;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return -99;
        }
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return -1;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Room-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_READ);
            if (lockGranted) {
                String command = "queryroomsprice," + xid + "," + location;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);
                return room_resourceManager.queryRoomsPrice(xid, location);
            }
            else
                return -1;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return -99;
        }
    }

    public String queryCustomerInfo(int xid, int customerID) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return "error";
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Customer-" + customerID;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_READ);
            if (lockGranted) {
                String command = "querycustomer," + xid + "," + customerID;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);

                String concat = "";
                concat = flight_resourceManager.queryCustomerInfo(xid, customerID);


                String r1 = car_resourceManager.queryCustomerInfo(xid, customerID);
                String[] response1 = r1.split("\n");
                for(int i = 1; i<response1.length; i++) {
                    concat += response1[i];
                }
                String r2 = room_resourceManager.queryCustomerInfo(xid, customerID);
                String[] response2 = r2.split("\n");
                for(int i = 1; i<response2.length; i++) {
                    concat += response2[i];
                }
                return concat;
            }
            else {
                return "error";
            }
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return "deadlock";
        }
    }

    public int newCustomer(int xid) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return -1;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String command = "addcustomer," + xid;
            ArrayList<String> temp = all_txn.get(xid);
            temp.add(command);

            int cid = customer_resourceManager.newCustomer(xid);
            flight_resourceManager.newCustomer(xid, cid);
            car_resourceManager.newCustomer(xid, cid);
            room_resourceManager.newCustomer(xid, cid);

            String objectID = "Customer-" + cid;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);

            String command1 = "addcustomerundo," + xid + "," + cid;
            temp.add(command1);
            all_txn.put(xid,temp);
            return cid;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return -99;
        }
    }

    public boolean newCustomer(int xid, int customerID) throws RemoteException
    {
//        long startTime = System.nanoTime();
//        if(!all_txn.containsKey(xid))
//            return false;
//        try {
//            String objectID = "Customer-" + customerID;
//            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
//            if (lockGranted) {
//                boolean bool1 = customer_resourceManager.newCustomer(xid, customerID);
//                boolean bool2 = flight_resourceManager.newCustomer(xid, customerID);
//                boolean bool3 = car_resourceManager.newCustomer(xid, customerID);
//                boolean bool4 = room_resourceManager.newCustomer(xid, customerID);
//
//                if(bool1 && bool2 && bool3 && bool4){
//                    String command = "addcustomerid," + xid + "," + customerID;
//                    ArrayList<String> temp = all_txn.get(xid);
//                    temp.add(command);
//                    all_txn.put(xid,temp);
//                    long endTime = System.nanoTime();
//                    System.out.println(endTime - startTime);
//                    return true;
//                }
//                else{
//                    return false;
//                }
//            }
//            else {
//                return false;
//            }
//        }
//        catch(DeadlockException e){
//            System.out.println("Deadlock Exception");
//            abort(xid);
//            return false;
//        }
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Customer-" + customerID;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                boolean bool1 = customer_resourceManager.newCustomer(xid, customerID);
                boolean bool2 = flight_resourceManager.newCustomer(xid, customerID);
                boolean bool3 = car_resourceManager.newCustomer(xid, customerID);
                boolean bool4 = room_resourceManager.newCustomer(xid, customerID);

                if(bool1 && bool2 && bool3 && bool4){
                    String command = "addcustomerid," + xid + "," + customerID;
                    ArrayList<String> temp = all_txn.get(xid);
                    temp.add(command);
                    all_txn.put(xid,temp);
                    return true;
                }
                else{
                    return false;
                }
            }
            else {
                return false;
            }
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    public boolean deleteCustomer(int xid, int customerID) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Customer-" + customerID;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                boolean bool = customer_resourceManager.deleteCustomer(xid, customerID) && flight_resourceManager.deleteCustomer(xid, customerID) &&
                        car_resourceManager.deleteCustomer(xid, customerID) && room_resourceManager.deleteCustomer(xid, customerID);
                if (!bool)
                    return false;
                String command = "deletecustomer," + xid + "," + customerID;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);

                String undo = "deletecustomerundo," + xid + "," + customerID;
                temp.add(undo);
                all_txn.put(xid,temp);

                return true;
            }
            else
                return false;
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Customer-" + customerID;
            String objectID2 = "Flight-" + flightNum;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            boolean lockGranted2 = lockManager.Lock(xid, objectID2, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted && lockGranted2) {
                boolean bool = flight_resourceManager.reserveFlight(xid, customerID, flightNum);
                if (!bool){
                    return false;
                }
                String command = "reserveflight," + xid + "," + customerID + "," + flightNum;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);

                return true;
            }
            else {
                return false;
            }
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Customer-" + customerID;
            String objectID2 = "Car-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            boolean lockGranted2 = lockManager.Lock(xid, objectID2, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted && lockGranted2) {
                boolean bool = car_resourceManager.reserveCar(xid,customerID,location);
                if(!bool) {
                    return false;
                }
                String command = "reservecar," + xid + "," + customerID + "," + location;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid, temp);
                return true;
            }
            else {
                return false;
            }
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return false;
        long live = System.currentTimeMillis();
        time_last.put(xid, live);
        try {
            String objectID = "Customer-" + customerID;
            String objectID2 = "Room-" + location;
            boolean lockGranted = lockManager.Lock(xid, objectID, TransactionLockObject.LockType.LOCK_WRITE);
            boolean lockGranted2 = lockManager.Lock(xid, objectID2, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted && lockGranted2) {
                boolean bool = room_resourceManager.reserveRoom(xid, customerID, location);
                if(!bool){
                    return false;
                }
                String command = "reserveroom," + xid + "," + customerID + "," + location;
                ArrayList<String> temp = all_txn.get(xid);
                temp.add(command);
                all_txn.put(xid,temp);

                return true;
            }
            else {
                return false;
            }
        }
        catch(DeadlockException e){
            System.out.println("Deadlock Exception");
            abort(xid);
            return false;
        }
    }

    // Reserve bundle
    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
    {
        if(!all_txn.containsKey(xid))
            return false;

        //If the car or the room is specified
        boolean f = true;
        Vector<String> completedTransactions = new Vector<>();
        //Reserve every flight
        for (String s : flightNumbers) {
            int flightID = Integer.parseInt(s);
            f = reserveFlight(xid, customerId, flightID);
            if (!f) {
                break;
            }
//            String command = "reserveflight," + xid + "," + customerId + "," + flightID;
//            ArrayList<String> temp = all_txn.get(xid);
//            temp.add(command);
//            all_txn.put(xid, temp);
            completedTransactions.add(0, s);
        }

        if (!f) {
            removeFromAllTxn(xid, customerId, completedTransactions, false);
            return false;
        }

        //Reserve car
        if (car) {
            boolean c = reserveCar(xid, customerId, location);
            if (!c) {
                removeFromAllTxn(xid, customerId, completedTransactions ,false);
                return false;
            }
            completedTransactions.add(location);
        }
        //Reserve room
        if (room) {
            boolean r = reserveRoom(xid, customerId, location);
            if (!r) {
                removeFromAllTxn(xid, customerId, completedTransactions, car);
                return false;
            }
        }
        return true;

    }

    public void removeFromAllTxn(int xid, int customerId, Vector<String> completedTransactions, boolean car) throws RemoteException {
        ArrayList<String> temp = all_txn.get(xid);

        if(car){
            String location = completedTransactions.get(completedTransactions.size()-1);
            car_resourceManager.unReserveCar(xid, customerId, location);
            String command = "reservecar," + xid + "," + customerId + "," + location;
            if(temp.lastIndexOf(command) != -1){
                temp.remove(temp.lastIndexOf(command));
            }
            completedTransactions.remove(completedTransactions.size()-1);
        }

        //Remove all flights that were added
        for (String s: completedTransactions){
            int flightID = Integer.parseInt(s);
            flight_resourceManager.unReserveFlight(xid, customerId, flightID);

            String command = "reserveflight," + xid + "," + customerId + "," + flightID;
            if(temp.lastIndexOf(command) != -1){
                temp.remove(temp.lastIndexOf(command));
            }
        }
        all_txn.put(xid, temp);
    }

    public boolean shutdown() throws RemoteException{
        if(all_txn.isEmpty()) {
            boolean b1 = flight_resourceManager.shutdown();
            boolean b2 = car_resourceManager.shutdown();
            boolean b3 = room_resourceManager.shutdown();
            boolean b4 = customer_resourceManager.shutdown();

            if(b1&&b2&&b3&&b4) {
                TimedExit t = new TimedExit();
                return true;
            }
        }
        return false;
    }

    public boolean unReserveCar(int id, int customerID, String location) { return false; }
    public boolean unReserveRoom(int id, int customerID, String location) throws RemoteException{return false;}
    public boolean unReserveFlight(int id, int customerID, int flightNumber) throws  RemoteException{return false;}
    public boolean unDeleteCustomer(int xid, int customerID) throws RemoteException {return false;}

    public String getName() throws RemoteException
    {
        return m_name;
    }

    private void printhashmap(){
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

        System.out.println("Transaction commands:");
        for (ArrayList<String> com: all_txn.values()){
            for(String s: com){
                System.out.print(s + "; ");
            }
            System.out.println();
        }
    }

}
