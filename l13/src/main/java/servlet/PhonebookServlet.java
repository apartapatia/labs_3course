package servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/phonebook"})
public class PhonebookServlet extends HttpServlet {

    List<User> users;

    @Override
    public void init() throws ServletException {
        super.init();
        this.users = loadUsersFromPhonebook();
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
        req.setAttribute("users", users);
        getServletContext().getRequestDispatcher("/view/phonebook.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reader = req.getReader();
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
            String userName = jsonObject.get("name").getAsString();
            String userPhoneNumber = jsonObject.get("phoneNumber").getAsString();
            User newUser = new User(userName, userPhoneNumber);
            users.add(newUser);

            saveUsersToPhonebook();
        } catch (JsonSyntaxException | IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON data");
        }
    }


    private void saveUsersToPhonebook() {
        try {
            Path filePath = Paths.get("/home/meow/Documents/labs_3course/l13/src/main/resources/phonebook.txt");
            File file = filePath.toFile();

            try (Writer writer = new FileWriter(file)) {
                for (User user : users) {
                    writer.write(user.getName() + "," + user.getPhoneNumber() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private List<User> loadUsersFromPhonebook() {
        List<User> userList = new ArrayList<>();

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("phonebook.txt");
            if (inputStream == null) {
                throw new FileNotFoundException("phonebook.txt not found in resources.");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    userList.add(new User(parts[0].trim(), parts[1].trim()));
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userList;
    }


}
