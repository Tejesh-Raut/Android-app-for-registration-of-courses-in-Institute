

import java.sql.*;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DbHandler {
	// connection strings
	private static String connString = "jdbc:postgresql://localhost:5432/postgres";
	private static String userName = "postgres";
	private static String passWord = "";
	
	
	public static JSONObject authenticate(String id, String password){		
		JSONObject obj = new JSONObject();
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			String query = "select count(*) from password where id=? and password=?;";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, id);
			preparedStmt.setString(2, password);
			ResultSet result =  preparedStmt.executeQuery();
			result.next();
			boolean ans = (result.getInt(1) > 0); 
			preparedStmt.close();
			conn.close();
			if(ans==true){
				obj.put("status",true);				
				obj.put("data", id);
				
			}
			else{						
					obj.put("status",false);
					obj.put("message", "Authentication Failed");					
			}			
		} 
		catch(Exception e){
			System.out.println(e);
		}
		return obj;
	}
	
	
	public static JSONArray getCoursesPresentSem(String id, String sem, String year){
		
		JSONArray jsonObj = new JSONArray();
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			String query = "select course_id, sec_id, title, dept_name, credits from takes natural join course where id=? and semester=? and year=?";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, id);
			preparedStmt.setString(2, sem);
			preparedStmt.setInt(3, Integer.parseInt(year));
			ResultSet result =  preparedStmt.executeQuery();
			jsonObj = ResultSetConverter(result);
			preparedStmt.close();
			conn.close();
			 
		} catch(Exception e){
			System.out.println(e);
		}
		
		return jsonObj;
	}
	private static JSONArray ResultSetConverter(ResultSet rs) throws SQLException, JSONException {
		
		// TODO Auto-generated method stub
		JSONArray json = new JSONArray();
		JSONObject jsonResponse = new JSONObject();
	    ResultSetMetaData rsmd = rs.getMetaData();
	    while(rs.next()) {
	        int numColumns = rsmd.getColumnCount();
	        JSONObject obj = new JSONObject();

	        for (int i=1; i<numColumns+1; i++) {
	          String column_name = rsmd.getColumnName(i);

	          if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
	           obj.put(column_name, rs.getArray(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
	           obj.put(column_name, rs.getInt(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
	           obj.put(column_name, rs.getBoolean(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
	           obj.put(column_name, rs.getBlob(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
	           obj.put(column_name, rs.getDouble(column_name)); 
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
	           obj.put(column_name, rs.getFloat(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
	           obj.put(column_name, rs.getInt(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
	           obj.put(column_name, rs.getNString(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
	           obj.put(column_name, rs.getString(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
	           obj.put(column_name, rs.getInt(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
	           obj.put(column_name, rs.getInt(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
	           obj.put(column_name, rs.getDate(column_name));
	          }
	          else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
	          obj.put(column_name, rs.getTimestamp(column_name));   
	          }
	          else{
	           obj.put(column_name, rs.getObject(column_name));
	          }
	        }

	        json.put(obj);
	      }
	    return json;
	}

	public static String[] getCurrentSemYear(){
		String ans[] = new String[2];
		ans[0] = null;
		ans[1] = null;
		Timestamp timestamp = new Timestamp(new Date().getTime());
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			String query = "select semester, year from regnDates where startTS <= ? and endTS >= ?;";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setTimestamp(1, timestamp);
			preparedStmt.setTimestamp(2, timestamp);
			ResultSet result =  preparedStmt.executeQuery();
			while(result.next()){
				ans[0] = result.getString(1);
				ans[1] = result.getString(2);
			} 
			preparedStmt.close();
			conn.close();
		} catch(Exception e){
			System.out.println(e);
		}
		return ans;
	}
	
	public static boolean addCourse(String id, String course_id, String sec_id, String sem, String year){
		try{
			// Create the connection
			JSONArray jsonObj = new JSONArray();
			JSONObject obj = new JSONObject();
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			String query = "insert into takes values(?,?,?,?,?);";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, id);
			preparedStmt.setString(2, course_id);
			preparedStmt.setString(3, sec_id);
			preparedStmt.setString(4, sem);
			preparedStmt.setInt(5, Integer.parseInt(year));
			Integer res = preparedStmt.executeUpdate();
			preparedStmt.close();
			conn.close();
			if  (res == 1)
                return true;
            else 
            	return false; 
		} catch(Exception e){
			System.out.println(e);
		}
		return false;
	}

	public static boolean deleteCourse(String id, String course_id, String sec_id, String sem, String year){
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			//First check whether the course present or not
			String preparedStmnt = "select count(*)  from takes where id= ? and course_id= ? and sec_id= ? and semester= ? and year = ?;";
			PreparedStatement query = conn.prepareStatement(preparedStmnt);
			query.setString(1, id);
			query.setString(2, course_id);
			query.setString(3, sec_id);
			query.setString(4, sem);
			query.setInt(5, Integer.parseInt(year));
			ResultSet result = query.executeQuery();
			result.next();
			boolean ans = (result.getInt(1) > 0); 
			if (ans){
				String preparedStmt = "delete from takes where id= ? and course_id= ? and sec_id= ? and semester= ? and year = ?;";
				query = conn.prepareStatement(preparedStmt);
				query.setString(1, id);
				query.setString(2, course_id);
				query.setString(3, sec_id);
				query.setString(4, sem);
				query.setInt(5, Integer.parseInt(year));
				query.executeUpdate();
				query.close();
				conn.close();
				return true;
			}
			
		} catch(Exception e){
			System.out.println(e);
		}
		return false;
	}
	
	public static JSONObject getCoursesOptions(String id, String sem, String year){				
		JSONArray jsonObjRs = new JSONArray();
		JSONObject obj = new JSONObject();
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			String query =
                            "(select course_id, sec_id, title, dept_name, credits from section natural join course where semester= ? and year= ?)" 
			   + "except (select course_id, sec_id, title, dept_name, credits from takes natural join course where id= ? and semester=? and year = ?)";
			PreparedStatement stmt = conn.prepareStatement(query);
                        stmt.setString(1, sem);
                        stmt.setInt(2, Integer.parseInt(year));
                        stmt.setString(3, id);
                        stmt.setString(4, sem);
                        stmt.setInt(5, Integer.parseInt(year));
			ResultSet result =  stmt.executeQuery();
			jsonObjRs = ResultSetConverter(result);
			obj.put("status",true);			
			obj.put("data", jsonObjRs);			
			stmt.close();
			conn.close();
		} catch(Exception e){
			System.out.println(e);
		}
		
		return obj;
	}

	public static JSONArray getDepartmentsForm(){		
		JSONArray jsonObj = new JSONArray();
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			Statement stmt = conn.createStatement();
			String query = "select dept_name from department";
			ResultSet result =  stmt.executeQuery(query);
			jsonObj = ResultSetConverter(result);
			stmt.close();
			conn.close();
		} catch(Exception e){
			System.out.println(e);
		}
		
		return jsonObj;
	}

	public static JSONArray getDepartmentCourses(String dept, String sem, String year){
		JSONArray jsonObj = new JSONArray();
		try{
			// Create the connection
			Connection conn = DriverManager.getConnection(connString, userName, passWord);
			String query = "select course_id, sec_id, title, credits from section natural join course "
                               + "where dept_name= ? and semester= ? and year= ?";
			PreparedStatement stmt = conn.prepareStatement(query);
                        stmt.setString(1, dept);
                        stmt.setString(2, sem);
                        stmt.setInt(3, Integer.parseInt(year));
 
			ResultSet result =  stmt.executeQuery();
			jsonObj = ResultSetConverter(result);
			stmt.close();
			conn.close();
		} catch(Exception e){
			System.out.println(e);
		}
		
		return jsonObj;
	}
}
