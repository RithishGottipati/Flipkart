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

@Component(service = Servlet.class,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_DELETE,
                "sling.servlet.paths=" + "/services/deleteuser"
        })
public class DeleteUserServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUserServlet.class);
    private static final String DATA_FILE_PATH = "/Users/rithishgottipati/Desktop/AEM/aem-sdk/AEM_Backend/aemmyproject/ui.content/src/main/content/jcr_root/content/dam/aemmyproject/userdata.json";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String username = request.getParameter("username");

        LOG.info("Received delete request for username: {}", username);

        if (username == null || username.isEmpty()) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Username is required");
            LOG.error("Invalid input: username is null or empty");
            return;
        }

        try {
            JSONArray users = readUserFile();
            boolean userFound = false;

            // Remove the user from the JSON array
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (username.equals(user.getString("username"))) {
                    users.remove(i);
                    userFound = true;
                    break;
                }
            }

            if (!userFound) {
                response.setStatus(SlingHttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("User not found");
                LOG.warn("User {} not found", username);
                return;
            }

            // Write updated JSON data back to the file
            if (users.length() > 0) {
                writeFile(users.toString());
                response.setStatus(SlingHttpServletResponse.SC_OK);
                response.getWriter().write("User deleted successfully");
                LOG.info("User {} deleted successfully", username);
            } else {
                // Delete the file if it's empty
                File file = new File(DATA_FILE_PATH);
                if (file.delete()) {
                    response.setStatus(SlingHttpServletResponse.SC_OK);
                    response.getWriter().write("User deleted and file removed");
                    LOG.info("User {} deleted and file removed", username);
                } else {
                    response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("User deleted but file removal failed");
                    LOG.error("User {} deleted but file removal failed", username);
                }
            }
        } catch (IOException | JSONException e) {
            LOG.error("Error handling user data", e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal Server Error");
        }
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
}