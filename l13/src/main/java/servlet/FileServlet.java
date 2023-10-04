package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/file"})
@MultipartConfig(location = "/home/meow/Documents/labs_3course/l13")
public class FileServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        for (Part part : req.getParts()) {
            if (part.getName().equals("author")) {
                handleAuthorPart(part);
            } else {
                handleFilePart(part);
            }
        }
        resp.sendRedirect("/l13/main");
    }

    private void handleAuthorPart(Part part) throws IOException {
        InputStream inputStream = part.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        String authorName = new BufferedReader(isr)
                .lines()
                .collect(Collectors.joining("\n"));
        log(authorName);
    }

    private void handleFilePart(Part part) throws IOException {
        String fileName = UUID.randomUUID() + part.getSubmittedFileName();
        part.write(fileName);
    }
}