package com.sg.service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

public class Copy_dropDown_of_masjid extends HttpServlet {
	//private static String dbUrl = "jdbc:mysql://mysql3000.mochahost.com/solatboo_solatbooking";
	private static String dbUrl = "jdbc:mysql://mysql3000.mochahost.com/solatboo_dev";
	private static String dbClass = "com.mysql.jdbc.Driver";
	private static final long serialVersionUID = 1L;
	private static Connection conn;
	public JSONArray visitCounter;
	public JSONArray solatSchedule = new JSONArray();
	public String solatListing = "";
	public int daysDuration = 7;
	public boolean updated = true;
	String formatedSolatString = "";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Copy_dropDown_of_masjid() {
        super();
        if (visitCounter == null)
        	visitCounter = new JSONArray();
        
        
    }
    
  	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject responses = new JSONObject();
		try {
			//if (conn == null)
			//		connect();
				
				String action = request.getParameter("ac");
				
				if (action.equals("login"))
				{
					String mUserId = request.getParameter("mUserId");
					String mPassword = request.getParameter("mPassword");
					responses = getLogin(mUserId,mPassword);  
				} 
				else if (action.equals("phoneLookup"))
				{
					String phone = request.getParameter("phone");
					responses = phoneLookup(phone);  
				} 
				else if (action.equals("icLookup"))
				{
					String vIc = request.getParameter("ic");
					responses = icLookup(vIc);  
				} 
				else if (action.equals("booking") )
				{
					int scheduleId = Integer.parseInt(request.getParameter("vScheduleId"));
					int visitorId = Integer.parseInt(request.getParameter("vVisitorId"));
					int masjidId = Integer.parseInt(request.getParameter("vMasjidId"));
					int solatId = Integer.parseInt(request.getParameter("vSolatId"));
					String solatDate = request.getParameter("vSolatDate");
					String phone = request.getParameter("vPhone");
					responses = insertBooking(scheduleId,masjidId,solatId,visitorId,phone,solatDate);
					updated = true;
				}
				else if (action.equals("register") )
				{
	      			String vPhone = request.getParameter("vPhone");
					String vName = request.getParameter("vName");
					int vAge = Integer.parseInt(request.getParameter("vAge"));
					String vIc = request.getParameter("vIc");
			
					responses = registerUser(vPhone,vName,vAge,vIc);
				}
				else if (action.equals("visit") )
				{
					int masjidId = Integer.parseInt(request.getParameter("masjidId"));
					int solatId = Integer.parseInt(request.getParameter("solatId"));
					double bodyTemp = 0;
					if (request.getParameter("temp")!="")
					{
					  bodyTemp = Double.parseDouble(request.getParameter("temp"));
					}
					int visitorId = 0;
					if (request.getParameter("visitorId")!="")
					{
					   visitorId = Integer.parseInt(request.getParameter("visitorId"));
					}
					String ic = request.getParameter("ic");
					int age = 0;
					if (request.getParameter("age")!="")
					{
						age = Integer.parseInt(request.getParameter("age"));
					}
					String name = request.getParameter("name");
					String phone = request.getParameter("phone");
					//String enterDate = request.getParameter("enterDate");
					String solatDate = request.getParameter("solatDate");
					//String leaveDate = request.getParameter("leaveDate");

					//Calendar now = Calendar.getInstance();
					//java.util.TimeZone timeZone = now.getTimeZone();
					//System.out.println("Current TimeZone is : " + timeZone.getDisplayName());
					
					
					 DateTimeZone timeZoneSing = DateTimeZone.forID( "Asia/Singapore" );
				     DateTime singaporeTime = DateTime.now( timeZoneSing );
				     DateTimeFormatter dateFormat = DateTimeFormat.forPattern("Y-MM-dd hh:m:ss");
				     String enterDate = dateFormat.print(singaporeTime);
					
				     DateTime exitTime = singaporeTime.plusMinutes(45);
					 String leaveDate  = dateFormat.print(exitTime);
					 String newUser = request.getParameter("newUser");
					 
					
					 if (newUser.equals("TRUE"))
					 {
						JSONObject creds = registerUser(phone,name,age,ic);
						if (creds.has("visitorId"))
						visitorId=creds.getInt("visitorId");
					 }
					 
					 updateVisit(masjidId,solatId,bodyTemp,visitorId,ic,age,name,phone,enterDate,leaveDate, solatDate);
                     updated = true;
				}
		
			} catch (Exception e) {
				responses.put("error", e.getMessage());
				e.printStackTrace();
		    }
		    finally
		    {
	        	try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} 
		    }
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("  ");
		out.print(responses.toString());
		out.flush();
	}
  	 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject responses = new JSONObject();
		try {
			//if (conn == null)
			//		connect();
				String userIpAddress = request.getRemoteAddr();
			    System.out.println(userIpAddress);
				String action = request.getParameter("ac");
				
				if (action.equals("login"))
				{
					String mUserId = request.getParameter("mUserId");
					String mPassword = request.getParameter("mPassword");
					responses = getLogin(mUserId,mPassword);  
					
				} else if (action.equals("report"))
				{
					formatedSolatString = "";
					String solatDateString = request.getParameter("solatDate");
					daysDuration = 7;
					int solatId = 5; //Integer.parseInt(request.getParameter("solatId"));
					
					String daysStr = request.getParameter("days");
					if(daysStr!=null)
					   daysDuration = Integer.parseInt(daysStr);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String solatDatePlusOneString = null;
				
					if (solatDateString!=null || solatDateString!="")
					{
						solatDateString = format.format(new Date());
					}
					
					Date selectedDate = format.parse(solatDateString);
					Calendar c = Calendar.getInstance();
					c.setTime(selectedDate);
					c.add(Calendar.DATE, daysDuration);
					Date selectedDatePlusOne = c.getTime();
					
				    solatDateString = format.format(selectedDate);
				    solatDatePlusOneString = format.format(selectedDatePlusOne);
				
				    int mId = Integer.parseInt(request.getParameter("masjidId"));

					String reports = getReportList(mId,solatId,solatDateString,solatDatePlusOneString);
					responses.put("reports", reports);
				}
				else if (action.equals("countVisit"))
				{
					formatedSolatString = "";
					String solatDateString = request.getParameter("solatDate");
					daysDuration = 7;
					int solatId = Integer.parseInt(request.getParameter("solatId"));
					
					String daysStr = request.getParameter("days");
					if(daysStr!=null)
					   daysDuration = Integer.parseInt(daysStr);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String solatDatePlusOneString = null;
				
					if (solatDateString!=null || solatDateString!="")
					{
						solatDateString = format.format(new Date());
					}
					
					Date selectedDate = format.parse(solatDateString);
					Calendar c = Calendar.getInstance();
					c.setTime(selectedDate);
					c.add(Calendar.DATE, daysDuration);
					Date selectedDatePlusOne = c.getTime();
					
				    solatDateString = format.format(selectedDate);
				    solatDatePlusOneString = format.format(selectedDatePlusOne);
				
				    int mId = Integer.parseInt(request.getParameter("masjidId"));

				    responses = countVisit(mId,solatId,solatDateString,solatDatePlusOneString);

				}
				else if (action.equals("ins") )
				{
					int stay = 1;
					int vMasjidId = Integer.parseInt(request.getParameter("vMasjidId"));
					int vSolatId = Integer.parseInt(request.getParameter("vSolatId"));
					int vTemp = Integer.parseInt(request.getParameter("vTemp"));
					String vIc = request.getParameter("vIc");
					int vAge = Integer.parseInt(request.getParameter("vAge"));
					String vName = request.getParameter("vName");
					String vPhone = request.getParameter("vPhone");
					String vSolatDate = request.getParameter("vSolatDate");

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Calendar c = Calendar.getInstance();
					c.setTime( new Date());
					c.add(Calendar.HOUR, stay);
					
					Date visitDate = new Date();
					Date leaveDate = c.getTime();
					String vEnterDate = format.format(visitDate);
					String vLeaveDate = format.format(leaveDate);
					
					int vVisitorId = 1; //Integer.parseInt(request.getParameter("vVisitorId"));
					vVisitorId = 10;
					boolean success = addUser(vMasjidId,vSolatId,vTemp,vVisitorId,vIc,vAge,vName,vPhone,vEnterDate,vLeaveDate, vSolatDate);
				}
				else if (action.equals("ml") )
				{

					if (updated == true || (formatedSolatString!=null && formatedSolatString.length()==0))
					{
						System.out.println("***** Reload *****");
						formatedSolatString = "";
						String solatDateString = request.getParameter("solatDate");
						daysDuration = 7;
						String daysStr = request.getParameter("days");
						if(daysStr!=null)
						   daysDuration = Integer.parseInt(daysStr);
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						String solatDatePlusOneString = null;
					
						if (solatDateString!=null || solatDateString!="")
						{
							solatDateString = format.format(new Date());
						}
						
						Date selectedDate = format.parse(solatDateString);
						Calendar c = Calendar.getInstance();
						c.setTime(selectedDate);
						c.add(Calendar.DATE, daysDuration);
						Date selectedDatePlusOne = c.getTime();
						
					    solatDateString = format.format(selectedDate);
					    solatDatePlusOneString = format.format(selectedDatePlusOne);
					    String selectHeader = "<div class='solatContainer'><select class='solatSelect' onChange='this.value' id='solatChoice'>";
						String solatHeader =  "<a class=\"solatListing\" onClick=\"clickModal(%d, %d,%d,'%s','%s','%s','%s','%s')\">%s -%s @%s Space Left: <b>(%d)</b></a>";
						String solatHeaderWalkin =  "<a onClick=\"alert('%s')\">Walk-In Visit. Register Now and No reservation needed.<br> Maximum at one time: <b>(%d)</b></a>";
						String solatHeaderClose =  "<span>%s -%s @%s - <b>Session FULL</b></span>";
																			
						String masjidHeader = "<span><b>%s</b></span>";
						String masjidLabel = "";
						
						int mId = Integer.parseInt(request.getParameter("masjidId"));
						// get the MASJID LIST FIRST
						JSONArray masjids = getMasjidList(mId);
							
							for (int i=0; i<masjids.length(); i++) {
							    JSONObject item = masjids.getJSONObject(i);
							    int masjidId = item.getInt("id");
							    String masjidName = item.getString("name");
							    masjidLabel = String.format(masjidHeader,masjidName);  
							    JSONArray solatSchedule = new JSONArray();
							    solatSchedule = getSolatList(masjidId,solatDateString,solatDatePlusOneString);
							    //System.out.println(solatSchedule);
							    solatListing = selectHeader;//"";
							    for (int j=0; j<solatSchedule.length(); j++) {
								    	JSONObject solatItem = solatSchedule.getJSONObject(j);
								    	int scheduleId = solatItem.getInt("scheduleId");
								    	int maxVisitor = solatItem.getInt("maxVisitor");
								    	String solat = solatItem.getString("solat");
								    	String solatTime = solatItem.getString("solatTime");
								    	String solatType = solatItem.getString("solatType");
								    	
								        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
								    	SimpleDateFormat dateFormat = new SimpleDateFormat("hh.mm aa");
								    	Date dt = timeFormat.parse(solatTime);
								    	solatTime = dateFormat.format(dt).toString();
								    	
								    	SimpleDateFormat dateFullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								    	String solatDate = solatItem.getString("solatDate");
								    	dt = dateFullFormat.parse(solatDate);
								    	SimpleDateFormat dateHalfFormat = new SimpleDateFormat("yyyy-MM-dd");
								    	solatDate = dateHalfFormat.format(dt).toString();
								    	
								    	int solId = solatItem.getInt("solId");
								    	
								    	int currentCount = getVisitCount(masjidId,solId,solatDateString,solatDatePlusOneString);
								    	System.out.println("currentCount=" + currentCount);
								    	System.out.println("maxVisitor=" + maxVisitor);
								    	
								    	String solatListingMain = solatListing;
								    	if ((maxVisitor-currentCount)<1)
								    	{
								    		//solatListing = solatListing + String.format(solatHeaderClose,solat,solatDate, solatTime) + "<br>";
								    	} else 
								    	{
								    		solatListing = solatListing+ String.format("<option value='%d'>%s -%s @%s Space Left:&nbsp;(%d)</option>",scheduleId,solat,solatDate, solatTime, (maxVisitor-currentCount));
								    		//solatListing = solatListing + String.format(solatHeader,scheduleId,masjidId,solId,solat, masjidName,solatTime,solatDate,solatType,solat,solatDate, solatTime, (maxVisitor-currentCount)) + "<br>";
								    	}
								    	if (solat.equalsIgnoreCase("Walk-In"))
								    	{
								    		//solatListing = solatListingMain + String.format(solatHeaderWalkin,"Anytime Walk-In Visit. No Reservation Needed.\r\nYou may come in anytime.",maxVisitor) + "<br>";
								    	}
								  }
							    solatListing = solatListing + "</select></div>";
							    formatedSolatString = formatedSolatString + masjidLabel + "<br>" + solatListing  + "<br>";
							    }
							updated = false;
						}
					}
					responses.put("masjid", formatedSolatString);
		  
		
			} catch (Exception e) {
				e.printStackTrace();
				responses.put("error", e.getMessage());
		    }   
		     finally
		    {
	        	try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} 
		    }
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("  ");
		out.print(responses.toString());
		out.flush();
		//writeResponse(request, response, responses);
		//response.getWriter().append(responses.toString());
	}
	
    public void connect() {
        try {
			Class.forName(dbClass);  
		    conn = DriverManager.getConnection(dbUrl,"solatboo_sariman", "Dont4get");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } 
    }
    
	public String getReportList(int masjidId,int solatId, String startDate,String endDate){
		String output = "";
		/*
    	String sql = String.format("Select v.id as id,v.solatId as solatId, " + 
    			" m.name as masjid,s.title as solat, vs.name as name, " + 
    			" vs.ic as ic,v.telephone as tel,v.solatDate as solatDate, " + 
    			" CAST(v.startDate AS Time) as entryTime,s.solatTime as solatTime, CAST(v.endDate AS Time) as exitTime " + 
    			" FROM visit v, masjid m, solat s, visitor vs " + 
    			" WHERE ((v.masjidId=m.id) and (v.solatId=s.id) and  (v.visitorId=vs.id)) " + 
    			" AND  ( v.solatDate >= '%s' AND v.solatDate < '%s') and m.id=%d",startDate,endDate,masjidId);
    	
        String sql = String.format("SELECT schedule.id as scheduleId, schedule.maxVisitor, schedule.totalVisitor, " + 
        	    " schedule.solatDate, masjid.name  as masjid,"+ 
        	    " solat.title  as solat, schedule.solatId as solId, solat.solatType, schedule.solatId, schedule.masjidId,"+
        	    " CAST(schedule.solatTime  AS Time) as solatTime "+
        	    " CAST(v.startDate AS Time) as entryTime "+
        	    " FROM solat RIGHT OUTER JOIN "+
        	    " schedule ON solat.id = schedule.solatId LEFT OUTER JOIN "+
        	    " masjid ON schedule.masjidId = masjid.id"+
        	    " WHERE ( solatDate >= '%s' AND solatDate < '%s') and masjidId=%d"+
        	    " ORDER BY schedule.masjidId",startDate,endDate,masjidId);
*/        	    
        
        String sql = String.format("SELECT visit.id,visitor.name AS visitorName,visitor.ic,visitor.id,visitor.mobilephone,visitor.age,  " + 
    	"visit.startDate as entryTime, visit.endDate as exitTime, visit.bodyTemp,  " + 
        "schedule.maxVisitor, schedule.solatDate, schedule.totalVisitor, solat.title " + 
        "FROM schedule INNER JOIN " + 
                      "visit ON schedule.id = visit.scheduleId LEFT OUTER JOIN " + 
                      "solat ON schedule.solatId = solat.id LEFT OUTER JOIN " + 
                      "visitor ON visit.visitorId = visitor.id LEFT OUTER JOIN " + 
                      "masjid ON visit.masjidId = masjid.id" +
                      " WHERE ( schedule.solatDate >= '%s' AND schedule.solatDate < '%s') and visit.masjidId=%d",startDate,endDate,masjidId);
        
    	System.out.println(sql);
        try {
        	if ((conn == null) || conn.isClosed())
    			this.connect();
        	
        	Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

           output =  "<table ><tr>"
					+ "<th>No</th>"
					+ "<th>Name</th>"
					+ "<th>IC</th>"
					+ "<th>Tel</th>"
					+ "<th>Temp</th>"
					+ "<th>Age</th>"
					+ "<th>Time Entered</th>"
					+ "<th>Exit Time</th>"
					+ "</tr>";
			String rows = "<tr><td align=\"left\">%d</td>"
					+ "<td align=\"left\">%s</td>"
					+ "<td align=\"left\">%s</td>"
					+ "<td align=\"left\">%s</td>"
					+ "<td align=\"center\">%d</td>"
					+ "<td align=\"left\">%s</td>"
					+ "<td align=\"left\">%s</td>"
					+ "<td align=\"left\">%s</td></tr>";

			int rowNum = 1;
            while (rs.next()) {
            	String entryTime = rs.getString("entryTime");
                if (entryTime==null)entryTime="";
                String exitTime = rs.getString("exitTime");
                if (exitTime==null)exitTime="";
                
              	String row = String.format(
              			rows, rowNum++,
              			rs.getString("visitorName"),
              			rs.getString("ic"),
              			rs.getString("mobilephone"),
              			rs.getInt("bodyTemp"),
              			rs.getString("age"),
              		    entryTime,
              			exitTime);
              	//solObj.put("id",rs.getInt("id"));
              	output=output+row;
            }
            output=output+"</table>";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }finally
	    {

	    }
        	return output;
    }
 
	private JSONObject countVisit(int masjidId,int solatId, String startDate,String endDate)
	{
		JSONObject response = new JSONObject();
    	String sql = String.format("Select count(*) as count from visit " +  
    		    " WHERE ( solatDate >= '%s' AND solatDate < '%s') and solatId=%d and masjidId=%d",startDate,endDate,solatId,masjidId);
	System.out.println(sql);
        try {
        	if ((conn == null) || conn.isClosed())
    			this.connect();
        	
        	Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
            	int count = rs.getInt("count");
            	response.put("totalVisitors", count);
            	
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return response;
    }

	   
    private int getVisitCount(int masjidId,int solatId, String startDate,String endDate)
    {
    	String sql = String.format("Select count(*) as count from visit " +  
        		    " WHERE ( solatDate >= '%s' AND solatDate < '%s') and solatId=%d and masjidId=%d",startDate,endDate,solatId,masjidId);
    	System.out.println(sql);
        	int count = 0;
            try {
            	if ((conn == null) || conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                	count = rs.getInt("count");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }finally
		    {

		    }
            return count;
    }

    private boolean addUser(int masjidId,int solatId, int bodyTemp,int visitorId, String ic,
    		                int age, String name, String phone, String enterDate, String leaveDate, String solatDate)
    {
    	boolean success = false;
    	String sql = String.format("INSERT INTO `visit` " + 
    	             "(`startDate`, `endDate`, `bodyTemp`, `visitorId`, `masjidId`, `solatId`, `telephone`, `solatDate`)" +
    			     " VALUES ('%s', '%s', '%d', '%d', '%d', '%d', '%s', '%s');",
    			           enterDate,leaveDate,bodyTemp,visitorId,masjidId,solatId,phone,solatDate);
    	System.out.println(sql);
            try {
            	if ((conn == null) || conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                success = true;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }finally
		    {

		    }
            return success;
    }

    private boolean insertVisit(int masjidId,int solatId, int bodyTemp,int visitorId, String ic,
            int age, String name, String phone, String enterDate, String leaveDate, String solatDate)
    {
    	JSONObject creds = new JSONObject();
    	boolean success = false;
    	String sql = String.format("INSERT INTO `visit` " + 
    	     "(`startDate`, `endDate`, `bodyTemp`, `visitorId`, `masjidId`, `solatId`, `telephone`, `solatDate`)" +
    	     " VALUES ('%s', '%s', '%d', '%d', '%d', '%d', '%s', '%s');",
    	           enterDate,leaveDate,bodyTemp,visitorId,masjidId,solatId,phone,solatDate);
    	System.out.println(sql);
    	try {
    	if ((conn == null) || conn.isClosed())
    		this.connect();

    	Statement stmt = conn.createStatement();
    	stmt.executeUpdate(sql);
    	success = true;
    	} catch (SQLException e) {
    	System.out.println(e.getMessage());
    	}finally
	    {

	    }
    	
    	
        if (success)
        {
        	creds.put("status", "PASS");
        } else {
        	creds.put("status", "FAILS");
        }
    	return success;
    }
    
    private boolean updateVisit(int masjidId,int solatId, double bodyTemp,int visitorId, String ic,
            int age, String name, String phone, String enterDate, String leaveDate, String solatDate)
    {
    	JSONObject creds = new JSONObject();
    	boolean success = false;
    	String sql = String.format("UPDATE `visit`" +
    			" SET `startDate` = '%s', `endDate` = '%s'," +
    			" `bodyTemp` = '%f', `telephone` = '%s' " +
    			" WHERE (`visitorId` = %d) and (`solatId` = %d) and (`solatDate` = '%s');",
    			enterDate,leaveDate,bodyTemp,phone,visitorId,solatId,solatDate);

    	System.out.println(sql);
    	try {
    	if ((conn == null) || conn.isClosed())
    		this.connect();

    	Statement stmt = conn.createStatement();
    	stmt.executeUpdate(sql);
    	success = true;
    	} catch (SQLException e) {
    	System.out.println(e.getMessage());
    	}finally
	    {

	    }
    	
    	
        if (success)
        {
        	creds.put("status", "PASS");
        } else {
        	creds.put("status", "FAILS");
        }
    	return success;
    }
    
    private JSONObject insertBooking(int scheduleId, int masjidId,int solatId,int visitorId, String phone, String solatDate)
    {
    	JSONObject confirmation = new JSONObject();
    	boolean success = false;
    	String sql = String.format("INSERT INTO `visit` " + 
    	     "( `scheduleId`, `visitorId`, `masjidId`, `solatId`, `telephone`, `solatDate`,`bodyTemp`)" +
    	     " VALUES ('%d','%d', '%d', '%d', '%s', '%s','%d');",
    	     scheduleId,visitorId,masjidId,solatId,phone,solatDate,0);
    	System.out.println(sql);
    	int count = 0;
    	try {
    	if ((conn == null) || conn.isClosed())
    		this.connect();

    	Statement stmt = conn.createStatement();
    	stmt.executeUpdate(sql);
    	success = true;
    	updated = false;
    	} catch (SQLException e) {
    	System.out.println(e.getMessage());
    	}finally
	    {

	    }
    	
        if (success)
        {
        	confirmation.put("status", "PASS");
        } else {
        	confirmation.put("status", "FAILS");
        }
    	return confirmation;
    }
    
    private JSONObject registerUser(String vPhone,String vName,int vAge, String vIc)
    {
    	JSONObject confirmation = new JSONObject();
    	boolean success = false;
    	String sql = String.format("INSERT INTO `visitor` " + 
    								" (`ic`, `name`, `mobilephone`, `age`) " +
    								" VALUES ('%s', '%s','%s', '%d');",vIc, vName, vPhone,vAge);
    	System.out.println(sql);
    	try {
    	if ((conn == null) || conn.isClosed())
    		this.connect();

    	Statement stmt = conn.createStatement();
    	stmt.executeUpdate(sql);
    	
    	success = true;
    	updated = false;
    	} catch (SQLException e) {
    	   System.out.println(e.getMessage());
    	   confirmation.put("status", e.getMessage());
    	}
    	finally
	    {

	    }
    	
        if (success)
        {
           confirmation = icLookup(vIc);//phoneLookup(vPhone);
        } else {
        	confirmation.put("status", "FAILS");
        }
    	return confirmation;
    }
    
    private JSONObject getLogin(String userId, String password)
    {
    	JSONObject creds = new JSONObject();
    	creds.put("userId", userId);
    	String sql = String.format("Select u.*, m.name as masjidName from users u, " + 
    			  "masjid m WHERE userId = '%s' and password='%s' and u.masjidId = m.id;",userId,password);
    	System.out.println(sql);
        	int count = 0;
            try {
            	if ((conn == null) || conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                boolean success=false;
                while (rs.next()) {
                	String masjidName = rs.getString("masjidName");
                	creds.put("masjidName", masjidName);
                	String userName = rs.getString("name");
                	creds.put("userName", userName);
                	 success=true;
                }
                if (success)
                {
                	creds.put("status", "PASS");
                } else {
                	creds.put("status", "FAILS");
                }
            } catch (SQLException e) {
            	creds.put("status", "FAILS");
            	creds.put("error", e.getMessage());
                System.out.println(e.getMessage());
            }finally
		    {

		    }
            return creds;
    }
    
    private JSONObject icLookup(String vIc)
    {
    	JSONObject creds = new JSONObject();
    	String sql = String.format("SELECT id,ic,name,mobilephone,age FROM visitor WHERE ic='%s'",vIc);
    	System.out.println(sql);
            try {
            	if ((conn == null) || conn.isClosed())
        			this.connect();
            	
             	Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                boolean success=false;
                while (rs.next()) {
                	int visitorId = rs.getInt("id");
                	creds.put("visitorId", visitorId);
                	String ic = rs.getString("ic");
                	creds.put("ic", ic);
                	String name = rs.getString("name");
                	creds.put("name", name);
                	String mobilephone = rs.getString("mobilephone");
                	creds.put("phone", mobilephone);
                	int age = rs.getInt("age");
                	creds.put("age", age);
                	JSONObject bookings = findBookings(visitorId);
                	creds.put("bookings", bookings);
                 	success=true;
                }
                if (success)
                {
                	creds.put("status", "PASS");
                } else {
                	creds.put("status", "FAILS");
                }
            } catch (SQLException e) {
            	creds.put("status", "FAILS");
            	creds.put("error", e.getMessage());
                System.out.println(e.getMessage());
            }finally
		    {

		    }
            return creds;
    }
    
    private JSONObject phoneLookup(String phone)
    {
    	JSONObject creds = new JSONObject();
    	String sql = String.format("SELECT id,ic,name,mobilephone,age FROM visitor WHERE mobilephone='%s'",phone);
    	System.out.println(sql);
            try {
            	if ((conn == null) || conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                boolean success=false;
                while (rs.next()) {
                	int visitorId = rs.getInt("id");
                	creds.put("visitorId", visitorId);
                	String ic = rs.getString("ic");
                	creds.put("ic", ic);
                	String name = rs.getString("name");
                	creds.put("name", name);
                	String mobilephone = rs.getString("mobilephone");
                	creds.put("phone", mobilephone);
                	int age = rs.getInt("age");
                	creds.put("age", age);
                	JSONObject bookings = findBookings(visitorId);
                	creds.put("bookings", bookings);
                 	success=true;
                }
                if (success)
                {
                	creds.put("status", "PASS");
                } else {
                	creds.put("status", "FAILS");
                }
            } catch (SQLException e) {
            	creds.put("status", "FAILS");
            	creds.put("error", e.getMessage());
                System.out.println(e.getMessage());
            }finally
		    {

		    }
            return creds;
    }
    
    private JSONObject userLookup(String phone, String vname)
    {
    	JSONObject creds = new JSONObject();
    	String sql = String.format("SELECT id,ic,name,mobilephone,age FROM visitor WHERE mobilephone='%s' and name='%s'",phone,vname);
    	System.out.println(sql);
            try {
            	if ((conn == null) || conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                boolean success=false;
                while (rs.next()) {
                	int visitorId = rs.getInt("id");
                	creds.put("visitorId", visitorId);
                	String ic = rs.getString("ic");
                	creds.put("ic", ic);
                	String name = rs.getString("name");
                	creds.put("name", name);
                	String mobilephone = rs.getString("mobilephone");
                	creds.put("phone", mobilephone);
                	int age = rs.getInt("age");
                	creds.put("age", age);
                	JSONObject bookings = findBookings(visitorId);
                	creds.put("bookings", bookings);
                 	success=true;
                }
                if (success)
                {
                	creds.put("status", "PASS");
                } else {
                	creds.put("status", "FAILS");
                }
            } catch (SQLException e) {
            	creds.put("status", "FAILS");
            	creds.put("error", e.getMessage());
                System.out.println(e.getMessage());
            }finally
		    {

		    }
            return creds;
    }
    
    private JSONObject findBookings(int visitorId)
    {
    	JSONObject resp = new JSONObject();
    	JSONArray bookings = new JSONArray();
    	String sql = String.format("SELECT visit.id, visit.telephone AS tel, visit.scheduleId, visit.solatId, visit.solatDate," + 
    			" visit.visitorId, masjid.name AS masjid, solat.title AS solat, solat.solatTime, " +
    			" solat.solatType, visitor.name, visitor.age, visitor.mobilephone" +
    			" FROM visit LEFT OUTER JOIN" +
                " visitor ON visit.visitorId = visitor.id LEFT OUTER JOIN" +
                " solat ON visit.solatId = solat.id LEFT OUTER JOIN" +
                " masjid ON visit.masjidId = masjid.id" +
    	        " WHERE (visit.solatDate >= '%s') and visit.visitorId=%d;",getCurrentDate(),visitorId);
    	System.out.println(sql);
            try {
            	if ((conn == null) || conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                boolean success=false;
                while (rs.next()) {
                	JSONObject visits = new JSONObject();
                	visits.put("visitId", rs.getInt("id"));
                	visits.put("solatId", rs.getInt("solatId"));
                	visits.put("scheduleId", rs.getInt("scheduleId"));
                	visits.put("tel", rs.getString("tel"));
                	visits.put("name", rs.getString("name"));
                	visits.put("solatDate", rs.getString("solatDate"));
                	visits.put("solatType", rs.getString("solatType"));
                	visits.put("masjid", rs.getString("masjid"));               	
                	visits.put("solat",  rs.getString("solat"));  
                	visits.put("solatTime", rs.getString("solatTime"));  
                	bookings.put(visits);
                	
                 	success=true;
                }
                resp.put("visits",bookings);
                if (success)
                {
                	resp.put("status", "PASS");
                } else {
                	resp.put("status", "FAILS");
                }
            } catch (SQLException e) {
            	resp.put("status", "FAILS");
            	resp.put("error", e.getMessage());
                System.out.println(e.getMessage());
            }
            finally
		    {

		    }
            return resp;
    }
    
    private String getCurrentDate()
    {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime( new Date() );
		Date visitDate = new Date();
		return format.format(visitDate);
    }
    
    private String getDurationDate(int stay)
    {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime( new Date() );
		c.add(Calendar.DAY_OF_MONTH, stay);
		return format.format(c.getTime());
    }

   /**
     * select all rows in the warehouses table
     */
    @SuppressWarnings("finally")
	public JSONArray getSolatList(int masjidId, String startDate,String endDate){
    	
   /* 	
    String sql = String.format("SELECT sol.title as solat,sol.solatTime as solatTime, m.name as masjid, s.solatDate as solatDate,sol.solatType as solatType , " + 
    		    " m.id as masjidId, sol.id as solId, sol.maxVisitor as maxVisitor, s.id as scheduleId " + 
    		    " FROM schedule as s,masjid as m, solat as sol " + 
    		    " WHERE m.id = s.masjidId and sol.id = s.solatId " +
    		    " AND ( solatDate >= '%s' AND solatDate < '%s') and m.id=%d",startDate,endDate,masjidId);
    */ 
   
    String sql = String.format("SELECT schedule.id as scheduleId, schedule.maxVisitor, schedule.totalVisitor, " + 
    " schedule.solatDate, masjid.name  as masjid,"+ 
    " solat.title  as solat, schedule.solatId as solId, solat.solatType, schedule.solatId, schedule.masjidId, CAST(schedule.solatTime  AS Time) as solatTime "+
    " FROM solat RIGHT OUTER JOIN "+
    " schedule ON solat.id = schedule.solatId LEFT OUTER JOIN "+
    " masjid ON schedule.masjidId = masjid.id"+
    " WHERE ( solatDate >= '%s' AND solatDate < '%s') and masjidId=%d"+
    " ORDER BY schedule.masjidId",startDate,endDate,masjidId);
    
    	/*
    	String sql = String.format("SELECT sol.title as solat,sol.solatTime as solatTime, m.name as masjid, s.solatDate as solatDate,sol.solatType as solatType , " + 
    		    " m.id as masjidId, sol.id as solId, (SELECT DISTINCT(maxVisitor) From schedule WHERE solatId=sol.id AND masjidId=4) as maxVisitor, s.id as scheduleId " + 
    		    " FROM schedule as s,masjid as m, solat as sol " + 
    		    " WHERE m.id = s.masjidId and sol.id = s.solatId " +
    		    " AND ( solatDate >= '%s' AND solatDate < '%s') and m.id=%d",startDate,endDate,masjidId);
    	*/
    	
    	System.out.println(sql);
    	JSONArray solatList = new JSONArray();
        try {
        	if (conn==null || conn.isClosed())
    			this.connect();
        	
        	Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
              	JSONObject solObj = new JSONObject();
              	solObj.put("solat", rs.getString("solat"));
              	solObj.put("solatTime", rs.getString("solatTime"));
              	solObj.put("solatDate", rs.getString("solatDate"));
              	solObj.put("solatType", rs.getString("solatType"));
            	solObj.put("solId", rs.getInt("solId"));
            	solObj.put("maxVisitor",rs.getInt("maxVisitor"));
            	solObj.put("scheduleId",rs.getInt("scheduleId"));
            	
            	solatList.put(solObj);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        finally
	    {

	    }
        	return solatList;
    }
    
    @SuppressWarnings("finally")
 	public JSONArray getMasjidList(int masjidId){
         String sql = "SELECT id,name FROM masjid";
         if(masjidId>0)
         {
        	 sql = sql + " WHERE id=" + masjidId;
         }
        		 
         JSONArray masjidListObj = new JSONArray();
         try {
         	if ((conn == null) || conn.isClosed())
     			this.connect();
         	
         	Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql);
             
             // loop through the result set
             while (rs.next()) {
             	JSONObject msjObj = new JSONObject();
             	msjObj.put("id", rs.getInt("id"));
             	msjObj.put("name", rs.getString("name"));
             	masjidListObj.put(msjObj);
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
         } finally
		    {

		    }
         	return masjidListObj;
     }
    
 	public JSONArray getSolatList(int masjidId){
        String sql = "SELECT id,name FROM masjid;";
        JSONArray masjidListObj = new JSONArray();
        try {
        	if ((conn == null) || conn.isClosed())
    			this.connect();
        	
        	Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            
            // loop through the result set
            while (rs.next()) {
            	JSONObject msjObj = new JSONObject();
            	msjObj.put("id", rs.getInt("id"));
            	msjObj.put("name", rs.getString("name"));
            	masjidListObj.put(msjObj);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally
	    {

	    }
        	return masjidListObj;
    }
    
	private void writeResponse(HttpServletRequest request, HttpServletResponse response, JSONObject json) throws Exception {
		Writer writer = null;

		try {
			// Prevent browsers from returning cached response on subsequent
			// request
			response.addHeader("Cache-Control", "no-cache");

			// GZip JSON response if client supports it
			String acceptedEncodings = request.getHeader("Accept-Encoding");
			if (acceptedEncodings != null && acceptedEncodings.indexOf("gzip") >= 0) {
				if (!response.isCommitted())
					response.setBufferSize(65536); // since many times response
													// is larger than default
													// buffer (4096)
				response.setHeader("Content-Encoding", "gzip");
				response.setContentType("text/plain"); // must be text/plain for
														// firebug
				GZIPOutputStream gzos = new GZIPOutputStream(response.getOutputStream());
				writer = new OutputStreamWriter(gzos, "UTF-8");
				// Add secure JSON prefix
				writer.write("{}&&");
				writer.flush();
			//	json.serialize(writer);
			} else {
				response.setContentType("text/plain"); // must be text/plain for
														// firebug
				response.setCharacterEncoding("UTF-8");
				writer = response.getWriter();
				writer.write("{}&&");
				writer.flush();
			//	json.serialize(writer);
			}
		} catch (Exception e) {
			//LOGGER.error(LOGGER, e);
			throw e;
		} finally {
			if (writer != null)
				writer.close();
		}
	}
    
}
