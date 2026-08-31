

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

@WebServlet("/appointments")
public class AppointmentsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        String sql =
                "SELECT " +
                "a.appointment_id, " +
                "a.appointment_number, " +
                "p.full_name, " +
                "a.dentist_name, " +
                "a.treatment_id, " +
                "a.appointment_date, " +
                "a.appointment_time, " +
                "a.status " +
                "FROM appointments a " +
                "INNER JOIN patients p " +
                "ON a.patient_id = p.patient_id " +
                "ORDER BY a.appointment_date, a.appointment_time";

        try {

            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery();

            boolean hasData = false;

            while (result.next()) {

                hasData = true;

                int appointmentId =
                        result.getInt("appointment_id");

                String appointmentNumber =
                        result.getString("appointment_number");

                String patientName =
                        result.getString("full_name");

                String dentist =
                        result.getString("dentist_name");

                int treatmentId =
                        result.getInt("treatment_id");

                String date =
                        result.getString("appointment_date");

                String time =
                        result.getString("appointment_time");

                String status =
                        result.getString("status");

                String statusClass =
                        status.toLowerCase();

                out.println("<tr>");

                out.println(
                    "<td><strong>" +
                    appointmentNumber +
                    "</strong></td>"
                );

                out.println(
                    "<td>" +
                    patientName +
                    "</td>"
                );

                out.println(
                    "<td>" +
                    dentist +
                    "</td>"
                );

                out.println(
                    "<td>" +
                    getTreatmentName(treatmentId) +
                    "</td>"
                );

                out.println(
                    "<td>" +
                    date +
                    "</td>"
                );

                out.println(
                    "<td>" +
                    time +
                    "</td>"
                );

                out.println(
                    "<td>" +
                    "<span class='status " +
                    statusClass +
                    "'>" +
                    status +
                    "</span>" +
                    "</td>"
                );

                // ACTIONS

                out.println("<td>");

                if (status.equals("BOOKED")) {

                    out.println(
                        "<a href='edit-appointment.html?id=" +
                        appointmentId +
                        "' " +
                        "style='margin-right:8px;'>✏️</a>"
                    );

                    out.println(
                        "<a href='appointment-action?action=cancel&id=" +
                        appointmentId +
                        "' " +
                        "style='margin-right:8px;' " +
                        "onclick=\"return confirm('Cancel this appointment?');\">" +
                        "❌</a>"
                    );

                    out.println(
                        "<a href='appointment-action?action=complete&id=" +
                        appointmentId +
                        "' " +
                        "onclick=\"return confirm('Mark this appointment as completed?');\">" +
                        "✅</a>"
                    );

                } else {

                    out.println(
                        "<span style='color:#777;'>No actions</span>"
                    );

                }

                out.println("</td>");

                out.println("</tr>");
            }

            if (!hasData) {

                out.println(
                    "<tr>" +
                    "<td colspan='8' class='empty'>" +
                    "No appointments found." +
                    "</td>" +
                    "</tr>"
                );
            }

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {

            e.printStackTrace();

            out.println(
                "<tr>" +
                "<td colspan='8' class='empty'>" +
                "Database Error: " +
                e.getMessage() +
                "</td>" +
                "</tr>"
            );
        }
    }

    private String getTreatmentName(int treatmentId) {

        switch (treatmentId) {

            case 1:
                return "Dental Consultation";

            case 2:
                return "Teeth Cleaning";

            case 3:
                return "Dental Filling";

            case 4:
                return "Tooth Extraction";

            case 5:
                return "Root Canal Treatment";

            case 6:
                return "Teeth Whitening";

            default:
                return "Unknown Treatment";
        }
    }
}