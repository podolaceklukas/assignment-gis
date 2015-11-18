package com.javatechig;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.println("<h1>Servlet works!</h1>");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
            int length = request.getContentLength();
            byte[] input = new byte[length];
            ServletInputStream sin = request.getInputStream();
            int c, count = 0 ;
            while ((c = sin.read(input, count, input.length - count)) != -1) {
                count +=c;
            }
            sin.close();
 
            String recievedString = new String(input);
            response.setStatus(HttpServletResponse.SC_OK);
            
            String result = new PostgreSQLJDBC().Send(recievedString);
            
            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
            writer.write(result);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            try{
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(e.getMessage());
                response.getWriter().close();
            } catch (IOException ioe) {
            }
        }
    }

	@Override
	public void destroy() {
		super.destroy();
	}
}