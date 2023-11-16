package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class DefaultServletHelper implements ServletHelper {

    @SneakyThrows
    public String readJsonInput(HttpServletRequest req) {
        try (BufferedReader reader = req.getReader()) {
            StringBuilder jsonInput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonInput.append(line);
            }
            return jsonInput.toString();
        }
    }
    @SneakyThrows
    public void sendJsonResponse(HttpServletResponse resp, JSONObject responseJson) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(responseJson.toString());
    }
    @SneakyThrows
    public void forwardToJSP(HttpServletRequest req, HttpServletResponse resp, String path)  {
        req.getServletContext().getRequestDispatcher(path).forward(req, resp);
    }

    public boolean isUserAuthenticated(HttpSession session) {
        return session.getAttribute("username") != null;
    }

    public boolean checkCredentials(String username, String password) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("dataBase.txt")) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error reading database file", e);
        }
        return false;
    }
}