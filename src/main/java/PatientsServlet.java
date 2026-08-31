
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

@WebServlet("/patients")
public class PatientsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;


    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {


        response.setContentType(
                "text/html;charset=UTF-8"
        );


        PrintWriter out =
                response.getWriter();


        String sql =
                "SELECT " +
                "patient_id, " +
                "full_name, " +
                "phone, " +
                "email, " +
                "address, " +
                "date_of_birth " +
                "FROM patients " +
                "ORDER BY patient_id DESC";


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


                int patientId =
                        result.getInt("patient_id");


                String fullName =
                        result.getString("full_name");


                String phone =
                        result.getString("phone");


                String email =
                        result.getString("email");


                String address =
                        result.getString("address");


                String dateOfBirth =
                        result.getString("date_of_birth");


                // Handle NULL values

                if (phone == null) {
                    phone = "-";
                }

                if (email == null) {
                    email = "-";
                }

                if (address == null) {
                    address = "-";
                }

                if (dateOfBirth == null) {
                    dateOfBirth = "-";
                }


                out.println("<tr>");


                out.println(
                    "<td><strong>" +
                    patientId +
                    "</strong></td>"
                );


                out.println(
                    "<td>" +
                    fullName +
                    "</td>"
                );


                out.println(
                    "<td>" +
                    phone +
                    "</td>"
                );


                out.println(
                    "<td>" +
                    email +
                    "</td>"
                );


                out.println(
                    "<td>" +
                    address +
                    "</td>"
                );


                out.println(
                    "<td>" +
                    dateOfBirth +
                    "</td>"
                );


                out.println("</tr>");

            }


            if (!hasData) {

                out.println(
                    "<tr>" +
                    "<td colspan='6' class='empty'>" +
                    "No patients found." +
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
                "<td colspan='6' class='empty'>" +
                "Database Error: " +
                e.getMessage() +
                "</td>" +
                "</tr>"
            );

        }

    }

}