package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.AdvertisementsModel;
import utils.DefaultServletHelper;
import utils.ServletHelper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@WebServlet(urlPatterns = "/main")
public class AdvertisementsServlet extends HttpServlet {
    private final List<AdvertisementsModel> advertisements = new ArrayList<>();
    private final ServletHelper servletHelper = new DefaultServletHelper();

    @SneakyThrows
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  {
        String username = (String) req.getSession().getAttribute("username");
        req.setAttribute("username", username);
        req.setAttribute("advertisements", advertisements);
        req.getRequestDispatcher("/view/advertisements.jsp").forward(req, resp);
    }

    @SneakyThrows
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession();
        String username = (String) req.getSession().getAttribute("username");

        if (servletHelper.isUserAuthenticated(session)) {
            String title = req.getParameter("title");
            String text = req.getParameter("text");

            if (title != null && text != null) {
                AdvertisementsModel advertisement = new AdvertisementsModel(title, text, username);
                advertisements.add(advertisement);
            }
        }

        resp.sendRedirect(req.getContextPath() + "/main");
    }
}
