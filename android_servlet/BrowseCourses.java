

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class BrowseCourses
 */
@WebServlet("/browseCourses")
public class BrowseCourses extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BrowseCourses() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
			JSONObject obj = new JSONObject();			
			try{
				String semYear[] = DbHandler.getCurrentSemYear();
				PrintWriter out = response.getWriter();
				if(semYear[0] == null){
					obj.put("status",false);					
					obj.put("message", "No semester for current time");									
				}
				else{
					obj.put("status",true);					
					obj.put("data", DbHandler.getDepartmentsForm());					
					
				}
				out.println(obj);			
				out.close();
			}
			catch(Exception e){
		    	System.out.println(e);
		    }
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
			JSONObject obj = new JSONObject();			
			try{
				String semYear[] = DbHandler.getCurrentSemYear();
				PrintWriter out = response.getWriter();
				if(semYear[0] == null){
					obj.put("status",false);					
					obj.put("message", "No semester for current time");					
				}
				else{
					String dept = request.getParameter("dept");
					obj.put("status",true);					
					obj.put("data", DbHandler.getDepartmentsForm());					
					obj.put("datanext", DbHandler.getDepartmentCourses(dept, semYear[0], semYear[1]));
							
								
				}
				out.println(obj);			
				out.close();
			}
			catch(Exception e){
		    	System.out.println(e);
		    }
			
		
	}

}
