package com.myproject.aem.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.HttpConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component(service = Servlet.class,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.paths=" + "/services/checkfile"
        })
public class FileCheckServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(FileCheckServlet.class);
    private static final String DATA_FILE_PATH = "/Users/rithishgottipati/Desktop/AEM/aem-sdk/AEM_Backend/aemmyproject/ui.content/src/main/content/jcr_root/content/dam/aemmyproject/userdata.json";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        File file = new File(DATA_FILE_PATH);

        if (file.exists()) {
            response.setStatus(SlingHttpServletResponse.SC_OK);
            response.getWriter().write("true");
            LOG.info("File exists: {}", DATA_FILE_PATH);
        } else {
            createFile(file);
            response.setStatus(SlingHttpServletResponse.SC_OK);
            response.getWriter().write("false");
            LOG.info("File created: {}", DATA_FILE_PATH);
        }
    }

    private void createFile(File file) throws IOException {
        if (file.createNewFile()) {
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                // Optionally write some initial content to the file
                outputStream.write("[]".getBytes(StandardCharsets.UTF_8)); // Empty JSON array
            }
        } else {
            throw new IOException("Failed to create the file.");
        }
    }
}