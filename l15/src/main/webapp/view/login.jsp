<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Войти в личный кабинет</title>
</head>
<body>
<h1>Добро пожаловать!</h1>
<div id="message"></div>
<div id="userPanel" style="display: none;">
    <p>Привет, <span id="welcomeUsername"></span>!</p>
    <button type="button" id="logoutButton">Выйти</button>
    <a href="${pageContext.request.contextPath}"><button type="button" id="backButton">Вернуться на главную</button></a>
</div>
<div id="loginPanel">
    <form id="loginForm">
        <label for="username">Имя пользователя:</label>
        <input type="text" id="username" name="username"><br>
        <label for="password">Пароль:</label>
        <input type="password" id="password" name="password"><br>
        <button type="button" id="loginButton">Войти</button>
        <a href="${pageContext.request.contextPath}"><button type="button" id="backtwoButton">Вернуться на главную</button></a>
    </form>
</div>

<script>
    function displayUserInfo(username) {
        const messageElement = document.getElementById('message');
        const loginForm = document.getElementById('loginForm');
        const userPanel = document.getElementById('userPanel');
        const welcomeUsername = document.getElementById('welcomeUsername');
        const logoutButton = document.getElementById('logoutButton');

        messageElement.innerHTML = "Добро пожаловать, " + username + "!";
        loginForm.style.display = 'none';
        userPanel.style.display = 'block';
        welcomeUsername.textContent = username;
        logoutButton.style.display = 'block';
    }

    function checkSuccess(responseData) {
        if (responseData.success) {
            displayUserInfo(responseData.username);
            localStorage.setItem("username", responseData.username);
        } else {
            document.getElementById('message').innerHTML = "Пользователь не вошел в систему";
        }
    }
    var savedUsername = localStorage.getItem("username");
    if (savedUsername) {
        displayUserInfo(savedUsername);
    }
    document.getElementById('loginButton').addEventListener('click', function() {
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        const data = {
            username: username,
            password: password
        };

        fetch('login', {
            method: 'POST',
            body: JSON.stringify(data),
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => response.json())
            .then(data => {
                checkSuccess(data);
            });
    });

    document.getElementById('logoutButton').addEventListener('click', function() {
        fetch('logout', {
            method: 'DELETE'
        })
            .then(response => {
                localStorage.removeItem("username");
                document.getElementById('message').innerHTML = "Вы успешно вышли";
                document.getElementById('loginForm').style.display = 'block';
                document.getElementById('userPanel').style.display = 'none';
                document.getElementById('username').value = "";
                document.getElementById('logoutButton').style.display = 'none';
            });
    });
</script>
</body>
</html>
