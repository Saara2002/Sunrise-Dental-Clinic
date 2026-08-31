

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/appointment-action")
public class AppointmentActionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String action =
                request.getParameter("action");

        String id =
                request.getParameter("id");

        try {

            Connection connection =
                    DBConnection.getConnection();

            String newStatus = "";

            if ("cancel".equals(action)) {

                newStatus = "CANCELLED";

            } else if ("complete".equals(action)) {

                newStatus = "COMPLETED";

            } else {

                response.sendRedirect("appointments.html");
                return;
            }

            String sql =
                    "UPDATE appointments " +
                    "SET status = ? " +
                    "WHERE appointment_id = ?";

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setString(1, newStatus);
            statement.setInt(2, Integer.parseInt(id));

            statement.executeUpdate();

            statement.close();
            connection.close();

            response.sendRedirect("appointments.html");

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType("text/html");

            response.getWriter().println(
                "<h2>Database Error</h2>"
            );

            response.getWriter().println(
                "<p>" + e.getMessage() + "</p>"
            );
        }
    }
}