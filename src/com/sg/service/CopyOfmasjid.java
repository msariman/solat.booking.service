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
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class CopyOfmasjid extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Connection conn;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CopyOfmasjid() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public static void connect() {
        try {
            // db parameters
            String url = "jdbc:sqlite:C://temp/SingaporeMasjid.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } 
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject responses = new JSONObject();
		try {
			if (conn == null)
					connect();
				
				String action = request.getParameter("ac");
				if (action.equals("sl"))
				{
					String masjidList =  "<div><b>%s</b></div>" + 
							  "<div class='dropdown'>" +
							  "<span>%s</span>" +
							  "<div class='dropdown-content'>" +
							  "<form action='http://msariman-p51:8080/masjid/updateVisit?ac=adph&masjid=%d&solat=%d'>" +
							     "<label for='vPhoneL'>Phone No:</label><br>" + 
							     "<input type='text' id='vPhone' name='vPhone' value=''><br>" +
							     "<button>Register</button>" +
							  "</form>" +
							  "</div>" +
							  "</div>";
					JSONArray masjids = getMasjidList();
					for (int i=0; i<masjids.length(); i++) {
					    JSONObject item = masjids.getJSONObject(i);
					    int id = item.getInt("id");
					    String name = item.getString("name");
					    String formatedString = String.format(masjidList,name,"Solat",id,1);  
					    //responses.put("masjid", formatedString);
					}
				} else if (action.equals("ml"))
				{
					String masjidList =  "<div class='dropdown'>" +
										 "<span>%s</span>" +
										 "</div>";
					JSONArray masjids = getMasjidList();
					for (int i=0; i<masjids.length(); i++) {
					    JSONObject item = masjids.getJSONObject(i);
					    int id = item.getInt("id");
					    String name = item.getString("name");
					    String formatedString = String.format(masjidList,name);  
					    responses.put("masjid", formatedString);
					}
				}
				
			} catch (Exception e) {
				responses.put("error", e.getMessage());
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
		JSONObject memberDetail = new JSONObject();
		try {
			memberDetail.put("wrsp_svc_desc", "Successfully inserted the record to QUAD DB");
			memberDetail.put("wrsp_svc_cd", "200");
			memberDetail.put("wrsp_svc_stts", "Success");
		} catch (Exception e) {
			memberDetail.put("error", e.getMessage());
		}
		response.getWriter().append(memberDetail.toString());
	}

	   /**
     * select all rows in the warehouses table
     */
    @SuppressWarnings("finally")
	public JSONArray getMasjidList(){
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
