// Simple REST webserver for transit data

//import java.io.IOException;
//import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import org.mariadb.jdbc.Driver;


public class TransitDataServer {

    public static void main(String[] args) throws Exception {
	// Vars
	String sql_string = "jdbc:mariadb://localhost:3306/";
	String sql_user   = "root";
	//String sql_pass   = "password";

	String data_dir = "gtfs";

	int port = 8200;

	
	// Set-up SQL Database Connection
	Class.forName("org.mariadb.jdbc.Driver");
	Properties connectionProps = new Properties();
	connectionProps.put("user", sql_user);
	//connectionProps.put("password", sql_pass);
	    
	Connection sql_conn = DriverManager.getConnection(sql_string, connectionProps);
	    
	System.out.println("Connected to database");


	// Get data
	
	getData(data_dir, sql_conn);
	System.out.println("Data loaded");

	// Start Server
	HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
	server.createContext("/test", new TestHandler());
	server.setExecutor(null);
	server.start();
    }

    static class TestHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange t) throws IOException {
	    String response = "This is the response";
	    t.sendResponseHeaders(200, response.length());
	    OutputStream os = t.getResponseBody();
	    os.write(response.getBytes());
	    os.close();
	}
    }

    private static void getData(String data_dir, Connection sql_conn) throws SQLException {
	File folder = new File(data_dir);
	File[] agency_folders = folder.listFiles();
	Statement sql_stmt = sql_conn.createStatement();
	ResultSet sql_result;

	// Get current database names
	sql_result = sql_stmt.executeQuery("SHOW DATABASES;");

	List<String> databases = new ArrayList<String>();
	while(sql_result.next())
	    databases.add(sql_result.getString("Database"));
	

	String[] lines = null;    
    
	for (File file : folder.listFiles())
	    if (file.isDirectory() && !file.getName().startsWith(".")) {

		//System.out.println("In dir: " + file.getName());
		File agency = new File(file.getAbsolutePath() + "/agency.txt");
		lines = readFile(agency);
		String[] headers = lines[0].split(",", -1);
		String agency_name = lines[1].split(",", -1)[Arrays.asList(headers).indexOf("agency_name")].replace(' ','_').replace('-','_');
		System.out.println("Processing agency: " + agency_name);

		// Create SQL Database with agency name
		if (!databases.contains(agency_name))
		    sql_stmt.executeUpdate("CREATE DATABASE " + agency_name + ";");

		// Select Database
		sql_stmt.executeUpdate("USE " + agency_name + ";");
		
		for (File table : file.listFiles()) {
		    // Check for existing table name
		    String table_name = stripExtension(table.getName());
		    String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='" + agency_name + "';";
		    sql_result = sql_stmt.executeQuery(query);
		    List<String> table_names = new ArrayList<String>();

		    System.out.println("\tTable: " + table_name);

		    while (sql_result.next())
			table_names.add(sql_result.getString("TABLE_NAME"));

		    // Delete table if it exists
		    if (table_names.contains(table_name))
			sql_stmt.executeUpdate("DROP TABLE " + table_name + ";");

		    // Create new table
		    lines = readFile(table);
		    headers = lines[0].split(",");
		    String create_table_query = "CREATE TABLE " + table_name + " (\n";
		    for (int i = 0; i < headers.length; i++) {
			String header = headers[i];
			
			if (header.endsWith("id"))
			    create_table_query += header + "\tINT\tNOT NULL";
			else
			    create_table_query += header + "\tVARCHAR (100)";

			if (i != headers.length - 1)
			    create_table_query += ",\n";
		    }
		    
		    create_table_query += ");";
		    
		    sql_stmt.executeUpdate(create_table_query);

		    // Add data to table
		    for (int j = 1; j < lines.length; j++) {
			if (lines.length > 1000 && j % 1000 == 0)
			    updateProgress((double)j / lines.length);
			
			String row = createRow(lines[j].replace("'", "").split(",", -1));
			query = "INSERT INTO " + table_name + "\n VALUES " + row + ";";
			try {
			    sql_stmt.executeUpdate(query);
			} catch (SQLException e) {
			    for (int k = 0; k < 120; k++)
				System.out.print(" ");
			    System.out.print("\r");
			    System.out.print("\tSQL Error: Line: " + j + "\n\t" + e.getMessage());
			}
		    }

 		    System.out.print("\r");
		    for (int k = 0; k < 120; k++)
			System.out.print(" ");
		    
		    System.out.print("\r");
		    
		}
		
	    }
	
	
	return;
    }

    private static void updateProgress(double progressPercentage) {
	final int width = 100;

	System.out.print("\rProgress: [");
	int i = 0;
	for (; i <= (int)(progressPercentage*width); i++)
	    System.out.print(".");
	for (; i < width; i++)
	    System.out.print(" ");
	System.out.print("] " + (int)(progressPercentage*100) + "%");
    }

    private static String stripExtension(String filename) {
	if (filename == null) return null;
	int pos = filename.lastIndexOf(".");
	if (pos == -1) return filename;
	return filename.substring(0, pos);
    }

    private static String createRow(String[] array) {
	if (array == null || array.length == 0) return null;
	
	String result = "('" + array[0] + "'";;
	
	for (int i = 1; i < array.length; i++)
	    result += ",'" + array[i] + "'";

	return result + ")";
    }

    private static String[] readFile(File file) {
	Object[] file_data = null;
	try {
	    file_data = Files.lines(file.toPath()).toArray();
	} catch (IOException e) {
	    System.out.println("Error: " + e.getMessage());
	    return null;
	}
    
	return Arrays.copyOf(file_data, file_data.length, String[].class);
    }
}



