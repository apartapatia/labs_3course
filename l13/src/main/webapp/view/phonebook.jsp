<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Phonebook</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f4f4f4; /* Добавим фоновый цвет */
        }

        .container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #fff; /* Фон контейнера */
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); /* Тень контейнера */
        }

        .title {
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 20px;
        }

        .user-card {
            border: 1px solid #ccc;
            padding: 10px;
            margin-bottom: 10px;
            border-radius: 5px;
            text-align: left;
            transition: background-color 0.3s;
        }

        .user-card:hover {
            background-color: #f0f0f0;
        }

        .user-name {
            font-weight: bold;
            font-size: 18px;
        }

        .user-details {
            margin-top: 5px;
            color: #555;
        }

        /* Стили для ввода данных */
        label {
            display: block;
            font-weight: bold;
            margin-top: 10px;
        }

        input[type="text"] {
            width: 100%;
            padding: 8px;
            margin-top: 5px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 16px;
        }

        button {
            background-color: #007bff;
            color: #fff;
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
            margin-top: 10px;
        }

        button:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="title">Телефонная книга</div>
    <div>
        <form id="userForm">
            <label for="userName"></label><input type="text" id="userName" placeholder="Имя пользователя">
            <label for="userPhoneNumber"></label><input type="text" id="userPhoneNumber" placeholder="Номер телефона">
            <button type="button" id="addUserButton">Добавить пользователя</button>
        </form>
    </div>
    <hr>
    <script>
        document.getElementById('addUserButton').addEventListener('click', function () {
            const userName = document.getElementById('userName').value;
            const userPhoneNumber = document.getElementById('userPhoneNumber').value;
            const data = {
                name: userName,
                phoneNumber: userPhoneNumber
            };

            fetch('http://localhost:8080/l13/phonebook', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                })
                .catch(error => console.error('Error:', error));
        });
    </script>
    <div class="user-list">
        <%
            var users = (List<User>) request.getAttribute("users");

            for (User user : users) {
        %>
        <div class="user-card">
            <div class="user-name"><%= user.getName() %>
            </div>
            <div class="user-details">
                Phone: <%= user.getPhoneNumbers() %><br>
            </div>
        </div>
        <%
            }
        %>
    </div>
</div>
</body>
</html>