package com.example.attendance.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

//p72~81

import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.User;

public class UserServlet extends HttpServlet {
  private final UserDAO userDAO = new UserDAO();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      String action = req.getParameter("action");
      HttpSession session = req.getSession(false);
      User currentUser = (User) session.getAttribute("user");
      
      if (currentUser == null || !"admin".equals(currentUser.getRole())) {
          resp.sendRedirect("login.jsp");
          return;
      }

      // Retrieve and clear message from session
      String message = (String) session.getAttribute("successMessage");
      if (message != null) {
          req.setAttribute("successMessage", message);
          session.removeAttribute("successMessage");
      }

      // Additional logic for handling different actions can be added here
      // For example:
      // if ("listUsers".equals(action)) {
      //     Collection<User> users = userDAO.findAll();
      //     req.setAttribute("users", users);
      //     RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_list.jsp");
      //     rd.forward(req, resp);
      // }
  }
}
