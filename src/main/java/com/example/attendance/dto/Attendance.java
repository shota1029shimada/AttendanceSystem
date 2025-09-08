//p17~20
//勤怠情報を表現するDTO
package com.example.attendance.dto;

import java.time.LocalDateTime;

public class Attendance {
    private String userId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    // コンストラクタ
    public Attendance(String userId) {
        this.userId = userId;
    }

    // ユーザーIDの取得
    public String getUserId() {
        return userId;
    }

    // チェックイン時間の取得
    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    // チェックイン時間の設定
    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    // チェックアウト時間の取得
    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    // チェックアウト時間の設定
    public void setCheckOutTime(LocalDateTime checkOutTime) {
        if (checkInTime != null && checkOutTime.isBefore(checkInTime)) {
            throw new IllegalArgumentException("チェックアウト時間はチェックイン時間よりも後でなければなりません。");
        }
        this.checkOutTime = checkOutTime;
    }

    // 出席情報の表示
    public String displayAttendance() {
        StringBuilder sb = new StringBuilder();
        sb.append("ユーザーID: ").append(userId).append("\n");
        sb.append("チェックイン時間: ").append(checkInTime != null ? checkInTime : "未チェックイン").append("\n");
        sb.append("チェックアウト時間: ").append(checkOutTime != null ? checkOutTime : "未チェックアウト").append("\n");
        return sb.toString();
    }
}