<%--p82~85 --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>勤怠管理システム - ログイン</title>
    <link rel="stylesheet" href="style.css"> <!-- スタイルシートのリンク -->
</head>
<body>
    <div class="container">
        <h1>勤怠管理システム</h1>
        <form action="login" method="post"> <!-- ログインフォーム -->
            <p>
                <label for="username">ユーザーID:</label>
                <input type="text" id="username" name="username" required> <!-- ユーザーID入力フィールド -->
            </p>
            <p>
                <label for="password">パスワード:</label>
                <input type="password" id="password" name="password" required> <!-- パスワード入力フィールド -->
            </p>
            <div class="button-group">
                <input type="submit" value="ログイン"> <!-- ログインボタン -->
            </div>
        </form>
        <p class="error-message"><c:out value="${errorMessage}"/></p> <!-- エラーメッセージの表示 -->
        <c:if test="${not empty sessionScope.successMessage}"> <!-- セッションに成功メッセージがある場合 -->
            <p class="success-message"><c:out value="${sessionScope.successMessage}"/></p> <!-- 成功メッセージの表示 -->
            <c:remove var="successMessage" scope="session"/> <!-- 成功メッセージをセッションから削除 -->
        </c:if>
    </div>
</body>
</html>
