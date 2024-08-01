package com.myproject.aem.core.servlets;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.HttpConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Component(service = Servlet.class,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_POST,
                "sling.servlet.paths=" + "/bin/signup"
        })
public class SignupServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SignupServlet.class);
    private static final String DATA_FILE_PATH = "/Users/rithishgottipati/Desktop/AEM/aem-sdk/AEM_Backend/aemmyproject/ui.content/src/main/content/jcr_root/content/dam/aemmyproject/userdata.json";
    private static final String FILE_CHECK_URL = "/services/checkfile"; // Path to the FileCheckServlet

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        LOG.info("Received signup request for username: {}", username);

        if (username == null || password == null || !password.equals(confirmPassword)) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid input");
            LOG.error("Invalid input: username or password is null or passwords do not match");
            return;
        }

        if (!checkFileExists()) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("User data file not found or created");
            LOG.error("User data file not found or created");
            return;
        }

        try {
            String hashedPassword = hashPassword(password);
            JSONObject newUser = new JSONObject();
            newUser.put("username", username);
            newUser.put("password", hashedPassword);

            JSONArray userArray = readUserFile();
            userArray.put(newUser);

            writeFile(userArray.toString());

            response.setStatus(SlingHttpServletResponse.SC_OK);
            response.getWriter().write("User registered successfully");
            LOG.info("User {} registered successfully", username);
        } catch (Exception e) {
            LOG.error("Error storing user data", e);
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

    private JSONArray readUserFile() throws IOException, JSONException {
        File file = new File(DATA_FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                return new JSONArray(content);
            }
        }
        return new JSONArray();
    }

    private void writeFile(String data) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(DATA_FILE_PATH)) {
            IOUtils.write(data, outputStream, StandardCharsets.UTF_8);
        }
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