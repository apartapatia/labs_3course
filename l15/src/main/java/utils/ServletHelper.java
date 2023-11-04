package utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;

import java.io.IOException;

public interface ServletHelper {
    String readJsonInput(HttpServletRequest req) throws IOException;

    void sendJsonResponse(HttpServletResponse resp, JSONObject responseJson) throws IOException;

    void forwardToJSP(HttpServletRequest req, HttpServletResponse resp, String path) throws ServletException, IOException;

    boolean isUserAuthenticated(HttpSession session);

    boolean checkCredentials(String username, String password);
}