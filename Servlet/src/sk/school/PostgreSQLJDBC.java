package sk.school;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PostgreSQLJDBC {

	public String Send(String query){
		Connection c = null;
		JSONArray array = null;
		
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gis", "postgres", "");
	      
	         Statement stmt = c.createStatement();
	         ResultSet rs = stmt.executeQuery(query);
	         
	         array = new JSONArray();
	         while ( rs.next() ) {
	            JSONObject item = new JSONObject();
	            item.put("name", rs.getString("name"));
	            item.put("points", rs.getString("points"));
	            array.put(item);
	         }
	         
	         rs.close();
	         stmt.close();
	         c.close();
	         
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      
	      System.out.println(array.toString());
	      
	      return array != null ? array.toString() : "";
	}
}
