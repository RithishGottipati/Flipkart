package com.myproject.aem.core.servlets;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.HttpConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Component(service = Servlet.class,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.paths=" + "/services/login"
        })
public class LoginServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(LoginServlet.class);
    private static final String DATA_FILE_PATH = "/Users/rithishgottipati/Desktop/AEM/aem-sdk/AEM_Backend/aemmyproject/ui.content/src/main/content/jcr_root/content/dam/aemmyproject/userdata.json";
    private static final String FILE_CHECK_URL = "/services/checkfile"; // Path to the FileCheckServlet

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        LOG.info("Received login request for username: {}", username);

        if (username == null || password == null) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid input");
            LOG.error("Invalid input: username or password is null");
            return;
        }

        if (!checkFileExists()) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("User data file not found or created");
            LOG.error("User data file not found or created");
            return;
        }

        try (FileInputStream inputStream = new FileInputStream(DATA_FILE_PATH)) {
            String jsonString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            JSONArray users = new JSONArray(jsonString);

            LOG.info("Read JSON file successfully");

            String hashedPassword = hashPassword(password);
            boolean validUser = false;
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (username.equals(user.getString("username")) && hashedPassword.equals(user.getString("password"))) {
                    validUser = true;
                    break;
                }
            }

            if (validUser) {
                response.setStatus(SlingHttpServletResponse.SC_OK);
                response.getWriter().write("Login successful");
                LOG.info("User {} logged in successfully", username);
            } else {
                response.setStatus(SlingHttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid username or password");
                LOG.warn("Invalid login attempt for username: {}", username);
            }
        } catch (IOException e) {
            LOG.error("Error reading user data", e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal Server Error");
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal Server Error");
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean checkFileExists() {
        try {
            URL url = new URL("http://localhost:4502" + FILE_CHECK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Basic Authentication header
            String userCredentials = "admin:admin"; // Use actual credentials
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
            connection.setRequestProperty("Authorization", basicAuth);

            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            LOG.error("Error checking file existence", e);
            return false;
        }
    }
}