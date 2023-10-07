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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/phonebook"})
public class PhonebookServlet extends HttpServlet {

    List<User> users;

    @Override
    public void init() throws ServletException {
        super.init();
        this.users = new ArrayList<>();
        this.users.addAll(loadUsersFromPhonebook());
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


            User existingUser = findUserByName(userName);
            if (existingUser != null) {
                if (!existingUser.getPhoneNumbers().contains(userPhoneNumber)) {
                    existingUser.addPhoneNumber(userPhoneNumber);
                }
            } else {
                User newUser = new User(userName);
                newUser.addPhoneNumber(userPhoneNumber);
                users.add(newUser);
            }

            saveUsersToPhonebook();
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (JsonSyntaxException | IllegalStateException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON data");
        }
    }

    private User findUserByName(String userName) {
        return users.stream()
                .filter(user -> user.getName().equals(userName))
                .findFirst()
                .orElse(null);
    }

    private void saveUsersToPhonebook() {
        Path filePath = Paths.get("/home/meow/Documents/clone/labs_3course/l13/src/main/resources/phonebook.txt");
        try {
            Files.write(filePath, users.stream()
                    .flatMap(user -> user.getPhoneNumbers().stream()
                            .map(phoneNumber -> user.getName() + "," + phoneNumber + "\n"))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<User> loadUsersFromPhonebook() {
        List<User> userList = new ArrayList<>();

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("phonebook.txt");
            if (inputStream == null) {
                throw new FileNotFoundException("phonebook.txt not found in resources.");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            userList = reader.lines()
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length == 2)
                    .map(parts -> {
                        String userName = parts[0].trim();
                        String phoneNumber = parts[1].trim();
                        User existingUser = findUserByName(userName);
                        if (existingUser != null) {
                            existingUser.addPhoneNumber(phoneNumber);
                        } else {
                            User user = new User(userName);
                            user.addPhoneNumber(phoneNumber);
                            return user;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userList;
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
