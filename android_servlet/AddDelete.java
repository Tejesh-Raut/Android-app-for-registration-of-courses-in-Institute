

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet implementation class AddDelete
 */
@WebServlet("/AddDelete")
public class AddDelete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddDelete() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				String id = request.getParameter("id");			
				PrintWriter out = response.getWriter();
					String data[] = ((String) request.getParameter("data")).split(",");
					String requestType = data[0];
					String course_id = data[1];
					String sec_id = data[2];
					String semYear[] = DbHandler.getCurrentSemYear();				
					JSONObject obj = new JSONObject();
					try{
						if(semYear[0] == null){
							obj.put("status",false);							
							obj.put("message", "No semester for current time");							
						}
						else{
							boolean result = false;
							if(requestType.equalsIgnoreCase("add"))
								result = DbHandler.addCourse(id, course_id, sec_id, semYear[0], semYear[1]);
							else
								result = DbHandler.deleteCourse(id, course_id, sec_id, semYear[0], semYear[1]);
							if(!result){
								obj.put("status",false);								
								if(requestType.equalsIgnoreCase("add"))
									obj.put("message", "Error during course add");
								else
									obj.put("message", "Error during course delete");							
								
							}
							else{
								obj.put("status",true);								
								obj.put("data", DbHandler.getCoursesPresentSem(id, semYear[0], semYear[1]));
								
							}
						}
						out.print(obj);
						out.close();
						
					}
					catch(Exception e){
						System.out.println(e);
					}
					
		// TODO Auto-generated method stub
		
	}

}
