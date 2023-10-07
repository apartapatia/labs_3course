<%@ page import="model.User" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Phonebook</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #1f1f1f; /* Dark background */
            color: #ffffff; /* Light text */
        }

        .container {
            max-width: 800px;
            padding: 20px;
            background-color: #2c2c2c; /* Dark container */
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(255, 255, 255, 0.1);
            margin: 20px auto 0;
        }

        .title {
            font-size: 36px;
            font-weight: bold;
            margin-bottom: 20px;
            color: #ffffff; /* White title */
            text-align: center;
            width: 100%;
        }

        .user-card {
            border: 1px solid #444; /* Lighter border */
            padding: 15px;
            margin-bottom: 10px;
            border-radius: 10px;
            text-align: left;
            transition: background-color 0.3s;
        }

        .user-card:hover {
            background-color: #333; /* Darker hover background */
        }

        .user-name {
            font-weight: bold;
            font-size: 18px;
            color: #ffffff; /* White text */
        }

        .user-details {
            margin-top: 5px;
            color: #bbbbbb; /* Lighter text */
        }

        /* Styles for input fields */
        label {
            display: block;
            font-weight: bold;
            margin-top: 10px;
            color: #ffffff; /* White label */
        }

        input[type="text"] {
            width: 100%;
            padding: 12px;
            margin-top: 5px;
            border: 1px solid #444; /* Lighter border */
            border-radius: 10px;
            font-size: 16px;
            color: #ffffff; /* White text */
            background-color: #333; /* Darker input background */
        }

        button {
            background-color: #17a2b8; /* Turquoise button */
            color: #ffffff; /* White text */
            padding: 12px 24px;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            cursor: pointer;
            margin-top: 20px;
        }

        button:hover {
            background-color: #138496; /* Darker turquoise on hover */
        }
    </style>
</head>
<body>
<div class="container">
    <div class="title">Телефонная книга</div>
    <div>
        <form id="userForm">
            <label for="userName"></label><input type="text" id="userName" placeholder="Имя пользователя">
            <label for="userPhoneNumber"></label><input type="text" id="userPhoneNumber" pattern="[0-9+]" placeholder="Номер телефона">
            <button type="button" id="addUserButton">Добавить пользователя</button>
        </form>
    </div>
    <hr>
    <script>
        document.getElementById('addUserButton').addEventListener('click', function () {
            const userName = document.getElementById('userName').value;
            const userPhoneNumber = document.getElementById('userPhoneNumber').value;

            const phoneNumberPattern = /^[0-9+]+$/;
            if (!phoneNumberPattern.test(userPhoneNumber)) {
                alert("Invalid");
                return;
            }
            const usernamePatterns = /\S/g;
            if (!usernamePatterns.test(userName)) {
                alert("Invalid")
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
                    location.reload();
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
            <div class="user-name"><%= userName %></div>
            <div class="user-details">
                <strong>Phone Numbers:</strong> <%= String.join(", ", phoneNumbers) %>
            </div>
        </div>
        <%
            }
        %>
    </div>
</div>
</body>
</html>