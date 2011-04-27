//package org.cumulus4j.keyserver.front.webapp;
//
//import java.io.IOException;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * <p>
// * Servlet responsible for session management.
// * </p>
// * <p>
// * The key server managing keys and sessions
// * is implemented in <code>org.cumulus4j.keyserver.front.webapp</code> and this
// * servlet provides the ability to manage sessions.
// * </p>
// *
// * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
// */
//@WebServlet(urlPatterns="/Session/*")
//public class Session extends HttpServlet
//{
//	private static final Logger logger = LoggerFactory.getLogger(Session.class);
//
//	private static final long serialVersionUID = 1L;
//
//	@Override
//	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doAction(request, response);
//	}
//
//	@Override
//	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		doAction(request, response);
//	}
//
//	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		String pathInfo = request.getPathInfo();
//		logger.warn("doAction: pathInfo={}", pathInfo);
//
//		if (pathInfo != null) {
//			if ("/open".equals(pathInfo)) {
//				sessionOpen(request, response);
//			}
//			else if ("/close".equals(pathInfo)) {
//
//			}
//		}
//	}
//
//	protected void sessionOpen(HttpServletRequest request, HttpServletResponse response)
//	{
//		String userName = request.getParameter("userName");
//		String password = request.getParameter("password");
//
//	}
//
//
//}
