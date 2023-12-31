package servlet;

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
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/phonebook", "/phonebook/add", "/phonebook/delete"})
public class PhonebookServlet extends HttpServlet {

    private static final String PHONEBOOK_FILE_PATH = "/home/meow/Documents/clone/labs_3course/l13/src/main/resources/phonebook.txt";

    private List<User> users;

    @Override
    public void init() throws ServletException {
        super.init();
        this.users = new ArrayList<>();
        this.users.addAll(loadUsersFromPhonebook());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("users", users);
        getServletContext().getRequestDispatcher("/view/phonebook.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (var reader = req.getReader()) {
            var requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            try {
                var jsonObject = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String userName = jsonObject.get("name").getAsString();
                String userPhoneNumber = jsonObject.get("phoneNumber").getAsString();

                Optional<User> optionalUser = users.stream()
                        .filter(user -> user.getName().equals(userName))
                        .findFirst();

                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    if (!user.getPhoneNumbers().contains(userPhoneNumber)) {
                        user.addPhoneNumber(userPhoneNumber);
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
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            var requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            try {
                var jsonObject = JsonParser.parseString(requestBody.toString()).getAsJsonObject();
                String userName = jsonObject.get("name").getAsString();
                String userPhoneNumber = jsonObject.get("phoneNumber").getAsString();

                if (userName.isEmpty() && userPhoneNumber.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Both name and phoneNumber are empty.");
                    return;
                }

                Optional<User> userOptional = users.stream()
                        .filter(user -> user.getName().equals(userName))
                        .findFirst();

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    if (!userPhoneNumber.isEmpty()) {
                        boolean phoneNumberRemoved = user.getPhoneNumbers().remove(userPhoneNumber);
                        if (!phoneNumberRemoved) {
                            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User with specified phoneNumber not found.");
                            return;
                        }
                        if (user.getPhoneNumbers().isEmpty()) {
                            users.remove(user);
                        }
                    } else {
                        users.remove(user);
                    }
                    saveUsersToPhonebook();
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User with specified name not found.");
                }
            } catch (JsonSyntaxException | IllegalStateException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON data");
            }
        }
    }

    private User findUserByName(String userName) {
        return users.stream()
                .filter(user -> user.getName().equals(userName))
                .findFirst()
                .orElse(null);
    }

    private void saveUsersToPhonebook() {
        Path filePath = Paths.get(PHONEBOOK_FILE_PATH);
        try {
            Files.write(filePath, users.stream()
                    .flatMap(user -> user.getPhoneNumbers().stream()
                            .map(phoneNumber -> user.getName() + "," + phoneNumber + "\n"))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            log(e.getMessage());
        }
    }

    private List<User> loadUsersFromPhonebook() {
        List<User> userList = new ArrayList<>();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("phonebook.txt")) {
            if (inputStream == null) {
                throw new FileNotFoundException("phonebook.txt not found in resources.");
            }

            var reader = new BufferedReader(new InputStreamReader(inputStream));

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

        } catch (IOException e) {
            log(e.getMessage());
        }

        return userList;
    }
}
