package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class PhonebookServletTest {
    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    private PhonebookServlet phonebookServlet;

    @BeforeEach
    public void setUp() throws ServletException {
        initMocks(this);
        phonebookServlet = new PhonebookServlet();
        phonebookServlet.init();
    }
    @ParameterizedTest
    @CsvSource({
            "John Doe, 1234567890",
            "Jane Smith, 9876543210"
    })
    public void testDoPost_whenRightString(String name, String phoneNumber) throws IOException, ServletException {
        String requestBody = String.format("{\"name\":\"%s\",\"phoneNumber\":\"%s\"}", name, phoneNumber);
        when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        phonebookServlet.doPost(mockRequest, mockResponse);

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
    }
}
