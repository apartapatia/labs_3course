package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import model.TreeItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@WebServlet(urlPatterns = "/tree")
public class TreeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<TreeItem> treeData = loadTreeDataFromFile();

        if (!treeData.isEmpty()) {
            String jsonData = new ObjectMapper().writeValueAsString(treeData);
            request.setAttribute("treeDataJson", jsonData);
        }

        response.setContentType("text/html;charset=UTF-8");

        RequestDispatcher dispatcher = request.getRequestDispatcher("/view/tree.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        deleteListFromDatabase();
        response.sendRedirect(request.getContextPath() + "/tree");
    }

    private void deleteListFromDatabase() {
        String filePath = getServletContext().getRealPath("/WEB-INF/classes/dataBase.txt");

        try (PrintWriter writer = new PrintWriter(filePath)) {
            writer.print("");
        } catch (FileNotFoundException e) {
            log.info(e.getMessage());
        }
    }

    private List<TreeItem> loadTreeDataFromFile() throws IOException {
        List<TreeItem> treeData = new ArrayList<>();
        String filePath = getServletContext().getRealPath("/WEB-INF/classes/dataBase.txt");

        File file = new File(filePath);
        if (!file.exists()) {
            return treeData;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            TreeItem currentItem = null;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                int level = getIndentationLevel(line);
                String itemName = line.trim().replaceAll("\\*", "");

                TreeItem newItem = new TreeItem(itemName, level);

                if (currentItem == null || level == 0) {
                    treeData.add(newItem);
                    currentItem = newItem;
                } else {
                    currentItem.addChild(newItem);
                }
            }
        }
        return treeData;
    }

    private int getIndentationLevel(String line) {
        int level = 0;
        while (line.startsWith("    ")) {
            level++;
            line = line.substring(4);
        }
        return level;
    }
}
