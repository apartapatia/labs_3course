<%@ page import="model.AdvertisementsModel" %>
<%@ page import="model.TreeItem" %>
<%@ page import="java.util.List" %>
<%@ page import="static java.lang.System.out" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%!
    // Move the function outside the scriptlet
    void printTree(TreeItem node) {
        out.print("<li><strong>" + node.getName() + "</strong>");
        if (!node.getChildren().isEmpty()) {
            out.print("<ul>");
            for (TreeItem child : node.getChildren()) {
                printTree(child);
            }
            out.print("</ul>");
        }
        out.print("</li>");
    }
%>

<html>
<head>
    <title>Доска объявлений</title>
    <link type="text/css" rel="stylesheet" href="./view/style_two.css">
</head>
<body>
<a href="login"><button>Личный кабинет</button></a>
<%
    String username = (String) request.getAttribute("username");
%>
<h1>Доска объявлений ϟ</h1>
<% if (username != null) { %>
<form method="post">
    <label for="title">Заголовок объявления:</label>
    <input type="text" id="title" name="title" required><br>
    <label for="text">Текст объявления:</label>
    <textarea id="text" name="text" rows="4" required></textarea><br>
    <input type="submit" value="Добавить объявление">
</form>
<% } else { %>
<p>Для добавления объявления, пожалуйста, войдите.</p>
<% } %>

<div>
    <h2>Категории</h2>
    <ul>
        <%
            TreeItem treeRoot = (TreeItem) request.getAttribute("treeRoot");
            if (treeRoot != null) {
                printTree(treeRoot);
            }
        %>
    </ul>
</div>

<h2>Объявления</h2>
<ul>
    <%
        List<AdvertisementsModel> advertisements = (List<AdvertisementsModel>) request.getAttribute("advertisements");
        if (advertisements != null) {
            for (AdvertisementsModel advertisement : advertisements) {
    %>
    <li>
        <strong><%= advertisement.getTitle() %></strong><br>
        <%= advertisement.getText() %><br>
        <i>Автор: <%= advertisement.getUsername() %></i><br>
        <i>Дата: <%= advertisement.getDate() %></i>
    </li>
    <%
            }
        }
    %>
</ul>
<div class="centered-content">
    <div class="image-container"></div>
</div>

<%
    // Initiate the printing of the tree
    printTree(treeRoot);
%>

</body>
</html>
