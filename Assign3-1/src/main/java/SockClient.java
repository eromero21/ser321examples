import org.json.JSONArray;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 */
class SockClient {
  static Socket sock = null;
  static String host = "localhost";
  static int port = 8888;
  static OutputStream out;
  // Using and Object Stream here and a Data Stream as return. Could both be the same type I just wanted
  // to show the difference. Do not change these types.
  static ObjectOutputStream os;
  static DataInputStream in;
  public static void main (String args[]) {

    if (args.length != 2) {
      System.out.println("Expected arguments: <host(String)> <port(int)>");
      System.exit(1);
    }

    try {
      host = args[0];
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port|sleepDelay] must be an integer");
      System.exit(2);
    }

    try {
      connect(host, port); // connecting to server
      System.out.println("Client connected to server.");
      boolean requesting = true;
      while (requesting) {
        System.out.println("What would you like to do: 1 - echo, 2 - add, 3 - addmany, 4 - movies, (0 to quit)");
        Scanner scanner = new Scanner(System.in);
        int choice = Integer.parseInt(scanner.nextLine());
        // You can assume the user put in a correct input, you do not need to handle errors here
        // You can assume the user inputs a String when asked and an int when asked. So you do not have to handle user input checking
        JSONObject json = new JSONObject(); // request object
        switch(choice) {
          case 0:
            System.out.println("Choose quit. Thank you for using our services. Goodbye!");
            requesting = false;
            break;
          case 1:
            System.out.println("Choose echo, which String do you want to send?");
            String message = scanner.nextLine();
            json.put("type", "echo");
            json.put("data", message);
            break;
          case 2:
            System.out.println("Choose add, enter first number:");
            String num1 = scanner.nextLine();
            json.put("type", "add");
            json.put("num1", num1);

            System.out.println("Enter second number:");
            String num2 = scanner.nextLine();
            json.put("num2", num2);
            break;
          case 3:
            System.out.println("Choose addmany, enter as many numbers as you like, when done choose 0:");
            JSONArray array = new JSONArray();
            String num = "1";
            while (!num.equals("0")) {
              num = scanner.nextLine();
              array.put(num);
              System.out.println("Got your " + num);
            }
            json.put("type", "addmany");
            json.put("nums", array);
            break;
            // add the movies option here with its different options
          case 4:
        	  System.out.println("Would you like to 1.Add movie 2.View all ratings 3.View specified rating 4.Rate movie.");
        	  scanner = new Scanner(System.in);
        	  choice = Integer.parseInt(scanner.nextLine());
        	  String input="";
        	  int rate=0;
        	  
        	  switch(choice) {
        	  	case 1:
        	  		json.put("type", "rating");
        	  		json.put("task", "add");
        	  		
        	  		System.out.println("Enter movie name:");
        	  		scanner = new Scanner(System.in);
        	  		input = scanner.nextLine();
        	  		json.put("movie", input);
        	  		
        	  		System.out.println("Enter movie rating (1-5):");
        	  		scanner = new Scanner(System.in);
        	  		rate = Integer.parseInt(scanner.nextLine());
        	  		json.put("rating", rate);
        	  		
        	  		System.out.println("Enter username:");
        	  		scanner = new Scanner(System.in);
        	  		input = scanner.nextLine();
        	  		json.put("username", input);
        	  		
        	  		break;
        	  		
        	  	case 2:
        	  		json.put("type", "rating");
        	  		json.put("task", "view");
        	  		
        	  		break;
        	  		
        	  	case 3:
        	  		json.put("type", "rating");
        	  		json.put("task", "view");
        	  		scanner = new Scanner(System.in);
        	  		input = scanner.nextLine();
        	  		json.put("movie", input);
        	  		
        	  		break;
        	  		
        	  	case 4:
        	  		json.put("type", "rating");
        	  		json.put("task", "rate");
        	  		
        	  		System.out.println("Enter movie name:");
        	  		scanner = new Scanner(System.in);
        	  		input = scanner.nextLine();
        	  		json.put("movie", input);
        	  		
        	  		System.out.println("Enter movie rating (1-5):");
        	  		scanner = new Scanner(System.in);
        	  		rate = Integer.parseInt(scanner.nextLine());
        	  		json.put("rating", rate);
        	  		
        	  		System.out.println("Enter username:");
        	  		scanner = new Scanner(System.in);
        	  		input = scanner.nextLine();
        	  		json.put("username", input);
        	  		
        	  		break;
        	  }
        	  
        	  break;
          		
        }
        if(!requesting) {
          continue;
        }

        // write the whole message
        os.writeObject(json.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        // TODO: handle the response
        // - not doing anything other than printing payload
        // !! you will most likely need to parse the response for the other services so this works. 
        String i = (String) in.readUTF();
        JSONObject res = new JSONObject(i);
        System.out.println("Got response: " + res);
        if (res.getBoolean("ok")){
          if (res.getString("type").equals("echo")) {
            System.out.println(res.getString("echo"));
          } 
          else if(res.getString("type").equals("rating")) {
        	  JSONArray arr = res.getJSONArray("movies");
        	  
        	  if(arr.length()!=0 && arr!=null) {
	        	  for(int j=0; j<arr.length(); j++) {
	        		  res = arr.getJSONObject(j);
	        		  System.out.println("Movie: " + res.getString("movie"));
	        		  System.out.println("Rating: " + res.getString("rating"));
	        		  JSONArray raters = res.getJSONArray("raters");
	        		  System.out.println("Raters: " + raters.getString(0));
	        		  if(raters.length()>1) {
	        			  for(int l=1; l<raters.length(); l++) {
	        				  System.out.println(raters.getString(l));
	        			  }
	        		  }
	        	  }
        	  } else {
        		  System.out.println("Empty movie list returned..");
        	  }
          } 
          else {
            System.out.println(res.getInt("result"));
          }
        } else {
          System.out.println(res.getString("message"));
        }
      }
      // want to keep requesting services so don't close connection
      //overandout();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void overandout() throws IOException {
    //closing things, could
    in.close();
    os.close();
    sock.close(); // close socked after sending
  }

  public static void connect(String host, int port) throws IOException {
    // open the connection
    sock = new Socket(host, port); // connect to host and socket on port 8888

    // get output channel
    out = sock.getOutputStream();

    // create an object output writer (Java only)
    os = new ObjectOutputStream(out);

    in = new DataInputStream(sock.getInputStream());
  }
}