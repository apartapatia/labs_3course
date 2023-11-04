package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import utils.DefaultServletHelper;
import utils.ServletHelper;


@Slf4j
@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginLogoutServlet extends HttpServlet {

    private final ServletHelper servletHelper = new DefaultServletHelper();
    @SneakyThrows
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession();
        JSONObject responseJson = new JSONObject();

        String jsonInput = servletHelper.readJsonInput(req);
        JSONObject inputJson = new JSONObject(jsonInput);

        String username = inputJson.getString("username");
        String password = inputJson.getString("password");

        if (servletHelper.checkCredentials(username, password)) {
            session.setAttribute("username", username);
            responseJson.put("success", true);
            responseJson.put("username", username);
        } else {
            responseJson.put("success", false);
        }

        servletHelper.sendJsonResponse(resp, responseJson);
    }

    @SneakyThrows
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession();
        JSONObject responseJson = new JSONObject();

        if (servletHelper.isUserAuthenticated(session)) {
            servletHelper.forwardToJSP(req, resp, "/view/login.jsp");
            String username = (String) session.getAttribute("username");
            log.info(username);
            responseJson.put("success", true);
            responseJson.put("username", username);
        } else {
            servletHelper.forwardToJSP(req, resp, "/view/login.jsp");
        }

        servletHelper.sendJsonResponse(resp, responseJson);
    }

    @SneakyThrows
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        req.getSession().invalidate();
        JSONObject responseJson = new JSONObject();
        responseJson.put("success", true);
        servletHelper.sendJsonResponse(resp, responseJson);
    }
}