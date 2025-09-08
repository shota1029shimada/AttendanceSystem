// p33~39

//ユーザーの認証処理（ID・パスワードチェック
//ユーザーの追加・削除・一覧取得など
package com.example.attendance.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.example.attendance.dto.User;

public class UserDAO {
    // ユーザー情報を格納するための静的なマップ
    private static final Map<String, User> users = new HashMap<>();

    static {
        // 初期ユーザーをマップに追加
        users.put("employee1", new User("employee1", hashPassword("password"), "employee", true));
        users.put("admin1", new User("admin1", hashPassword("adminpass"), "admin", true));
        users.put("employee2", new User("employee2", hashPassword("password"), "employee", true));
    }

    // ユーザー名でユーザーを検索するメソッド
    public User findByUsername(String username) {
        return users.get(username);
    }

    // ユーザー名とパスワードを検証するメソッド
    public boolean verifyPassword(String username, String password) {
        User user = findByUsername(username);
        // ユーザーが存在し、有効で、パスワードが一致するかを確認
        return user != null && user.isEnabled() && user.getPassword().equals(hashPassword(password));
    }

    // すべてのユーザーを取得するメソッド
    public Collection<User> getAllUsers() {
        return users.values();
    }

    // 新しいユーザーを追加するメソッド
    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    // ユーザー情報を更新するメソッド
    public void updateUser(User user) {
        users.put(user.getUsername(), user);
    }

    // ユーザーを削除するメソッド
    public void deleteUser(String username) {
        users.remove(username);
    }

    // ユーザーのパスワードをリセットするメソッド
    public void resetPassword(String username, String newPassword) {
        User user = users.get(username);
        if (user != null) {
            // 新しいパスワードでユーザーを更新
            users.put(username, new User(user.getUsername(), hashPassword(newPassword), user.getRole(), user.isEnabled()));
        }
    }

    // ユーザーの有効/無効を切り替えるメソッド
    public void toggleUserEnabled(String username, boolean enabled) {
        User user = users.get(username);
        if (user != null) {
            // 有効/無効の状態を更新
            users.put(username, new User(user.getUsername(), user.getPassword(), user.getRole(), enabled));
        }
    }

    // パスワードをハッシュ化するメソッド
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
