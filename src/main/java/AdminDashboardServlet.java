

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

@WebServlet("/admin-dashboard")
public class AdminDashboardServlet extends HttpServlet {


private static final long serialVersionUID = 1L;

@Override
protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    PrintWriter out = response.getWriter();

    Connection connection = null;

    try {

        connection = DBConnection.getConnection();

        /* =====================================================
           1. TODAY'S APPOINTMENTS
           ===================================================== */

        String todayAppointmentsSql =
                "SELECT COUNT(*) AS total " +
                "FROM appointments " +
                "WHERE appointment_date = CURDATE()";

        PreparedStatement ps1 =
                connection.prepareStatement(todayAppointmentsSql);

        ResultSet rs1 =
                ps1.executeQuery();

        int todayAppointments = 0;

        if (rs1.next()) {
            todayAppointments =
                    rs1.getInt("total");
        }

        rs1.close();
        ps1.close();


        /* =====================================================
           2. TOTAL PATIENTS
           ===================================================== */

        String totalPatientsSql =
                "SELECT COUNT(*) AS total " +
                "FROM patients";

        PreparedStatement ps2 =
                connection.prepareStatement(totalPatientsSql);

        ResultSet rs2 =
                ps2.executeQuery();

        int totalPatients = 0;

        if (rs2.next()) {
            totalPatients =
                    rs2.getInt("total");
        }

        rs2.close();
        ps2.close();


        /* =====================================================
           3. COMPLETED APPOINTMENTS TODAY
           ===================================================== */

        String completedSql =
                "SELECT COUNT(*) AS total " +
                "FROM appointments " +
                "WHERE appointment_date = CURDATE() " +
                "AND status = 'COMPLETED'";

        PreparedStatement ps3 =
                connection.prepareStatement(completedSql);

        ResultSet rs3 =
                ps3.executeQuery();

        int completed = 0;

        if (rs3.next()) {
            completed =
                    rs3.getInt("total");
        }

        rs3.close();
        ps3.close();


        /* =====================================================
           4. CANCELLED APPOINTMENTS TODAY
           ===================================================== */

        String cancelledSql =
                "SELECT COUNT(*) AS total " +
                "FROM appointments " +
                "WHERE appointment_date = CURDATE() " +
                "AND status = 'CANCELLED'";

        PreparedStatement ps4 =
                connection.prepareStatement(cancelledSql);

        ResultSet rs4 =
                ps4.executeQuery();

        int cancelled = 0;

        if (rs4.next()) {
            cancelled =
                    rs4.getInt("total");
        }

        rs4.close();
        ps4.close();


        /* =====================================================
           5. TODAY'S APPOINTMENT LIST
           
           IMPORTANT:
           patient name comes from patients.full_name
           NOT appointments.patient_name
           ===================================================== */

        String appointmentsSql =
                "SELECT " +
                "a.appointment_number, " +
                "p.full_name, " +
                "a.dentist_name, " +
                "a.appointment_time, " +
                "a.status " +
                "FROM appointments a " +
                "INNER JOIN patients p " +
                "ON a.patient_id = p.patient_id " +
                "WHERE a.appointment_date = CURDATE() " +
                "ORDER BY a.appointment_time ASC";

        PreparedStatement ps5 =
                connection.prepareStatement(appointmentsSql);

        ResultSet rs5 =
                ps5.executeQuery();


        /* =====================================================
           6. CREATE JSON RESPONSE
           ===================================================== */

        StringBuilder json =
                new StringBuilder();

        json.append("{");


        /* ================= CARDS ================= */

        json.append(
                "\"todayAppointments\":"
                + todayAppointments
                + ","
        );

        json.append(
                "\"totalPatients\":"
                + totalPatients
                + ","
        );

        json.append(
                "\"completed\":"
                + completed
                + ","
        );

        json.append(
                "\"cancelled\":"
                + cancelled
                + ","
        );


        /* ================= APPOINTMENTS ================= */

        json.append("\"appointments\":[");

        boolean first = true;

        while (rs5.next()) {

            if (!first) {
                json.append(",");
            }

            first = false;


            String appointmentNumber =
                    rs5.getString(
                            "appointment_number"
                    );

            String patientName =
                    rs5.getString(
                            "full_name"
                    );

            String dentistName =
                    rs5.getString(
                            "dentist_name"
                    );

            String appointmentTime =
                    rs5.getString(
                            "appointment_time"
                    );

            String status =
                    rs5.getString(
                            "status"
                    );


            json.append("{");


            json.append(
                    "\"appointmentNumber\":\""
                    + escapeJson(appointmentNumber)
                    + "\","
            );


            json.append(
                    "\"patientName\":\""
                    + escapeJson(patientName)
                    + "\","
            );


            json.append(
                    "\"dentistName\":\""
                    + escapeJson(dentistName)
                    + "\","
            );


            json.append(
                    "\"appointmentTime\":\""
                    + escapeJson(appointmentTime)
                    + "\","
            );


            json.append(
                    "\"status\":\""
                    + escapeJson(status)
                    + "\""
            );


            json.append("}");

        }

        json.append("]");

        json.append("}");


        /* =====================================================
           SEND JSON TO HTML
           ===================================================== */

        out.print(json.toString());


        rs5.close();
        ps5.close();

    }

    catch (Exception e) {

        e.printStackTrace();

        response.setStatus(
                HttpServletResponse
                        .SC_INTERNAL_SERVER_ERROR
        );

        out.print(
                "{"
                + "\"error\":\""
                + escapeJson(e.getMessage())
                + "\""
                + "}"
        );

    }

    finally {

        try {

            if (connection != null) {
                connection.close();
            }

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

}


/* =========================================================
   ESCAPE JSON SPECIAL CHARACTERS
   ========================================================= */

private String escapeJson(String value) {

    if (value == null) {
        return "";
    }

    return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");

}


}
