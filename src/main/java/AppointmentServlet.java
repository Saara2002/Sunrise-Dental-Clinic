import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/appointment")
public class AppointmentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        // ==============================
        // GET PATIENT DETAILS
        // ==============================

        String fullName =
                request.getParameter("patientName");

        String phone =
                request.getParameter("contactNumber");

        String email =
                request.getParameter("email");

        String address =
                request.getParameter("address");

        String dateOfBirth =
                request.getParameter("dateOfBirth");


        // ==============================
        // GET APPOINTMENT DETAILS
        // ==============================

        String dentistName =
                request.getParameter("dentistName");

        String treatmentId =
                request.getParameter("treatmentId");

        String appointmentDate =
                request.getParameter("appointmentDate");

        String appointmentTime =
                request.getParameter("appointmentTime");


        response.setContentType(
                "text/html;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();


        Connection connection = null;

        try {

            // ==============================
            // DATABASE CONNECTION
            // ==============================

            connection =
                    DBConnection.getConnection();

            // Start transaction
            connection.setAutoCommit(false);


            // ==============================
            // GENERATE APPOINTMENT NUMBER
            // ==============================

            String numberSQL =
                    "SELECT MAX(" +
                    "CAST(SUBSTRING(appointment_number, 4) " +
                    "AS UNSIGNED)" +
                    ") AS max_number " +
                    "FROM appointments";

            int nextNumber = 1;

            try (PreparedStatement numberStatement =
                         connection.prepareStatement(numberSQL);

                 ResultSet numberResult =
                         numberStatement.executeQuery()) {

                if (numberResult.next()) {

                    int maxNumber =
                            numberResult.getInt("max_number");

                    if (!numberResult.wasNull()) {
                        nextNumber = maxNumber + 1;
                    }
                }
            }

            String appointmentNumber =
                    String.format(
                            "APP%03d",
                            nextNumber
                    );


            // ==============================
            // 1. INSERT PATIENT
            // ==============================

            String patientSQL =
                    "INSERT INTO patients " +
                    "(full_name, phone, email, address, date_of_birth) " +
                    "VALUES (?, ?, ?, ?, ?)";


            int patientId;

            try (PreparedStatement patientStatement =
                         connection.prepareStatement(
                                 patientSQL,
                                 Statement.RETURN_GENERATED_KEYS
                         )) {

                patientStatement.setString(
                        1,
                        fullName
                );

                patientStatement.setString(
                        2,
                        phone
                );

                patientStatement.setString(
                        3,
                        email
                );

                patientStatement.setString(
                        4,
                        address
                );


                if (dateOfBirth == null ||
                        dateOfBirth.trim().isEmpty()) {

                    patientStatement.setNull(
                            5,
                            java.sql.Types.DATE
                    );

                } else {

                    patientStatement.setString(
                            5,
                            dateOfBirth
                    );
                }


                patientStatement.executeUpdate();


                // ==============================
                // GET AUTO GENERATED PATIENT ID
                // ==============================

                try (ResultSet generatedKeys =
                             patientStatement.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        patientId =
                                generatedKeys.getInt(1);

                    } else {

                        throw new Exception(
                                "Patient ID could not be generated."
                        );
                    }
                }
            }


            // ==============================
            // 2. INSERT APPOINTMENT
            // ==============================

            String appointmentSQL =
                    "INSERT INTO appointments " +
                    "(appointment_number, patient_id, " +
                    "dentist_name, treatment_id, " +
                    "appointment_date, appointment_time, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";


            int result;

            try (PreparedStatement appointmentStatement =
                         connection.prepareStatement(
                                 appointmentSQL
                         )) {

                appointmentStatement.setString(
                        1,
                        appointmentNumber
                );

                appointmentStatement.setInt(
                        2,
                        patientId
                );

                appointmentStatement.setString(
                        3,
                        dentistName
                );

                appointmentStatement.setInt(
                        4,
                        Integer.parseInt(treatmentId)
                );

                appointmentStatement.setString(
                        5,
                        appointmentDate
                );

                appointmentStatement.setString(
                        6,
                        appointmentTime
                );

                appointmentStatement.setString(
                        7,
                        "BOOKED"
                );


                result =
                        appointmentStatement.executeUpdate();
            }


            // ==============================
            // COMMIT TRANSACTION
            // ==============================

            if (result > 0) {

                connection.commit();


                // ==============================
                // SUCCESS PAGE
                // ==============================

                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");

                out.println(
                        "<title>Appointment Registered</title>"
                );

                out.println("<style>");

                out.println(
                        "body{" +
                        "font-family:Arial;" +
                        "background:#f4f8fb;" +
                        "text-align:center;" +
                        "padding-top:70px;}"
                );

                out.println(
                        ".box{" +
                        "background:white;" +
                        "width:550px;" +
                        "margin:auto;" +
                        "padding:40px;" +
                        "border-radius:15px;" +
                        "box-shadow:0 5px 20px rgba(0,0,0,0.1);}"
                );

                out.println(
                        "h1{color:#087ea4;}"
                );

                out.println(
                        ".details{" +
                        "text-align:left;" +
                        "line-height:2;" +
                        "margin-top:20px;}"
                );

                out.println(
                        ".button{" +
                        "display:inline-block;" +
                        "margin-top:20px;" +
                        "padding:12px 25px;" +
                        "background:#087ea4;" +
                        "color:white;" +
                        "text-decoration:none;" +
                        "border-radius:7px;}"
                );

                out.println("</style>");

                out.println("</head>");
                out.println("<body>");

                out.println("<div class='box'>");


                out.println(
                        "<h1>✓ Appointment Registered!</h1>"
                );

                out.println(
                        "<p>Appointment saved successfully.</p>"
                );


                out.println(
                        "<div class='details'>"
                );


                out.println(
                        "<strong>Patient ID:</strong> " +
                        patientId
                );

                out.println("<br>");


                out.println(
                        "<strong>Patient Name:</strong> " +
                        fullName
                );

                out.println("<br>");


                out.println(
                        "<strong>Address:</strong> " +
                        address
                );

                out.println("<br>");


                out.println(
                        "<strong>Phone:</strong> " +
                        phone
                );

                out.println("<br>");


                out.println(
                        "<strong>Appointment Number:</strong> " +
                        appointmentNumber
                );

                out.println("<br>");


                out.println(
                        "<strong>Dentist:</strong> " +
                        dentistName
                );

                out.println("<br>");


                out.println(
                        "<strong>Date:</strong> " +
                        appointmentDate
                );

                out.println("<br>");


                out.println(
                        "<strong>Time:</strong> " +
                        appointmentTime
                );

                out.println("<br>");


                out.println(
                        "<strong>Status:</strong> BOOKED"
                );


                out.println("</div>");


                out.println(
                        "<a class='button' " +
                        "href='appointment.html'>" +
                        "Register Another Appointment" +
                        "</a>"
                );


                out.println("<br>");


                out.println(
                        "<a class='button' " +
                        "href='receptionist-dashboard.html'>" +
                        "Dashboard" +
                        "</a>"
                );


                out.println("</div>");

                out.println("</body>");
                out.println("</html>");
            }

        } catch (Exception e) {

            // ==============================
            // ROLLBACK IF ERROR OCCURS
            // ==============================

            try {

                if (connection != null) {
                    connection.rollback();
                }

            } catch (Exception rollbackError) {

                rollbackError.printStackTrace();
            }


            e.printStackTrace();


            out.println(
                    "<h2>Database Error</h2>"
            );

            out.println(
                    "<p>" +
                    e.getMessage() +
                    "</p>"
            );


        } finally {

            try {

                if (connection != null) {
                    connection.close();
                }

            } catch (Exception closeError) {

                closeError.printStackTrace();
            }
        }
    }
}