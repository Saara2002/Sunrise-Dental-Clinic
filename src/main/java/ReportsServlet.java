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

@WebServlet("/reports")
public class ReportsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {

            Connection connection =
                    DBConnection.getConnection();

            // --------------------------------
            // Total Appointments
            // --------------------------------

            String sql1 =
                    "SELECT COUNT(*) FROM appointments";

            PreparedStatement ps1 =
                    connection.prepareStatement(sql1);

            ResultSet rs1 =
                    ps1.executeQuery();

            int totalAppointments = 0;

            if (rs1.next()) {
                totalAppointments = rs1.getInt(1);
            }

            rs1.close();
            ps1.close();


            // --------------------------------
            // Today's Appointments
            // --------------------------------

            String sql2 =
                    "SELECT COUNT(*) FROM appointments " +
                    "WHERE appointment_date = CURDATE() " +
                    "AND status <> 'CANCELLED'";

            PreparedStatement ps2 =
                    connection.prepareStatement(sql2);

            ResultSet rs2 =
                    ps2.executeQuery();

            int todayAppointments = 0;

            if (rs2.next()) {
                todayAppointments = rs2.getInt(1);
            }

            rs2.close();
            ps2.close();


            // --------------------------------
            // Pending / BOOKED
            // --------------------------------

            String sql3 =
                    "SELECT COUNT(*) FROM appointments " +
                    "WHERE status = 'BOOKED'";

            PreparedStatement ps3 =
                    connection.prepareStatement(sql3);

            ResultSet rs3 =
                    ps3.executeQuery();

            int pendingAppointments = 0;

            if (rs3.next()) {
                pendingAppointments = rs3.getInt(1);
            }

            rs3.close();
            ps3.close();


            // --------------------------------
            // Completed
            // --------------------------------

            String sql4 =
                    "SELECT COUNT(*) FROM appointments " +
                    "WHERE status = 'COMPLETED'";

            PreparedStatement ps4 =
                    connection.prepareStatement(sql4);

            ResultSet rs4 =
                    ps4.executeQuery();

            int completedAppointments = 0;

            if (rs4.next()) {
                completedAppointments = rs4.getInt(1);
            }

            rs4.close();
            ps4.close();


            // --------------------------------
            // Cancelled
            // --------------------------------

            String sql5 =
                    "SELECT COUNT(*) FROM appointments " +
                    "WHERE status = 'CANCELLED'";

            PreparedStatement ps5 =
                    connection.prepareStatement(sql5);

            ResultSet rs5 =
                    ps5.executeQuery();

            int cancelledAppointments = 0;

            if (rs5.next()) {
                cancelledAppointments = rs5.getInt(1);
            }

            rs5.close();
            ps5.close();


            // --------------------------------
            // Total Patients
            // --------------------------------

            String sql6 =
                    "SELECT COUNT(*) FROM patients";

            PreparedStatement ps6 =
                    connection.prepareStatement(sql6);

            ResultSet rs6 =
                    ps6.executeQuery();

            int totalPatients = 0;

            if (rs6.next()) {
                totalPatients = rs6.getInt(1);
            }

            rs6.close();
            ps6.close();


            // --------------------------------
            // Treatment Report
            // --------------------------------

            String sql7 =
                    "SELECT treatment_id, COUNT(*) AS total " +
                    "FROM appointments " +
                    "GROUP BY treatment_id " +
                    "ORDER BY treatment_id";

            PreparedStatement ps7 =
                    connection.prepareStatement(sql7);

            ResultSet rs7 =
                    ps7.executeQuery();


            StringBuilder treatments =
                    new StringBuilder();

            treatments.append("[");

            boolean firstTreatment = true;

            while (rs7.next()) {

                if (!firstTreatment) {
                    treatments.append(",");
                }

                treatments.append("{");

                treatments.append(
                        "\"treatmentId\":\""
                        + rs7.getString("treatment_id")
                        + "\","
                );

                treatments.append(
                        "\"count\":"
                        + rs7.getInt("total")
                );

                treatments.append("}");

                firstTreatment = false;
            }

            treatments.append("]");

            rs7.close();
            ps7.close();


            connection.close();


            // --------------------------------
            // JSON Response
            // --------------------------------

            out.print("{");

            out.print(
                    "\"totalAppointments\":"
                    + totalAppointments + ","
            );

            out.print(
                    "\"todayAppointments\":"
                    + todayAppointments + ","
            );

            out.print(
                    "\"pendingAppointments\":"
                    + pendingAppointments + ","
            );

            out.print(
                    "\"completedAppointments\":"
                    + completedAppointments + ","
            );

            out.print(
                    "\"cancelledAppointments\":"
                    + cancelledAppointments + ","
            );

            out.print(
                    "\"totalPatients\":"
                    + totalPatients + ","
            );

            out.print(
                    "\"bookedCount\":"
                    + pendingAppointments + ","
            );

            out.print(
                    "\"completedCount\":"
                    + completedAppointments + ","
            );

            out.print(
                    "\"cancelledCount\":"
                    + cancelledAppointments + ","
            );

            out.print(
                    "\"treatments\":"
                    + treatments
            );

            out.print("}");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"error\":\""
                    + e.getMessage()
                    + "\"}"
            );
        }
    }
}