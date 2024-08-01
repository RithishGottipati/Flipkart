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

@Component(service = Servlet.class,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_POST,
                "sling.servlet.paths=" + "/bin/signup"
        })
public class SignupServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SignupServlet.class);
    private static final String FILE_PATH = "/Users/rithishgottipati/Desktop/AEM/aem-sdk/AEM_Backend/aemmyproject/ui.content/src/main/content/jcr_root/content/dam/aemmyproject/userdata.json";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (username == null || password == null) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid input");
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

    private JSONArray readUserFile() throws IOException {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                return new JSONArray(content);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return new JSONArray();
    }

    private void writeFile(String data) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(FILE_PATH)) {
            IOUtils.write(data, outputStream, StandardCharsets.UTF_8);
        }
    }
}