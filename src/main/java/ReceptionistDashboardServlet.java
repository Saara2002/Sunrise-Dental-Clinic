import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/receptionist-dashboard")
public class ReceptionistDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        int todayAppointments = 0;
        int totalPatients = 0;
        int pendingAppointments = 0;

        try {

            Connection connection = DBConnection.getConnection();

            // Today's Appointments
            String sql1 =
                "SELECT COUNT(*) FROM appointments " +
                "WHERE appointment_date = CURDATE() " +
                "AND status <> 'CANCELLED'";

            PreparedStatement ps1 =
                connection.prepareStatement(sql1);

            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {
                todayAppointments = rs1.getInt(1);
            }

            rs1.close();
            ps1.close();


            // Total Patients
            String sql2 =
                "SELECT COUNT(*) FROM patients";

            PreparedStatement ps2 =
                connection.prepareStatement(sql2);

            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {
                totalPatients = rs2.getInt(1);
            }

            rs2.close();
            ps2.close();


            // Pending Appointments
            String sql3 =
                "SELECT COUNT(*) FROM appointments " +
                "WHERE status = 'BOOKED'";

            PreparedStatement ps3 =
                connection.prepareStatement(sql3);

            ResultSet rs3 = ps3.executeQuery();

            if (rs3.next()) {
                pendingAppointments = rs3.getInt(1);
            }

            rs3.close();
            ps3.close();

            connection.close();


            // Send JSON response
            PrintWriter out = response.getWriter();

            out.print("{");
            out.print("\"todayAppointments\":" + todayAppointments + ",");
            out.print("\"totalPatients\":" + totalPatients + ",");
            out.print("\"pendingAppointments\":" + pendingAppointments);
            out.print("}");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            PrintWriter out = response.getWriter();

            out.print("{");
            out.print("\"error\":\"" + e.getMessage() + "\"");
            out.print("}");
        }
    }
}