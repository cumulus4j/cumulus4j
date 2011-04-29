package org.cumulus4j.keyserver.back.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for establishing a key-request-channel. The client is the key server and
 * this servlet sends key requests to the client whenever a key is needed. This works
 * by blocking the client's request until either a timeout occurs (we don't block the
 * client's request forever) or a key is needed.
 */
//@WebServlet("/cumulus4j/KeyRequest")
public class KeyRequest extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
