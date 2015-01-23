/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));


   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {
      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public String getlistid (String authorisedUser, int type) throws SQLException {
	   //0 = contact, 1 = block
	   String listtype = "contact_list";
	   if (type == 1) listtype = "block_list";	   
	   String query = String.format("SELECT " + listtype + " FROM Usr WHERE login = '%s'", authorisedUser);
	   Statement stmt = this._connection.createStatement ();
	   ResultSet rs = stmt.executeQuery (query);
	   rs.next();
	   return rs.getString(1);
   }
   
   public String getuserfromphone (String phonenum) throws SQLException {
	   String query = String.format("SELECT login FROM Usr WHERE phoneNum = '%s'", phonenum);
	   Statement stmt = this._connection.createStatement ();
	   ResultSet rs = stmt.executeQuery (query);
	   rs.next();
	   return rs.getString(1);
   }
	   
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Browse contact list");
                System.out.println("2. Add to contact list");
                System.out.println("3. Delete from contact list");
                System.out.println("4. Browse block list");
                System.out.println("5. Add to block list");
                System.out.println("6. Delete from block list");
                System.out.println("7. Read notification list");
                System.out.println("8. Chats");
                System.out.println("9. Delete your account");                
                System.out.println(".........................");
                System.out.println("0. Log out");
                switch (readChoice()){
                   case 1: ListContacts(esql, authorisedUser); break;
                   case 2: AddToContact(esql, authorisedUser); break;
                   case 3: DeleteContact(esql, authorisedUser); break;
                   case 4: ListBlocks(esql, authorisedUser); break;
                   case 5: AddToBlock(esql, authorisedUser); break;
                   case 6: DeleteBlock(esql, authorisedUser); break;
                   case 7: ReadNotifications(esql, authorisedUser); break;
                   case 8: Chats(esql, authorisedUser); break;
                   case 9: {if(deleteacc(esql)) usermenu = false;  break; }                  
                   case 0: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main
  
   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

	 //Creating empty contact\block lists for a user
	 esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
	 int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
	 int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
         
	 String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);
	 //System.out.println("Query:" + query);
         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println ("Invalid creation, Login or Phone is currently taken");
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
        
     if (userNum == 0)
       query = String.format("SELECT * FROM Usr WHERE login = '%s'", login);
         userNum = esql.executeQuery(query);
         if (userNum == 0) System.out.println("Login does not exist");
         else  System.out.println("Incorrect Password");
         return null;    
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static void AddToContact(Messenger esql, String authorisedUser){
	   String contact = "";
	   String query = "";
	   boolean go = true;
	   int choice = 0;
	   try{
		    while(go) {
				System.out.println("Add Contact Menu");
				System.out.println("---------");
				System.out.println("1. Add by login");
				System.out.println("2. Add by phone number");
				System.out.println("3. Back");
				choice = readChoice();
				switch(choice) {
					case 1: System.out.print("\tEnter contact login: ");
							contact = in.readLine();
							query = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
							go = false;
							break;
					case 2: System.out.print("\tEnter contact phone number: ");
							contact = in.readLine();                
							query = String.format("SELECT * FROM Usr WHERE phoneNum  = '%s'", contact); 
							go = false;
							break;
					case 3: return;
					default : System.out.println("Unrecognized choice!"); break;
				}
			}
			int userNum = esql.executeQuery(query);
			if (userNum == 0) {
				System.out.println("User does not exist");
				return;
			}
		}
		catch (Exception e) {
			//System.err.println (e.getMessage ());
			return;
		}
		try{
			if (choice == 2) {
				contact = esql.getuserfromphone(contact);
			}
			String listid = esql.getlistid(authorisedUser, 0);
			query = String.format("INSERT INTO USER_LIST_CONTAINS VALUES (" + listid + ", '" + contact + "')");
			esql.executeUpdate(query);
			System.out.println("Contact successfully added.");
		}
		catch (Exception e) {
			System.out.println("User already exists in contact list.");
			//System.err.println (e.getMessage ());
			return;
		}      
   }//end

   public static void AddToBlock(Messenger esql, String authorisedUser){
	   String contact = "";
	   String query = "";
	   boolean go = true;
	   int choice = 0;
	   try{
		    while(go) {
				System.out.println("Block User Menu");
				System.out.println("---------");
				System.out.println("1. Add by login");
				System.out.println("2. Add by phone number");
				System.out.println("3. Back");
				choice = readChoice();
				switch(choice) {
					case 1: System.out.print("\tEnter user login: ");
							contact = in.readLine();
							query = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
							go = false;
							break;
					case 2: System.out.print("\tEnter user phone number: ");
							contact = in.readLine();                
							query = String.format("SELECT * FROM Usr WHERE phoneNum  = '%s'", contact); 
							go = false;
							break;
					case 3: return;
					default : System.out.println("Unrecognized choice!"); break;
				}
			}
			int userNum = esql.executeQuery(query);
			if (userNum == 0) {
				System.out.println("User does not exist.");
				return;
			}
		}
		catch (Exception e) {
			//System.err.println (e.getMessage ());
			return;
		}
		try{
			if (choice == 2) {
				contact = esql.getuserfromphone(contact);
			}
			if(contact.equals(authorisedUser))
			{
				System.out.println("Can not block yourself");
				return;
			}
			String listid = esql.getlistid(authorisedUser, 1);
			query = String.format("INSERT INTO USER_LIST_CONTAINS VALUES (" + listid + ", '" + contact + "')");
			esql.executeUpdate(query);
			System.out.println("User successfully added to block list.");
		}
		catch (Exception e) {
			System.out.println("User already exists in block list.");
			//System.err.println (e.getMessage ());
			return;
		}   
   }//end
   
   public static void DeleteBlock(Messenger esql, String authorisedUser){
	   String contact = "";
	   String query = "";
	   boolean go = true;
	   int choice = 0;
	   try{
		    while(go) {
				System.out.println("Block Delete Menu");
				System.out.println("---------");
				System.out.println("1. Delete by login");
				System.out.println("2. Delete by phone number");
				System.out.println("3. Back");
				choice = readChoice();
				switch(choice) {
					case 1: System.out.print("\tEnter user login: ");
							contact = in.readLine();
							query = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
							go = false;
							break;
					case 2: System.out.print("\tEnter user phone number: ");
							contact = in.readLine();                
							query = String.format("SELECT * FROM Usr WHERE phoneNum  = '%s'", contact); 
							go = false;
							break;
					case 3: return;
					default : System.out.println("Unrecognized choice!"); break;
				}
			}
			int userNum = esql.executeQuery(query);
			if (userNum == 0) {
				System.out.println("User does not exist.");
				return;
			}
		}
		catch (Exception e) {
			//System.err.println (e.getMessage ());
			return;
		}
		try{
			if (choice == 2) {
				contact = esql.getuserfromphone(contact);
			}
			String listid = esql.getlistid(authorisedUser, 1);
			query = String.format("SELECT * FROM USER_LIST_CONTAINS WHERE list_id = " + listid + " AND list_member = '" + contact + "'");
			int userNum = esql.executeQuery(query);
			if(userNum == 0)
			{
				System.out.println("User is not in block list.");
				return;
			}

			query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = " + listid + " AND list_member = '" + contact + "'");
			esql.executeUpdate(query);
			System.out.println("User successfully removed from block list.");
		}
		catch (Exception e) {
			//System.out.println("User is not in block list.");
			System.err.println (e.getMessage ());
			return;
		}   
   }//end   
   
   public static void DeleteContact(Messenger esql, String authorisedUser){
	   String contact = "";
	   String query = "";
	   boolean go = true;
	   int choice = 0;
	   try{
		    while(go) {
				System.out.println("Contact Delete Menu");
				System.out.println("---------");
				System.out.println("1. Delete by login");
				System.out.println("2. Delete by phone number");
				System.out.println("3. Back");
				choice = readChoice();
				switch(choice) {
					case 1: System.out.print("\tEnter user login: ");
							contact = in.readLine();
							query = String.format("SELECT * FROM Usr WHERE login = '%s'", contact);
							go = false;
							break;
					case 2: System.out.print("\tEnter user phone number: ");
							contact = in.readLine();                
							query = String.format("SELECT * FROM Usr WHERE phoneNum  = '%s'", contact); 
							go = false;
							break;
					case 3: return;
					default : System.out.println("Unrecognized choice!"); break;
				}
			}
			int userNum = esql.executeQuery(query);
			if (userNum == 0) {
				System.out.println("User does not exist.");
				return;
			}
		}
		catch (Exception e) {
			//System.err.println (e.getMessage ());
			return;
		}
		try{
			if (choice == 2) {
				contact = esql.getuserfromphone(contact);
			}
			String listid = esql.getlistid(authorisedUser, 0);
			query = String.format("SELECT * FROM USER_LIST_CONTAINS WHERE list_id = " + listid + " AND list_member = '" + contact + "'");
			int userNum = esql.executeQuery(query);
			if(userNum == 0)
			{
				System.out.println("User is not in contacts list.");
				return;
			}

			query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = " + listid + " AND list_member = '" + contact + "'");
			esql.executeUpdate(query);
			System.out.println("User successfully removed from contacts list.");
		}
		catch (Exception e) {
			//System.out.println("User is not in block list.");
			System.err.println (e.getMessage ());
			return;
		}   
   }//end   
   


   public static void ListContacts(Messenger esql, String authorisedUser){
	    try{
			String query = String.format("SELECT C2.list_member,L2.status FROM (SELECT C.list_member FROM USR L, USER_LIST_CONTAINS C WHERE L.contact_list = C.list_id AND L.login = '" + authorisedUser + "') as C2, USR L2 WHERE C2.list_member = L2.login");
			int rows =  esql.executeQueryAndPrintResult(query);
			System.out.println("Total Contacts: " + rows);
           
		}catch(Exception e){
			System.err.println (e.getMessage ());
			return;
		}	

   }//end
   
   public static void ListBlocks(Messenger esql, String authorisedUser){
	    try{
			String query = String.format("SELECT C.list_member FROM USR L, USER_LIST_CONTAINS C WHERE L.block_list = C.list_id AND L.login = '" + authorisedUser + "'");
			int rows =  esql.executeQueryAndPrintResult(query);
			System.out.println("Total Blocks: " + rows);
           
		}catch(Exception e){
			System.err.println (e.getMessage ());
			return;
		}	

   }

   public static void ReadNotifications(Messenger esql, String authorisedUser){
	    try{
			String query = String.format("SELECT M.sender_login FROM NOTIFICATION N, MESSAGE M WHERE N.msg_id = M.msg_id AND N.usr_login = '" + authorisedUser + "'");
			System.out.println("New Notifcations from: ");
			int rows =  esql.executeQueryAndPrintResult(query);
			System.out.println("Total Notification: " + rows);
            
		}catch(Exception e){
			System.err.println (e.getMessage ());
			return;
		}	      
      
   }//end

   public static boolean deleteacc(Messenger esql){
      System.out.println("Are you sure you wish to delete your account?");
      System.out.println("0. Yes");
      System.out.println("1. No");
             switch (readChoice()){
               case 0: {
				  try{   
				    System.out.println("Reenter login info to verify identity: ");
					System.out.print("\tEnter user login: ");
					String login = in.readLine();
					System.out.print("\tEnter user password: ");
					String password = in.readLine();
					String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
					int userNum = esql.executeQuery(query);
									    
					query = String.format("DELETE FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
					esql.executeQuery(query);
					if (userNum > 0)
				    System.out.println("Deleting.... ");
					
					return true;
					}
				catch(Exception e){
				    System.out.println("Deleting.... ");
				    return true;}
				}
               case 1: return false;
               //default : System.out.println("Unrecognized choice!"); break;
            }
            return false;//end switch     
   }//end

   public static void ListChats(Messenger esql, String authorisedUser){
	    try{
			System.out.println("Private Chats:");
			String query = String.format("(SELECT c.chat_id,max_time.max as \"Most Recent Timestamp \"FROM (SELECT chat_id FROM CHAT_LIST GROUP BY chat_id HAVING COUNT (member) < 3) as stuff, chat_list c, (select chat_id, max(msg_timestamp) from message group by chat_id) as max_time WHERE c.chat_id = stuff.chat_id AND member = '" + authorisedUser + "'AND max_time.chat_id = c.chat_id) ORDER BY max_time.max DESC");
			int rows =  esql.executeQueryAndPrintResult(query);
			System.out.println("Public Chats:");
			query = String.format("(SELECT c.chat_id,max_time.max as \"Most Recent Timestamp \"FROM (SELECT chat_id FROM CHAT_LIST GROUP BY chat_id HAVING COUNT (member) > 2) as stuff, chat_list c, (select chat_id, max(msg_timestamp) from message group by chat_id) as max_time WHERE c.chat_id = stuff.chat_id AND member = '" + authorisedUser + "'AND max_time.chat_id = c.chat_id) ORDER BY max_time.max DESC");
		    rows +=  esql.executeQueryAndPrintResult(query);
			System.out.println("Total Chats: " + rows);
		}catch(Exception e){
			System.err.println (e.getMessage ());
			return;
		}
        try{
            System.out.println("Which chat would you like to view? (Chat ID)");
            String chatid = in.readLine();
            String query = String.format( "SELECT member FROM CHAT_LIST WHERE chat_id ="+ chatid +" AND member = '" + authorisedUser + "'" );
            int rows = esql.executeQuery(query);
            if(rows == 0)
            {
                System.out.println("You are not in the chat");
                return;
            }
            ViewMessages(esql, authorisedUser, chatid);
        } catch (Exception e) {
            System.err.println (e.getMessage ());
        }
    }//end
   
   public static List<String> fillArray (Messenger esql, String authorisedUser, String chatid) throws SQLException{
       List<String> messagelist = new ArrayList<String>();
       String listid = esql.getlistid (authorisedUser, 1);
       String query = String.format("SELECT message.msg_id,sender_login,msg_text,msg_timestamp,media_type,URL FROM message LEFT OUTER JOIN media_attachment ON (message.msg_id = media_attachment.msg_id) WHERE message.chat_id = " + chatid + " AND sender_login NOT IN (SELECT list_member FROM USER_LIST_CONTAINS WHERE list_id = " + listid + ") ORDER BY msg_timestamp DESC");
       Statement stmt = esql._connection.createStatement ();
       ResultSet rs = stmt.executeQuery (query);
       ResultSetMetaData rsmd = rs.getMetaData ();
       int numCol = rsmd.getColumnCount ();
      while (rs.next()) {
         String currentmessage = "";
         for (int i=1; i<=numCol; ++i) {
			String temp = rs.getString(i);
			if (temp != null) currentmessage += (temp + "|");
		 }
         messagelist.add(currentmessage);
      }//end while
      stmt.close ();
      return messagelist;
  }
    
   public static void ViewMessages(Messenger esql, String authorisedUser, String chatid) {
      try {
      List<String> messagelist = fillArray(esql,authorisedUser,chatid);     
      int total = messagelist.size();
      int blah = 10;
      int current = blah;
      if (current > total) current = total;
      for (int i = current-1; i >= current - blah && i >= 0; i--) {
            System.out.println(messagelist.get(i));
      }
      boolean usermenu = true;
      while(usermenu) {
        boolean showmessages = true;
        System.out.println("1. Browse Older Messages");
        System.out.println("2. Browse Newer Messages");
        System.out.println("3. Create Message");
        System.out.println("4. Edit Message");
        System.out.println("5. Delete Message");
        System.out.println(".........................");
        System.out.println("0. Go Back");
        int choice = readChoice();
        switch(choice) {
            case 1: if (current < total) {
                        current += blah;
                        if (current > total) current = total;
                        showmessages = true;
                    }
                    else {
                        System.out.println("No Older messages");
                        showmessages = false;
                    }
                    break;
            case 2: if (current > blah) {
                        current -= blah;
                        if (current < blah) current = blah;
                        showmessages = true;
                    }
                    else {
                        System.out.println("No Newer messages");
                        showmessages = false;
                    }
                    break;
            case 3: createMessage(esql, authorisedUser, chatid);messagelist = fillArray(esql,authorisedUser,chatid);     
                    showmessages = true; 
                    total = messagelist.size();
                    current = (blah > total) ? total : blah; 
                    break;
            case 4: showmessages = editMessage(esql, authorisedUser,chatid); messagelist = fillArray(esql,authorisedUser,chatid);     
                    break;
            case 5: showmessages = deleteMessage(esql, authorisedUser,chatid);messagelist = fillArray(esql,authorisedUser,chatid);     
                    total = messagelist.size();
                    current = (current > total) ? total : current; break;
            case 0: usermenu = false; break;
            default : System.out.println("Unrecognized choice!"); showmessages = false; break;
        }
        if (!usermenu) break;
        if (!showmessages) continue;                        
        for (int i = current-1; i >= current - blah && i >= 0; i--) {
            System.out.println(messagelist.get(i));
        }
    }
    String query = String.format("DELETE FROM message WHERE chat_id = " + chatid + " AND destr_timestamp < now()");
    esql.executeUpdate(query);
    query = String.format("DELETE FROM NOTIFICATION where msg_id in (select msg_id from message where message.msg_id = notification.msg_id and notification.usr_login = '" + authorisedUser + "' AND message.chat_id = " + chatid + ")");
    esql.executeUpdate(query);
      } catch (Exception e) {
          System.err.println (e.getMessage ());
          return;
      }
          
  }
    
  public static boolean editMessage (Messenger esql, String authorisedUser, String chatid) {
      try {
          System.out.println("Which message would you like to edit? (Message ID)");
          String msgid = in.readLine();
          String query = ("SELECT * FROM message WHERE msg_id = " + msgid + " AND sender_login = '" + authorisedUser + "' AND chat_id = " + chatid);
          int rows = esql.executeQuery(query);
          if (rows == 0) {
              System.out.println("That message does not belong to you or does not exist in this chat");
              return false;
          }
          System.out.println("What would you like the message to say now?");
          String text = in.readLine();
          query = ("UPDATE message SET msg_text = '" + text + "' WHERE msg_id = " + msgid);
          esql.executeUpdate(query);
          System.out.println("Message successfully edited");
          int asdf = esql.getCurrSeqVal("message_msg_id_seq");
          query = String.format("SELECT member FROM chat_list WHERE chat_id = " + chatid);
          List<String> memberlist = new ArrayList<String>();
          Statement stmt = esql._connection.createStatement ();
          ResultSet rs = stmt.executeQuery (query);
          while (rs.next()) {
		  	String temp = rs.getString(1);
            System.out.println(temp);
            if (!temp.equals(authorisedUser)) memberlist.add(temp);
          }
          for (int i = 0; i < memberlist.size(); i++) {
            query = String.format("INSERT INTO NOTIFICATION (msg_id, usr_login) values (" + asdf + ", '"+ memberlist.get(i) +"')");
            esql.executeUpdate(query);        
          }
          return true;
      } catch (Exception e) {
          System.err.println (e.getMessage ());
          return false;
      }
  }
  public static boolean deleteMessage (Messenger esql, String authorisedUser, String chatid) {
      try {
          System.out.println("Which message would you like to delete? (Message ID)");
          String msgid = in.readLine();
          String query = ("SELECT * FROM message WHERE msg_id = " + msgid + " AND sender_login = '" + authorisedUser + "' AND chat_id = " + chatid);
          int rows = esql.executeQuery(query);
          if (rows == 0) {
              System.out.println("That message does not belong to you or does not exist in this chat");
              return false;
          }
          query = ("DELETE FROM message WHERE msg_id = " + msgid + " AND sender_login = '" + authorisedUser + "' AND chat_id = " + chatid);
          esql.executeUpdate(query);
          System.out.println("Message successfully deleted");
          return true;
      } catch (Exception e) {
          System.err.println (e.getMessage ());
          return false;
      }
  }
          
  
  public static void createMessage (Messenger esql, String authorisedUser, String chatid) {
	  try {
        System.out.println("What would you like your message to say?");
        String text = in.readLine();
        String query = String.format("INSERT INTO message (msg_text,msg_timestamp,sender_login,chat_id) VALUES ('" + text + "',now(),'" + authorisedUser + "'," + chatid + ")");
	    esql.executeUpdate(query);
        System.out.println("Message sent.");
        int asdf = esql.getCurrSeqVal("message_msg_id_seq");
        query = String.format("SELECT member FROM chat_list WHERE chat_id = " + chatid);
        List<String> memberlist = new ArrayList<String>();
        Statement stmt = esql._connection.createStatement ();
        ResultSet rs = stmt.executeQuery (query);
        while (rs.next()) {
			String temp = rs.getString(1);
            System.out.println(temp);
            if (!temp.equals(authorisedUser)) memberlist.add(temp);
        }
        for (int i = 0; i < memberlist.size(); i++) {
            query = String.format("INSERT INTO NOTIFICATION (msg_id, usr_login) values (" + asdf + ", '"+ memberlist.get(i) +"')");
            esql.executeUpdate(query);        
        }
    
      } catch (Exception e) {
          System.err.println (e.getMessage ());
      }
  }
      
      
       
   
   public static void AddChats(Messenger esql, String authorisedUser) {
	   String chat_type = "";
	   int choice = 0;
	   boolean usermenu = true;
	   while (usermenu) {
		   System.out.println("Create Chat MENU");	
           System.out.println("---------");
           System.out.println("1. Private Chat");
           System.out.println("2. Public Chat");
           System.out.println(".........................");
           System.out.println("0. Go Back");
           choice = readChoice();
           switch(choice) {
			   case 1: chat_type = "Private"; break;
			   case 2: chat_type = "Public"; break;
			   case 0: usermenu = false; break;
			   default : System.out.println("Unrecognized choice!"); break;
		   }
		   if (!usermenu) break;
		   try {
			   String query = String.format("INSERT INTO chat (chat_type, init_sender) VALUES ('" + chat_type + "','" + authorisedUser + "')");
			   esql.executeUpdate(query);
		   } catch (Exception e) {
			   System.err.println (e.getMessage ());
			   return;
		   }
		   String curseq = "";
		   try {
			   int asdf = esql.getCurrSeqVal("chat_chat_id_seq");
			   curseq = Integer.toString(asdf);
			   String query = String.format("INSERT INTO chat_list (chat_id	, member) VALUES (" + curseq + ",'" + authorisedUser + "')");
			   esql.executeUpdate(query);
		   } catch (Exception e) {
			   System.err.println (e.getMessage ());
			   return;
		   }
		   try {
			   String query = String.format("INSERT INTO message (msg_text, msg_timestamp, sender_login, chat_id) VALUES ('" + authorisedUser + " created this chat',now(),'" + authorisedUser + "','" + curseq + "')");
			   esql.executeUpdate(query);
		   } catch (Exception e) {
			   System.err.println (e.getMessage ());
			   return;
		   }
		   Editsubmenu(esql, authorisedUser, curseq);
		   usermenu = false;
	   }
   }
   
      public static void RemoveUsers(Messenger esql, String authorisedUser, String id)
   {
       try
       {
               String query = String.format( "SELECT member FROM CHAT_LIST WHERE chat_id ="+id );
            int rows = esql.executeQueryAndPrintResult(query);
 
       System.out.println("Enter Login of user you wish to remove: ");
       String login = in.readLine();
       if(login.equals(authorisedUser))
       {
           System.out.println("Can not remove yourself, to do so you must delete entire chat log");
       }
       else  
       {
               query = String.format( "SELECT member FROM CHAT_LIST WHERE chat_id ="+id +" AND member = '" + login+"'" );
            rows = esql.executeQuery(query);
            if(rows == 0)
            {
                System.out.println("Login is not in the chat");
                return;
            }
            else
            {
                query = String.format("DELETE FROM CHAT_LIST WHERE member = '"+login+"' AND chat_id = " + id);
                esql.executeUpdate(query);
                System.out.println("Login successfully removed");
            }
        }
   }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }     
        
 
        
   }
 
   public static void AddUsers(Messenger esql, String authorisedUser, String id)
   {
       try
       {
            System.out.println("Users currently in chat:");
            String query = String.format( "SELECT member FROM CHAT_LIST WHERE chat_id ="+id );
            int rows = esql.executeQueryAndPrintResult(query);
       System.out.println("Enter Login of user you wish to Add: ");
       String login = in.readLine();
       if(login.equals(authorisedUser))
       {
           System.out.println("Can not add yourself, you are already a member");
       }
       else  
       {
            query = String.format("Select * FROM USR where login = '" + login + "'");
            if(rows == 0)
            {
                System.out.println("Login does not exist");
                return;
            }
            query = String.format( "SELECT member FROM CHAT_LIST WHERE chat_id ="+id +" AND member = '" + login+"'" );
            rows = esql.executeQueryAndPrintResult(query);
            if(rows != 0)
            {
                System.out.println("Login is already in the chat");
                return;
            }
            else
            {
                query = String.format("INSERT INTO CHAT_LIST (chat_id, member) VALUES ("+id+ ", '"+login+"')");
                esql.executeUpdate(query);
                System.out.println("Login successfully added");
            }
        }
   }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }     
        
 
        
   }    
    
   public static void Editsubmenu(Messenger esql, String authorisedUser, String id)
                    {
                        System.out.println("What would you like to do?");
                        boolean usermenu = true;
                          while(usermenu) {
                            System.out.println("---------");
                            System.out.println("1. Add Users");
                            System.out.println("2. Remove Users");
                        
                            System.out.println(".........................");
                            System.out.println("0. Go Back");
                            switch (readChoice()){
                               case 1: AddUsers(esql, authorisedUser, id); break;
                               case 2: RemoveUsers(esql, authorisedUser, id); break;
                               case 0: usermenu = false; break;
                               default : System.out.println("Unrecognized choice!"); break;
                            }
                          }
                    }

   
   public static void DeleteChats(Messenger esql, String authorisedUser) {
	   String chatid = "";
	   try {
            String query = String.format( "SELECT chat_id FROM CHAT WHERE init_sender ='" + authorisedUser+"'");
            int rows = esql.executeQueryAndPrintResult(query);
            
        } catch (Exception e) {
            System.err.println (e.getMessage ());
            System.out.println("You are not the admin of any chats");
            return;
        }      
	   try {
           System.out.println("Which chat would you like to delete? (chat ID)");
           chatid = in.readLine();
		   String query = String.format( "SELECT chat_id FROM CHAT WHERE chat_id =" + chatid + " AND init_sender = '" +authorisedUser+"'");
           int rows = esql.executeQuery(query);
           if(rows == 0)
           {
                        System.out.println("That ID does not exist or you are not the admin of it");
                        return;
           }
           query = String.format("DELETE FROM CHAT WHERE CHAT_ID = " + chatid + " AND init_sender = '" + authorisedUser + "'");
           esql.executeUpdate(query);
		   System.out.println("Successfully deleted chat");
       } catch (Exception e) {
           System.out.println ("Invalid Input");
           //System.err.println (e.getMessage ());
           return;
       }
   }
	   
	   
 public static void EditChats(Messenger esql, String authorisedUser)
   {
               try{
                   String query = String.format( "SELECT * FROM CHAT WHERE init_sender ='" + authorisedUser+"'");
                int rows =  esql.executeQuery(query);
                if(rows == 0)
                {
                    System.out.println("You are not the admin of any chats");
                    return;
                }
                else
                {
                    query = String.format( "SELECT chat_id FROM CHAT WHERE init_sender ='" + authorisedUser+"'");
                    rows = esql.executeQueryAndPrintResult(query);
                    System.out.println("Enter the chat id that you wish to edit, -1 to go back: ");   
                    String id = in.readLine();
                    if(id.equals("-1")) return;
                    query = String.format( "SELECT chat_id FROM CHAT WHERE chat_id =" + id+" AND init_sender = '" +authorisedUser+"'");
                    rows = esql.executeQuery(query);
                    if(rows == 0)
                    {
                        System.out.println("That ID does not exist or you are not the admin of it");
                    }
                    else
                    {
                        Editsubmenu(esql, authorisedUser, id);
                    }
 
                     
 
                }
                //System.out.println("Total Chats: " + rows);
                return;
            }catch(Exception e){
            System.out.println ("Invalid Input");
            //System.err.println (e.getMessage ());
            return;
        }     
    }      

   public static void Chats(Messenger esql, String authorisedUser){
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("Chat MENU");
                System.out.println("---------");
                System.out.println("1. Browse Chat list");
                System.out.println("2. Create New Chat");
                System.out.println("3. Delete Chat");
                System.out.println("4. Edit Chat");
                System.out.println(".........................");
                System.out.println("0. Go Back");
                switch (readChoice()){
                   case 1: ListChats(esql, authorisedUser); break;
                   case 2: AddChats(esql, authorisedUser); break;
                   case 3: DeleteChats(esql, authorisedUser); break;
                   case 4: EditChats(esql, authorisedUser); break;
                   case 0: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
   }//end Query6

}//end Messenger
