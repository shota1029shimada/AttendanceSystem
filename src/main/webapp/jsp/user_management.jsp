<%-- p100~109 --%>
<%--ユーザー管理に関連する機能を提供するウェブページ --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>ユーザー管理</title>
    <link rel="stylesheet" href="../style.css">
</head>
<body>
    <div class="container">
        <h1>ユーザー管理</h1>
        <p>ようこそ, ${user.username}さん (管理者)</p>
        
        <!-- メインナビゲーション -->
        <div class="main-nav">
            <a href="attendance?action=filter">勤怠履歴管理</a>
            <a href="user">ユーザー管理</a>    <!-- user_manegement.jsp -->
            <a href="logout">ログアウト</a>   
        </div>

        <!-- セッションからの成功メッセージを表示 -->
        <c:if test="${not empty sessionScope.successMessage}">
            <p class="success-message"><c:out value="${sessionScope.successMessage}"/></p>
            <c:remove var="successMessage" scope="session"/> <!-- メッセージをセッションから削除 -->
        </c:if>

        <h2>ユーザー追加/編集</h2>
        <!-- ユーザー追加・編集用フォーム -->
      <form action="${pageContext.request.contextPath}/user" method="post" class="user-form">
      <input type="hidden" name="action" value="<c:choose><c:when test="${userToEdit != null}">update</c:when><c:otherwise>add</c:otherwise></c:choose>">
      <c:if test="${userToEdit != null}"> 
            
            
            <!-- 編集時のユーザー名を隠しフィールドで送信 -->
                <input type="hidden" name="username" value="${userToEdit.username}"> 
            </c:if>
            <label for="username">ユーザーID:</label>
            <input type="text" id="username" name="username" value="<c:out value="${userToEdit.username}"/>" <c:if test="${userToEdit != null}">readonly</c:if> required>
            <label for="password">パスワード:</label>
            <input type="password" id="password" name="password" <c:if test="${userToEdit == null}">required</c:if>> <!-- 新規ユーザーの場合は必須 -->
            <c:if test="${userToEdit != null}">
                <p class="error-message">※編集時はパスワードは変更されません。リセットする場合は別途操作してください。</p>
            </c:if>
            <label for="role">役割:</label>
            <select id="role" name="role" required>
                <option value="employee" <c:if test="${userToEdit.role == 'employee'}">selected</c:if>>従業員</option>
                <option value="admin" <c:if test="${userToEdit.role == 'admin'}">selected</c:if>>管理者</option>
            </select>
            <p>
                <label for="enabled">アカウント有効:</label>
                <input type="checkbox" id="enabled" name="enabled" value="true" <c:if test="${userToEdit == null || userToEdit.enabled}">checked</c:if>> <!-- 有効状態を反映 -->
            </p>
            <div class="button-group">
                <input type="submit" value="<c:choose><c:when test="${userToEdit != null}">更新</c:when><c:otherwise>追加</c:otherwise></c:choose>">
                <c:if test="${userToEdit != null}">
                    <!-- パスワードリセット用のフォーム -->
                    <form action="users" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="reset_password">
                        <input type="hidden" name="username" value="${userToEdit.username}">
                        <input type="hidden" name="newPassword" value="password">
                        <input type="submit" value="パスワードリセット" class="button secondary" onclick="return confirm('本当にパスワードをリセットしますか？（デフォルトパスワード: password）');">
                    </form>
                </c:if>
            </div>
        </form>

        <!-- エラーメッセージの表示 -->
        <p class="error-message"><c:out value="${errorMessage}"/></p>

        <h2>既存ユーザー</h2>
        <!-- 既存ユーザーの一覧表示 -->
        <table>
            <thead>
                <tr>
                    <th>ユーザーID</th>
                    <th>役割</th>
                    <th>有効</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="u" items="${users}">
                    <tr>
                        <td>${u.username}</td>
                        <td>${u.role}</td>
                        <td>
                            <!-- アカウントの有効・無効を切り替えるフォーム -->
                            <form action="users" method="post" style="display:inline;">
                                <input type="hidden" name="action" value="toggle_enabled">
                                <input type="hidden" name="username" value="${u.username}">
                                <input type="hidden" name="enabled" value="${!u.enabled}">
                                <input type="submit" value="<c:choose><c:when test="${u.enabled}">無効化</c:when><c:otherwise>有効化</c:otherwise></c:choose>" class="button <c:choose><c:when test="${u.enabled}">danger</c:when><c:otherwise>secondary</c:otherwise></c:choose>" onclick="return confirm('本当にこのユーザーを<c:choose><c:when test="${u.enabled}">無効</c:when><c:otherwise>有効</c:otherwise></c:choose>にしますか？');">
                            </form>
                        </td>
                        <td class="table-actions">
                            <!-- ユーザー編集へのリンク -->
                            <!-- <a href="users?action=edit&username=${u.username}" class="button">編集</a>  ⬇に変更️-->
                            <a href="${pageContext.request.contextPath}/user?action=edit&username=${u.username}" class="button">編集</a>
                            <!-- ユーザー削除のフォーム -->
                            <form action="user" method="post" style="display:inline;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="username" value="${u.username}">
                                <input type="submit" value="削除" class="button danger" onclick="return confirm('本当にこのユーザーを削除しますか？');">
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty users}">
                    <tr><td colspan="4">ユーザーがいません。</td></tr> <!-- ユーザーがいない場合のメッセージ -->
                </c:if>
            </tbody>
        </table>
    </div>
</body>
</html>
