<%@ page import="model.User" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Phonebook</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #1f1f1f;
            color: #ffffff;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }

        .container {
            max-width: 800px;
            padding: 20px;
            background-color: #2c2c2c;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(255, 255, 255, 0.1);
            margin: 20px auto 0;
            animation: fadeIn 1s ease-in-out;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
            }
            to {
                opacity: 1;
            }
        }

        .title {
            font-size: 36px;
            font-weight: bold;
            margin-bottom: 20px;
            color: #ffffff;
            text-align: center;
        }

        .user-card {
            border: 1px solid #444;
            padding: 15px;
            margin-bottom: 10px;
            border-radius: 10px;
            text-align: left;
            transition: background-color 0.3s;
            cursor: pointer;
        }

        .user-card:hover {
            background-color: #333;
            /*transform: scale(1.05);*/
        }

        .user-name {
            font-weight: bold;
            font-size: 18px;
            color: #ffffff;
        }

        .user-details {
            margin-top: 5px;
            color: #bbbbbb;
        }

        label {
            display: block;
            font-weight: bold;
            margin-top: 10px;
            color: #ffffff;
        }

        input[type="text"] {
            width: 100%;
            padding: 12px;
            margin-top: 5px;
            border: 1px solid #444;
            border-radius: 10px;
            font-size: 16px;
            color: #ffffff;
            background-color: #333;
        }

        button {
            background-color: #228e5d;
            color: #ffffff;
            padding: 12px 24px;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            cursor: pointer;
            margin-top: 20px;
            transition: background-color 0.3s;
        }

        button:hover {
            background-color: #1a724a;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="title">Телефонная книга</div>
        <div>
            <form id="userForm">
                <label for="userName"></label><input type="text" id="userName" placeholder="Имя пользователя">
                <label for="userPhoneNumber"></label><input type="text" id="userPhoneNumber" pattern="[0-9+]"
                                                            placeholder="Номер телефона">
                <button type="button" id="addUserButton">Добавить пользователя</button>
                <label for="userNameDelete"></label><input type="text" id="userNameDelete" placeholder="Имя пользователя">
                <label for="userPhoneDelete"></label><input type="text" id="userPhoneDelete" placeholder="Телефон пользователя">
                <button type="button" id="deletePhone">Удалить пользователя</button>
            </form>
        </div>
        <hr>
        <script>
            document.getElementById('addUserButton').addEventListener('click', function () {
                const userName = document.getElementById('userName').value;
                const userPhoneNumber = document.getElementById('userPhoneNumber').value;

                const phoneNumberPattern = /^[0-9+-]+$/;
                if (!phoneNumberPattern.test(userPhoneNumber)) {
                    alert("Invalid phone number!");
                    return;
                }
                const usernamePatterns = /\S/g;
                if (!usernamePatterns.test(userName)) {
                    alert("Invalid name value! Please enter non null value")
                    return;
                }

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
                        //location.reload();
                        window.location.href = 'http://localhost:8080/l13/phonebook/add';
                    })
                    .catch(error => console.error('Error:', error));
            });
            document.getElementById('deletePhone').addEventListener('click', function () {
                const userName = document.getElementById('userNameDelete').value;
                const userPhoneNumber = document.getElementById('userPhoneDelete').value;

                const data = {
                    name: userName,
                    phoneNumber: userPhoneNumber
                };

                fetch('http://localhost:8080/l13/phonebook', {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok');
                        }
                        window.location.href = 'http://localhost:8080/l13/phonebook/delete';
                    })
                    .catch(error => console.error('Error:', error));
            });
        </script>
    <div class="user-list">
        <%
            var users = (List<User>) request.getAttribute("users");
            var userNumbersMap = new HashMap<String, List<String>>();

            for (User user : users) {
                String userName = user.getName();
                String phoneNumber = user.getPhoneNumbers().toString();

                if (userNumbersMap.containsKey(userName)) {
                    userNumbersMap.get(userName).add(phoneNumber);
                } else {
                    List<String> phoneNumbersList = new ArrayList<>();
                    phoneNumbersList.add(phoneNumber);
                    userNumbersMap.put(userName, phoneNumbersList);
                }
            }

            for (Map.Entry<String, List<String>> entry : userNumbersMap.entrySet()) {
                String userName = entry.getKey();
                List<String> phoneNumbers = entry.getValue();
        %>
        <div class="user-card">
            <div class="user-name"><%= userName %>
            </div>
            <div class="user-details">
                <strong>Phone Numbers:</strong> <%= String.join(", ", phoneNumbers) %>
            </div>
        </div>
        <%
            }
        %>
    </div>
</div>
<%--/% переключение темы %/--%>
<%--<button id="toggleThemeButton">Переключить тему</button>]--%>
<%--<script>--%>
<%--    const toggleThemeButton = document.getElementById('toggleThemeButton');--%>
<%--    const body = document.body;--%>

<%--    toggleThemeButton.addEventListener('click', function () {--%>
<%--        body.classList.toggle('light-theme');--%>
<%--    });--%>
<%--</script> --%>
</body>
</html>