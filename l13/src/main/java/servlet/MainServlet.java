package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/main"})
public class MainServlet extends HttpServlet {

    List<User> users;

    @Override
    public void init() throws ServletException {
        super.init();
        this.users = new ArrayList<>();
        users.add(new User("huy", "987-1010"));
        users.add(new User("huec", "9123-1010"));
        users.add(new User("huyak", "3333-1010"));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    public void destroy() {

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       String URI = req.getRequestURI();
       String params = formatParams(req);
       resp.getWriter().write("Method doGet\nURI: " + URI + "\nParams:\n" + params + "\n");
       for (User user : users) {
           resp.getWriter().write("name : " + user.getName() + "\n");
           resp.getWriter().write("phone : " + user.getPhoneNumber() + "\n");
       }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URI = req.getRequestURI();
        String params = formatParams(req);
        resp.getWriter().write("Method doPost\nURI: " + URI + "\nParams:\n" + params + "\n");
    }

    private String formatParams(HttpServletRequest req) {
        return req.getParameterMap()
                .entrySet()
                .stream()
                .map(entry -> {
                    String param = String.join(" and ", entry.getValue());
                    return entry.getKey() + " => " + param;
                })
                .collect(Collectors.joining("\n"));
    }

}
