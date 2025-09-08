//p40~61
 //勤怠情報の管理を担当するServlet
package com.example.attendance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

//p40~61

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.User;

public class AttendanceServlet extends HttpServlet {
    // AttendanceDAOのインスタンスを作成
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // リクエストからアクションパラメータを取得
        String action = req.getParameter("action");
        // セッションを取得
        HttpSession session = req.getSession(false);
        // セッションからユーザー情報を取得
        User user = (User) session.getAttribute("user");
        
        // ユーザーがログインしていない場合、ログインページにリダイレクト
        if (user == null) {
            resp.sendRedirect("login.jsp");
            return;
        }
        
        // セッションから成功メッセージを取得
        String message = (String) session.getAttribute("successMessage");
        if (message != null) {
            // メッセージをリクエストに設定し、セッションから削除
            req.setAttribute("successMessage", message);
            session.removeAttribute("successMessage");
        }
        
        // アクションが"export_csv"で、ユーザーが管理者の場合
        if ("export_csv".equals(action) && "admin".equals(user.getRole())) {
            exportCsv(req, resp); // CSVエクスポート処理を呼び出す
        } 
        // アクションが"filter"で、ユーザーが管理者の場合
        else if ("filter".equals(action) && "admin".equals(user.getRole())) {
            // フィルタリング用のパラメータを取得
            String filterUserId = req.getParameter("filterUserId");
            String startDateStr = req.getParameter("startDate");
            String endDateStr = req.getParameter("endDate");
            LocalDate startDate = null;
            LocalDate endDate = null;
            try {
                // 日付文字列をLocalDateに変換
                if (startDateStr != null && !startDateStr.isEmpty()) {
                    startDate = LocalDate.parse(startDateStr);
                }
                if (endDateStr != null && !endDateStr.isEmpty()) {
                    endDate = LocalDate.parse(endDateStr);
                }
            } catch (DateTimeParseException e) {
                // 日付形式が不正な場合、エラーメッセージを設定
                req.setAttribute("errorMessage", "日付の形式が不正です。");
            }
            // フィルタリングされた出席記録を取得
            List<Attendance> filteredRecords = attendanceDAO.findFilteredRecords(filterUserId, startDate, endDate);
            req.setAttribute("allAttendanceRecords", filteredRecords);
            
            // ユーザーごとの合計勤務時間を計算
            Map<String, Long> totalHoursByUser = filteredRecords.stream()
                .collect(Collectors.groupingBy(Attendance::getUserId,
                        Collectors.summingLong(att -> {
                            if (att.getCheckInTime() != null && att.getCheckOutTime() != null) {
                                return java.time.temporal.ChronoUnit.HOURS.between(att.getCheckInTime(), att.getCheckOutTime());
                            }
                            return 0L;
                        })));
            req.setAttribute("totalHoursByUser", totalHoursByUser);
            // 月ごとの勤務時間とチェックイン回数を取得
            req.setAttribute("monthlyWorkingHours", attendanceDAO.getMonthlyWorkingHours(filterUserId));
            req.setAttribute("monthlyCheckInCounts", attendanceDAO.getMonthlyCheckInCounts(filterUserId));
            
            // 管理者メニューにフォワード
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/admin_menu.jsp");
            rd.forward(req, resp);
        } else {
            // ユーザーが管理者の場合
            if ("admin".equals(user.getRole())) {
                // 全ての出席記録を取得
                req.setAttribute("allAttendanceRecords", attendanceDAO.findAll());
                Map<String, Long> totalHoursByUser = attendanceDAO.findAll().stream()
                    .collect(Collectors.groupingBy(Attendance::getUserId,
                            Collectors.summingLong(att -> {
                                if (att.getCheckInTime() != null && att.getCheckOutTime() != null) {
                                    return java.time.temporal.ChronoUnit.HOURS.between(att.getCheckInTime(), att.getCheckOutTime());
                                }
                                return 0L;
                            })));
                req.setAttribute("totalHoursByUser", totalHoursByUser);
                // 月ごとの勤務時間とチェックイン回数を取得
                req.setAttribute("monthlyWorkingHours", attendanceDAO.getMonthlyWorkingHours(null));
                req.setAttribute("monthlyCheckInCounts", attendanceDAO.getMonthlyCheckInCounts(null));
                
                // 管理者メニューにフォワード
                RequestDispatcher rd = req.getRequestDispatcher("/jsp/admin_menu.jsp");
                rd.forward(req, resp);
            } else {
                // ユーザーが一般社員の場合、出席記録を取得
                req.setAttribute("attendanceRecords", attendanceDAO.findByUserId(user.getUsername()));
                // 社員メニューにフォワード
                RequestDispatcher rd = req.getRequestDispatcher("/jsp/employee_menu.jsp");
                rd.forward(req, resp);
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // セッションを取得
        HttpSession session = req.getSession(false);
        // セッションからユーザー情報を取得
        User user = (User) session.getAttribute("user");
        
        // ユーザーがログインしていない場合、ログインページにリダイレクト
        if (user == null) {
            resp.sendRedirect("login.jsp");
            return;
        }
        
        // リクエストからアクションパラメータを取得
        String action = req.getParameter("action");
        
        // アクションが"check_in"の場合
        if ("check_in".equals(action)) {
            attendanceDAO.checkIn(user.getUsername()); // 出勤を記録
            session.setAttribute("successMessage", "出勤を記録しました。");
        } 
        // アクションが"check_out"の場合
        else if ("check_out".equals(action)) {
            attendanceDAO.checkOut(user.getUsername()); // 退勤を記録
            session.setAttribute("successMessage", "退勤を記録しました。");
        } 
        // アクションが"add_manual"で、ユーザーが管理者の場合
        else if ("add_manual".equals(action) && "admin".equals(user.getRole())) {
            // 手動で追加するためのパラメータを取得
            String userId = req.getParameter("userId");
            String checkInStr = req.getParameter("checkInTime");
            String checkOutStr = req.getParameter("checkOutTime");
            try {
                // 日付/時刻をLocalDateTimeに変換
                LocalDateTime checkIn = LocalDateTime.parse(checkInStr);
                LocalDateTime checkOut = checkOutStr != null && !checkOutStr.isEmpty()
                        ? LocalDateTime.parse(checkOutStr)
                        : null;
                attendanceDAO.addManualAttendance(userId, checkIn, checkOut); // 勤怠記録を手動で追加
                session.setAttribute("successMessage", "勤怠記録を手動で追加しました。");
            } catch (DateTimeParseException e) {
                // 日付/時刻の形式が不正な場合、エラーメッセージを設定
                session.setAttribute("errorMessage", "日付/時刻の形式が不正です。");
            }
        } 
        // アクションが"update_manual"で、ユーザーが管理者の場合
        else if ("update_manual".equals(action) && "admin".equals(user.getRole())) {
            // 更新するためのパラメータを取得
            String userId = req.getParameter("userId");
            LocalDateTime oldCheckIn = LocalDateTime.parse(req.getParameter("oldCheckInTime"));
            LocalDateTime oldCheckOut = req.getParameter("oldCheckOutTime") != null
                    && !req.getParameter("oldCheckOutTime").isEmpty()
                            ? LocalDateTime.parse(req.getParameter("oldCheckOutTime"))
                            : null;
            LocalDateTime newCheckIn = LocalDateTime.parse(req.getParameter("newCheckInTime"));
            LocalDateTime newCheckOut = req.getParameter("newCheckOutTime") != null
                    && !req.getParameter("newCheckOutTime").isEmpty()
                            ? LocalDateTime.parse(req.getParameter("newCheckOutTime"))
                            : null;
            
            // 勤怠記録を手動で更新
            if (attendanceDAO.updateManualAttendance(userId, oldCheckIn, oldCheckOut, newCheckIn, newCheckOut)) {
                session.setAttribute("successMessage", "勤怠記録を手動で更新しました。");
            } else {
                session.setAttribute("errorMessage", "勤怠記録の更新に失敗しました。");
            }
        } 
        // アクションが"delete_manual"で、ユーザーが管理者の場合
        else if ("delete_manual".equals(action) && "admin".equals(user.getRole())) {
            // 削除するためのパラメータを取得
            String userId = req.getParameter("userId");
            LocalDateTime checkIn = LocalDateTime.parse(req.getParameter("checkInTime"));
            LocalDateTime checkOut = req.getParameter("checkOutTime") != null
                    && !req.getParameter("checkOutTime").isEmpty()
                            ? LocalDateTime.parse(req.getParameter("checkOutTime"))
                            : null;
            
            // 勤怠記録を削除
            if (attendanceDAO.deleteManualAttendance(userId, checkIn, checkOut)) {
                session.setAttribute("successMessage", "勤怠記録を削除しました。");
            } else {
                session.setAttribute("errorMessage", "勤怠記録の削除に失敗しました。");
            }
        }
        
        // ユーザーが管理者の場合、フィルタリングされた出席記録のページにリダイレクト
        if ("admin".equals(user.getRole())) {
            resp.sendRedirect("attendance?action=filter&filterUserId=" +
                    (req.getParameter("filterUserId") != null ? req.getParameter("filterUserId") : "") +
                    "&startDate=" + (req.getParameter("startDate") != null ? req.getParameter("startDate") : "") +
                    "&endDate=" + (req.getParameter("endDate") != null ? req.getParameter("endDate") : ""));
        } else {
            // 一般社員の場合、出席記録のページにリダイレクト
            resp.sendRedirect("attendance");
        }
    }

    private void exportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // レスポンスのコンテンツタイプとヘッダーを設定
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"attendance_records.csv\"");
        PrintWriter writer = resp.getWriter();
        writer.append("User ID,Check-in Time,Check-out Time\n"); // CSVのヘッダーを追加
        
        // フィルタリング用のパラメータを取得
        String filterUserId = req.getParameter("filterUserId");
        String startDateStr = req.getParameter("startDate");
        String endDateStr = req.getParameter("endDate");
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        try {
            // 日付文字列をLocalDateに変換
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = LocalDate.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = LocalDate.parse(endDateStr);
            }
        } catch (DateTimeParseException e) {
            // 日付形式が不正な場合、エラーメッセージを出力
            System.err.println("Invalid date format for CSV export: " + e.getMessage());
        }
        
        // フィルタリングされた出席記録を取得
        List<Attendance> records = attendanceDAO.findFilteredRecords(filterUserId, startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // 各出席記録をCSV形式で書き込む
        for (Attendance record : records) {
            writer.append(String.format("%s,%s,%s\n",
                record.getUserId(),
                record.getCheckInTime() != null ? record.getCheckInTime().format(formatter) : "",
                record.getCheckOutTime() != null ? record.getCheckOutTime().format(formatter) : ""));
        }
        
        writer.flush(); // 書き込みをフラッシュ
    }
}