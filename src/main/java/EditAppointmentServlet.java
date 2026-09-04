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

@WebServlet("/edit-appointment")
public class EditAppointmentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType(
                "application/json;charset=UTF-8"
        );


        PrintWriter out =
                response.getWriter();


        String id =
                request.getParameter("id");


        if (id == null || id.isEmpty()) {

            out.print(
                "{\"error\":\"Appointment ID not found\"}"
            );

            return;
        }


        String sql =
                "SELECT " +
                "a.appointment_id, " +
                "a.appointment_number, " +
                "p.full_name, " +
                "a.dentist_name, " +
                "a.treatment_id, " +
                "a.appointment_date, " +
                "a.appointment_time " +
                "FROM appointments a " +
                "INNER JOIN patients p " +
                "ON a.patient_id = p.patient_id " +
                "WHERE a.appointment_id = ?";


        try {

            Connection connection =
                    DBConnection.getConnection();


            PreparedStatement statement =
                    connection.prepareStatement(sql);


            statement.setInt(
                    1,
                    Integer.parseInt(id)
            );


            ResultSet result =
                    statement.executeQuery();


            if (result.next()) {


                int appointmentId =
                        result.getInt(
                                "appointment_id"
                        );


                String appointmentNumber =
                        result.getString(
                                "appointment_number"
                        );


                String patientName =
                        result.getString(
                                "full_name"
                        );


                String dentistName =
                        result.getString(
                                "dentist_name"
                        );


                int treatmentId =
                        result.getInt(
                                "treatment_id"
                        );


                String appointmentDate =
                        result.getString(
                                "appointment_date"
                        );


                String appointmentTime =
                        result.getString(
                                "appointment_time"
                        );


                out.print("{");

                out.print(
                    "\"appointmentId\":" +
                    appointmentId +
                    ","
                );


                out.print(
                    "\"appointmentNumber\":\"" +
                    appointmentNumber +
                    "\","
                );


                out.print(
                    "\"patientName\":\"" +
                    patientName +
                    "\","
                );


                out.print(
                    "\"dentistName\":\"" +
                    dentistName +
                    "\","
                );


                out.print(
                    "\"treatmentId\":" +
                    treatmentId +
                    ","
                );


                out.print(
                    "\"appointmentDate\":\"" +
                    appointmentDate +
                    "\","
                );


                out.print(
                    "\"appointmentTime\":\"" +
                    appointmentTime +
                    "\""
                );


                out.print("}");

            } else {

                out.print(
                    "{\"error\":\"Appointment not found\"}"
                );

            }


            result.close();

            statement.close();

            connection.close();


        } catch (Exception e) {

            e.printStackTrace();

            out.print(
                "{\"error\":\"" +
                e.getMessage() +
                "\"}"
            );

        }

    }


    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {


        String appointmentId =
                request.getParameter(
                        "appointmentId"
                );


        String dentistName =
                request.getParameter(
                        "dentistName"
                );


        String treatmentId =
                request.getParameter(
                        "treatmentId"
                );


        String appointmentDate =
                request.getParameter(
                        "appointmentDate"
                );


        String appointmentTime =
                request.getParameter(
                        "appointmentTime"
                );


        String sql =
                "UPDATE appointments SET " +
                "dentist_name = ?, " +
                "treatment_id = ?, " +
                "appointment_date = ?, " +
                "appointment_time = ? " +
                "WHERE appointment_id = ?";


        try {

            Connection connection =
                    DBConnection.getConnection();


            PreparedStatement statement =
                    connection.prepareStatement(sql);


            statement.setString(
                    1,
                    dentistName
            );


            statement.setInt(
                    2,
                    Integer.parseInt(treatmentId)
            );


            statement.setString(
                    3,
                    appointmentDate
            );


            statement.setString(
                    4,
                    appointmentTime
            );


            statement.setInt(
                    5,
                    Integer.parseInt(appointmentId)
            );


            int result =
                    statement.executeUpdate();


            statement.close();

            connection.close();


            if (result > 0) {

                response.sendRedirect(
                    "appointments.html"
                );

            } else {

                response.setContentType(
                    "text/html;charset=UTF-8"
                );

                response.getWriter().println(
                    "<h2>Appointment was not updated.</h2>"
                );

            }


        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType(
                "text/html;charset=UTF-8"
            );

            response.getWriter().println(
                "<h2>Database Error</h2>"
            );

            response.getWriter().println(
                "<p>" +
                e.getMessage() +
                "</p>"
            );

        }

    }

}
