package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.AdvertisementsModel;
import model.TreeItem;
import utils.DefaultServletHelper;
import utils.ServletHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@WebServlet(urlPatterns = "/main")
public class AdvertisementsServlet extends HttpServlet {
    private final List<AdvertisementsModel> advertisements = new ArrayList<>();
    private final ServletHelper servletHelper = new DefaultServletHelper();
    private final TreeItem treeRoot;

    @SneakyThrows
    public AdvertisementsServlet() {
        // Read data from the file and build the tree
        treeRoot = buildTreeFromDatabaseFile("dataBase.txt");
    }

    @SneakyThrows
    private TreeItem buildTreeFromDatabaseFile(String filePath) {
        TreeItem root = new TreeItem("Root", 0); // Root item to hold the top-level categories
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        TreeItem currentCategory = null;

        while ((line = reader.readLine()) != null) {
            int level = line.lastIndexOf('*') + 1; // Calculate the level based on the number of '*'
            String categoryName = line.trim().replace("*", "");

            TreeItem category = new TreeItem(categoryName, level);

            if (level == 0) {
                // Top-level category
                root.addChild(category);
                currentCategory = category;
            } else {
                // Sub-category
                if (currentCategory != null) {
                    currentCategory.addChild(category);
                }
            }
        }

        reader.close();
        return root;
    }

    @SneakyThrows
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String username = (String) req.getSession().getAttribute("username");
        req.setAttribute("username", username);
        req.setAttribute("advertisements", advertisements);
        req.setAttribute("treeRoot", treeRoot); // Pass the tree root to the JSP
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
