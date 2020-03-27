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

import org.json.JSONArray;
import org.json.JSONObject;

public class masjid extends HttpServlet {
	private static String dbUrl = "jdbc:mysql://mysql3000.mochahost.com/solatboo_solatbooking";
	private static String dbClass = "com.mysql.jdbc.Driver";
	private static final long serialVersionUID = 1L;
	private static Connection conn;
	public JSONArray visitCounter;
	public JSONArray solatSchedule = new JSONArray();
	public String solatListing = "";
	public int daysDuration = 7;
	public boolean updated = false;
	String formatedSolatString = "";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public masjid() {
        super();
        if (visitCounter == null)
        	visitCounter = new JSONArray();
        
        
    }
    
    public void connect() {
        try {
			Class.forName(dbClass);  
		    conn = DriverManager.getConnection(dbUrl,"solatboo_sariman", "Dont4get");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } 
    }
    
    private int getVisitCount(int masjidId,int solatId, String startDate,String endDate)
    {
    	String sql = String.format("Select count(*) as count from visit " +  
        		    " WHERE ( solatDate >= '%s' AND solatDate < '%s') and solatId=%d and masjidId=%d",startDate,endDate,solatId,masjidId);
        	int count = 0;
            try {
            	if ((conn != null) && conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                	count = rs.getInt("count");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return count;
    }

    private boolean addUser(int masjidId,int solatId, int bodyTemp,int visitorId, String ic,
    		                int age, String name, String phone, String enterDate, String leaveDate, String solatDate)
    {
    	boolean success = false;
    	String sql = String.format("INSERT INTO `solatboo_solatbooking`.`visit` " + 
    	             "(`startDate`, `endDate`, `bodyTemp`, `visitorId`, `masjidId`, `solatId`, `telephone`, `solatDate`)" +
    			     " VALUES ('%s', '%s', '%d', '%d', '%d', '%d', '%s', '%s');",
    			           enterDate,leaveDate,bodyTemp,visitorId,masjidId,solatId,phone,solatDate);
        	int count = 0;
            try {
            	if ((conn != null) && conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                success = true;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            return success;
    }

    private JSONObject insertVisit(int masjidId,int solatId, int bodyTemp,int visitorId, String ic,
            int age, String name, String phone, String enterDate, String leaveDate, String solatDate)
    {
    	JSONObject creds = new JSONObject();
    	creds.put("status", "PASS");
    	return creds;
    	/*
    	boolean success = false;
    	String sql = String.format("INSERT INTO `solatboo_solatbooking`.`visit` " + 
    	     "(`startDate`, `endDate`, `bodyTemp`, `visitorId`, `masjidId`, `solatId`, `telephone`, `solatDate`)" +
    	     " VALUES ('%s', '%s', '%d', '%d', '%d', '%d', '%s', '%s');",
    	           enterDate,leaveDate,bodyTemp,visitorId,masjidId,solatId,phone,solatDate);
    	int count = 0;
    	try {
    	if ((conn != null) && conn.isClosed())
    		this.connect();

    	Statement stmt = conn.createStatement();
    	stmt.executeUpdate(sql);
    	success = true;
    	} catch (SQLException e) {
    	System.out.println(e.getMessage());
    	}
    	return success;
    	*/
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
            	if ((conn != null) && conn.isClosed())
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
            }
            return creds;
    }
    
    private JSONObject phoneLookup(String phone)
    {
    	JSONObject creds = new JSONObject();
    	String sql = String.format("SELECT id,ic,name,mobilephone,age FROM visitor WHERE mobilephone='%s'",phone);
    	System.out.println(sql);
            try {
            	if ((conn != null) && conn.isClosed())
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
            }
            return creds;
    }
    
    private JSONObject findBookings(int visitorId)
    {
    	JSONObject resp = new JSONObject();
    	JSONArray bookings = new JSONArray();
    	String sql = String.format("Select v.id as id,v.telephone as tel,vs.name as name,v.solatId as solatId,"+ 
    	                           " v.solatDate as solatDate,m.name as masjid,s.title as solat,s.solatTime as solatTime "+
    			                   " FROM visit v, masjid m, solat s, visitor vs  " + 
    	                           " WHERE ((v.masjidId=m.id) and (v.solatId=s.id) and (v.visitorId=vs.id)) " +
    			                   " and  (v.solatDate >= '%s') and v.visitorId=%d;",getCurrentDate(),visitorId);
    	System.out.println(sql);
            try {
            	if ((conn != null) && conn.isClosed())
        			this.connect();
            	
            	Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                boolean success=false;
                while (rs.next()) {
                	JSONObject visits = new JSONObject();
                	int visitId = rs.getInt("id");
                	visits.put("visitId", visitId);
                	int solatId = rs.getInt("solatId");
                	visits.put("solatId", solatId);
                	String tel = rs.getString("tel");
                	visits.put("tel", tel);
                	String name = rs.getString("name");
                	visits.put("name", name);
                	String solatDate = rs.getString("solatDate");
                	visits.put("solatDate", solatDate);
                	String masjid = rs.getString("masjid");
                	visits.put("masjid", masjid);               	
                	String solat = rs.getString("solat");
                	visits.put("solat", solat);  
                	String solatTime = rs.getString("solatTime");
                	visits.put("solatTime", solatTime);  
                	bookings.put(visits);
                	
                 	success=true;
                }
                resp.put("visits	",bookings);
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
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject responses = new JSONObject();
		try {
			if (conn == null)
					connect();
				
				String action = request.getParameter("ac");
				
				if (action.equals("login"))
				{
					String mUserId = request.getParameter("mUserId");
					String mPassword = request.getParameter("mPassword");
					responses = getLogin(mUserId,mPassword);  
					
				} 
				else if (action.equals("ins") )
				{
					int stay = 1;
					int vMasjidId = Integer.parseInt(request.getParameter("vMasjidId"));
					int vSolatId = Integer.parseInt(request.getParameter("vSolatId"));
					int vTemp = Integer.parseInt(request.getParameter("vTemp"));
					String vIC = request.getParameter("vIC");
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
					boolean success = addUser(vMasjidId,vSolatId,vTemp,vVisitorId,vIC,vAge,vName,vPhone,vEnterDate,vLeaveDate, vSolatDate);
				}
				else if (action.equals("ml") )
				{
					updated = false;
					if (updated == false)
					{
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
					
						String solatHeader =  "<span onClick=\"clickModal(%d,%d,'%s','%s','%s','%s','%d')\">%s -%s @%s Space Left: <b>(%d)</b></span>";
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
							    solatSchedule = getSolatList(masjidId,solatDateString,solatDatePlusOneString);
							    
							    solatListing = "";
							    for (int j=0; j<solatSchedule.length(); j++) {
								    	JSONObject solatItem = solatSchedule.getJSONObject(j);
								    	int maxVisitor = solatItem.getInt("maxVisitor");
								    	String solat = solatItem.getString("solat");
								    	String solatTime = solatItem.getString("solatTime");
								    	
								        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
								    	SimpleDateFormat dateFormat = new SimpleDateFormat("hh.mm aa");
								    	Date dt = timeFormat.parse(solatTime);
								    	solatTime = dateFormat.format(dt).toString();
								    	
								    	SimpleDateFormat dateFullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								    	String solatDate = solatItem.getString("solatDate");
								    	dt = dateFullFormat.parse(solatDate);
								    	SimpleDateFormat dateHalfFormat = new SimpleDateFormat("yyyy/MM/dd");
								    	solatDate = dateHalfFormat.format(dt).toString();
								    	
								    	int solId = solatItem.getInt("solId");
								    	
								    	int currentCount = getVisitCount(masjidId,solId,solatDateString,solatDatePlusOneString);
								    	
								    	solatListing = solatListing + String.format(solatHeader,masjidId,solId,solat, masjidName,solatTime,solatDate,1,solat,solatDate, solatTime, (maxVisitor-currentCount)) + "<br>";
								  }
							    formatedSolatString = formatedSolatString + masjidLabel + "<br>" + solatListing + "<br>";
							    }
							updated = true;
						}
					}
					responses.put("masjid", formatedSolatString);
		  
		
			} catch (Exception e) {
				responses.put("error", e.getMessage());
		    } finally
		    {
	        	try {
					conn.close();
					conn = null;
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

    



	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject responses = new JSONObject();
		try {
			if (conn == null)
					connect();
				
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
				else if (action.equals("visit") )
				{
					int masjidId = 0;
					int solatId = 0;
					int bodyTemp = 0;
					int visitorId = 0;
					 String ic = null;
					 int age = 0;
					 String name = null;
					 String phone = null;
					 String enterDate = null;
					 String leaveDate = null;
					 String solatDate = null;
				   //insertVisit(masjidId,solatId,bodyTemp,visitorId,ic,
				//	           age,name,phone,enterDate,leaveDate,solatDate);
				   
					responses.put("status","SUCCESS");
				}
				else if (action.equals("ml") )
				{
					
				}
		  
		
			} catch (Exception e) {
				responses.put("error", e.getMessage());
		    } finally
		    {
	        	try {
					conn.close();
					conn = null;
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

   /**
     * select all rows in the warehouses table
     */
    @SuppressWarnings("finally")
	public JSONArray getSolatList(int masjidId, String startDate,String endDate){
    	
    	String sql = String.format("SELECT sol.title as solat,sol.solatTime as solatTime, m.name as masjid, s.solatDate as solatDate, " + 
    		    " m.id as masjidId, sol.id as solId, sol.maxVisitor as maxVisitor, s.id as scheduleId " + 
    		    " FROM schedule as s,masjid as m, solat as sol " + 
    		    " WHERE m.id = s.masjidId and sol.id = s.solatId " +
    		    " AND ( solatDate >= '%s' AND solatDate < '%s') and m.id=%d",startDate,endDate,masjidId);
    	
    	JSONArray solatList = new JSONArray();
        try {
        	if ((conn != null) && conn.isClosed())
    			this.connect();
        	
        	Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
              	JSONObject solObj = new JSONObject();
              	solObj.put("solat", rs.getString("solat"));
              	solObj.put("solatTime", rs.getString("solatTime"));
              	solObj.put("solatDate", rs.getString("solatDate"));
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
        	return solatList;
        }
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
         	if ((conn != null) && conn.isClosed())
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
         }
         finally
         {
         	return masjidListObj;
         }
     }
    
 	public JSONArray getSolatList(int masjidId){
        String sql = "SELECT id,name FROM masjid;";
        JSONArray masjidListObj = new JSONArray();
        try {
        	if ((conn != null) && conn.isClosed())
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
        }
        finally
        {
        	return masjidListObj;
        }
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