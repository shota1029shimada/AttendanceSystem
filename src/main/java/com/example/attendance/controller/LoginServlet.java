//p62~68
//ユーザーのログイン処理を担当するServlet
package com.example.attendance.controller;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

//DAO（データベースにアクセスするクラス）
import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.User;


public class LoginServlet extends HttpServlet {
	//ユーザー情報を扱うDAO
  private final UserDAO userDAO = new UserDAO();
//勤怠情報を扱うDAO
  private final AttendanceDAO attendanceDAO = new AttendanceDAO();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	  // フォームから送られてきたユーザー名とパスワードを取得
      String username = req.getParameter("username");
      String password = req.getParameter("password");
      
   // ユーザー名からユーザー情報を取得
      User user = userDAO.findByUsername(username);
      
   // ユーザーが存在し、有効で、パスワードも正しい場合
      if (user != null && user.isEnabled() && userDAO.verifyPassword(username, password)) {
    	  
    	  // セッションを取得し、ユーザー情報を保存
          HttpSession session = req.getSession();
          session.setAttribute("user", user);
          session.setAttribute("successMessage", "ログインしました。");  // ログイン成功メッセージを保存
          
          
       // 管理者の場合の処理
          if ("admin".equals(user.getRole())) {
        	  
       // 全ての勤怠情報を取得し、リクエストにセット
              req.setAttribute("allAttendanceRecords", attendanceDAO.findAll());
              
        // 勤怠情報からユーザーごとの合計勤務時間を計算
              Map<String, Long> totalHoursByUser = attendanceDAO.findAll().stream()
                  .collect(Collectors.groupingBy(com.example.attendance.dto.Attendance::getUserId, 
                      Collectors.summingLong(att -> {
                    	  
                    	  
                          if (att.getCheckInTime() != null && att.getCheckOutTime() != null) {
                              return java.time.temporal.ChronoUnit.HOURS.between(att.getCheckInTime(), att.getCheckOutTime());
                          }
                          return 0L;// 出退勤が揃っていない場合は 0時間
                      })));
              
              // 合計勤務時間のデータを JSP に渡す
              req.setAttribute("totalHoursByUser", totalHoursByUser);
              
           // 管理者用メニュー画面へ遷移
              RequestDispatcher rd = req.getRequestDispatcher("/jsp/admin_menu.jsp");
              rd.forward(req, resp);
          } else {
        	  
        	// 一般社員の場合、自分の勤怠情報だけ取得
              req.setAttribute("attendanceRecords", attendanceDAO.findByUserId(user.getUsername()));
              
           // 社員用メニュー画面へ遷移
              RequestDispatcher rd = req.getRequestDispatcher("/jsp/employee_menu.jsp");
              rd.forward(req, resp);
          }
      } else {
    	// ユーザーが存在しない、無効、パスワードが違う場合の処理
          req.setAttribute("errorMessage", "ユーザーID またはパスワードが不正です。またはアカウントが無効です。");
          
          // ログイン画面に戻す
          RequestDispatcher rd = req.getRequestDispatcher("/login.jsp");
          rd.forward(req, resp);
      }
  }
}
