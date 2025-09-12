//p72~8
//ユーザー情報の管理や表示を担当するServlet
package com.example.attendance.controller;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.User;

public class UserServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // リクエストからアクションパラメータを取得
        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        // 現在のユーザーが管理者でない場合、ログインページにリダイレクト
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            resp.sendRedirect("login.jsp");
            return;
        }

        // セッションからメッセージを取得し、リクエストに設定
        String message = (String) session.getAttribute("successMessage");
        if (message != null) {
            req.setAttribute("successMessage", message);
            session.removeAttribute("successMessage");
        }

        // アクションが"list"またはnullの場合、ユーザーリストを取得
        if ("list".equals(action) || action == null) {
            Collection<User> users = userDAO.getAllUsers();
            req.setAttribute("users", users);
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_management.jsp");
            rd.forward(req, resp);
        } 
        // アクションが"edit"の場合、指定されたユーザーを編集
        else if ("edit".equals(action)) {
            String username = req.getParameter("username");
            User user = userDAO.findByUsername(username);
            req.setAttribute("userToEdit", user);
            Collection<User> users = userDAO.getAllUsers();
            req.setAttribute("users", users);
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_management.jsp");
            rd.forward(req, resp);
        } 
        // その他のアクションの場合、ユーザーリストにリダイレクト
        else {
            resp.sendRedirect("users?action=list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8"); // リクエストの文字エンコーディングを設定
        String action = req.getParameter("action");
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("user");

        // 現在のユーザーが管理者でない場合、ログインページにリダイレクト
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            resp.sendRedirect("login.jsp");
            return;
        }

        // アクションが"add"の場合、新しいユーザーを追加
        if ("add".equals(action)) {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String role = req.getParameter("role");
            // ここでユーザーを追加する処理を実装する必要があります
            // ユーザー追加処理
            if (userDAO.findByUsername(username) == null) {
                // 新しいユーザーを追加
                userDAO.addUser(new User(username, UserDAO.hashPassword(password), role));
                session.setAttribute("successMessage", "ユーザーを追加しました。");
            } else {
                // ユーザーIDが既に存在する場合のエラーメッセージ
                req.setAttribute("errorMessage", "ユーザーIDは既に存在します。");
            }
            
            // ユーザー情報の更新処理
            } else if ("update".equals(action)) {
                String username = req.getParameter("username");
                String role = req.getParameter("role");
                boolean enabled = req.getParameter("enabled") != null; // 有効/無効の状態を取得
                User existingUser = userDAO.findByUsername(username);
                if (existingUser != null) {
                    // 既存のユーザー情報を更新
                    userDAO.updateUser(new User(username, existingUser.getPassword(), role, enabled));
                    session.setAttribute("successMessage", "ユーザー情報を更新しました。");
                }
            
            // ユーザー削除処理
            } else if ("delete".equals(action)) {
                String username = req.getParameter("username");
                userDAO.deleteUser(username); // ユーザーを削除
                session.setAttribute("successMessage", "ユーザーを削除しました。");
            
            // パスワードリセット処理
            } else if ("reset_password".equals(action)) {
                String username = req.getParameter("username");
                String newPassword = req.getParameter("newPassword");
                userDAO.resetPassword(username, newPassword); // パスワードをリセット
                session.setAttribute("successMessage", username + "のパスワードをリセットしました。(デフォルトパスワード: " + newPassword + ")");
            
            // ユーザーの有効/無効切り替え処理
            } else if ("toggle_enabled".equals(action)) {
                String username = req.getParameter("username");
                boolean enabled = Boolean.parseBoolean(req.getParameter("enabled")); // 有効/無効の状態を取得
                userDAO.toggleUserEnabled(username, enabled); // ユーザーの有効/無効を切り替え
                session.setAttribute("successMessage", username + "のアカウントを" + (enabled ? "有効" : "無効") + "にしました。");
            }

            // 処理が完了したらユーザーリストにリダイレクト
            resp.sendRedirect("users?action=list");
        }
    }

