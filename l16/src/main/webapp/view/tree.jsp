<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
    <title>Список</title>
</head>
<body>
<h2>Tree View</h2>
<ol id="treeList"></ol>

<button onclick="toggleAllLists()">Скрыть все списки</button>

<form id="deleteForm" action="tree" method="post">
    <button type="submit" onclick="return confirm('Вы точно хотите удалить все списки?')">Удалить все списки</button>
</form>

<%
    if (request.getAttribute("treeDataJson") != null) {
        String treeDataJson = (String) request.getAttribute("treeDataJson");
%>
<script>
    window.addEventListener("load", function () {
        var treeData = <%= treeDataJson %>;
        var treeList = document.getElementById("treeList");

        function createTree(treeData, parentElement, isSecondLevel) {
            for (var i = 0; i < treeData.length; i++) {
                var item = treeData[i];

                var li = document.createElement("li");
                var span = document.createElement("span");
                var ul = document.createElement("ul");

                span.className = "expand-icon";
                span.innerHTML = isSecondLevel ? "" : "[+]";
                span.onclick = function () {
                    if (!isSecondLevel) {
                        toggleChildren(this);
                    }
                };

                li.appendChild(span);
                li.appendChild(document.createTextNode(item.name));
                li.appendChild(ul);

                if (item.children && item.children.length > 0) {
                    createTree(item.children, ul, true);
                    ul.style.display = isSecondLevel ? 'block' : 'none';
                }

                parentElement.appendChild(li);
            }
        }

        createTree(treeData, treeList, false);
    });

    function toggleChildren(element) {
        var parentLi = element.parentNode;
        var childrenList = parentLi.querySelector("ul");

        if (childrenList.style.display === 'none' || childrenList.style.display === '') {
            childrenList.style.display = 'block';
            element.innerHTML = '[-]';
        } else {
            childrenList.style.display = 'none';
            element.innerHTML = '[+]';
        }
    }

    function toggleAllLists() {
        var allLists = document.querySelectorAll("ul");
        for (var i = 0; i < allLists.length; i++) {
            var list = allLists[i];
            list.style.display = 'none';
        }
    }

</script>
<%
    }
%>

</body>
</html>
