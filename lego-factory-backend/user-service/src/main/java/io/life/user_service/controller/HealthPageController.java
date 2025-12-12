package io.life.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class HealthPageController {

    @Autowired
    private DataSource dataSource;

    @GetMapping(value = "/status", produces = MediaType.TEXT_HTML_VALUE)
    public String getHealthPage() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>User Service Health Status</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:40px;} ");
        html.append(".status{color:green;font-weight:bold;} ");
        html.append(".error{color:red;font-weight:bold;} ");
        html.append("table{border-collapse:collapse;width:100%;} ");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;} ");
        html.append("th{background-color:#f2f2f2;}</style></head><body>");
        
        html.append("<h1>🔧 User Service Health Status</h1>");
        html.append("<p><strong>Status:</strong> <span class='status'>🟢 UP</span></p>");
        html.append("<p><strong>Service:</strong> User Service</p>");
        html.append("<p><strong>Timestamp:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</p>");
        
        // Memory information
        Runtime runtime = Runtime.getRuntime();
        html.append("<h2>💾 Memory Usage</h2>");
        html.append("<table>");
        html.append("<tr><th>Type</th><th>Value</th></tr>");
        html.append("<tr><td>Total Memory</td><td>").append(runtime.totalMemory() / 1024 / 1024).append(" MB</td></tr>");
        html.append("<tr><td>Free Memory</td><td>").append(runtime.freeMemory() / 1024 / 1024).append(" MB</td></tr>");
        html.append("<tr><td>Used Memory</td><td>").append((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024).append(" MB</td></tr>");
        html.append("<tr><td>Max Memory</td><td>").append(runtime.maxMemory() / 1024 / 1024).append(" MB</td></tr>");
        html.append("</table>");
        
        // Database status
        html.append("<h2>🗄️ Database Status</h2>");
        try (Connection connection = dataSource.getConnection()) {
            html.append("<p><strong>Status:</strong> <span class='status'>🟢 UP</span></p>");
            html.append("<p><strong>Database:</strong> ").append(connection.getMetaData().getDatabaseProductName()).append("</p>");
            html.append("<p><strong>Driver:</strong> ").append(connection.getMetaData().getDriverName()).append("</p>");
        } catch (Exception e) {
            html.append("<p><strong>Status:</strong> <span class='error'>🔴 DOWN</span></p>");
            html.append("<p><strong>Error:</strong> ").append(e.getMessage()).append("</p>");
        }
        
        // System information
        html.append("<h2>⚙️ System Information</h2>");
        html.append("<table>");
        html.append("<tr><th>Property</th><th>Value</th></tr>");
        html.append("<tr><td>Java Version</td><td>").append(System.getProperty("java.version")).append("</td></tr>");
        html.append("<tr><td>Operating System</td><td>").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("</td></tr>");
        html.append("<tr><td>Available Processors</td><td>").append(Runtime.getRuntime().availableProcessors()).append("</td></tr>");
        html.append("</table>");
        
        html.append("<h2>🔗 Available Endpoints</h2>");
        html.append("<ul>");
        html.append("<li><a href='/'>JSON Health Stats</a></li>");
        html.append("<li><a href='/health'>Simple Health Check</a></li>");
        html.append("<li><a href='/actuator/health'>Actuator Health</a></li>");
        html.append("<li><a href='/h2-console'>H2 Database Console</a></li>");
        html.append("</ul>");
        
        html.append("</body></html>");
        return html.toString();
    }
}