
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

@WebServlet("/dentists")
public class DentistServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ==========================================
    // GET - VIEW ALL DENTISTS
    // ==========================================

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {

            Connection connection = DBConnection.getConnection();

            String sql =
                    "SELECT dentist_id, dentist_code, dentist_name, " +
                    "specialization, contact_number, status " +
                    "FROM dentists ORDER BY dentist_id";

            PreparedStatement ps =
                    connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            out.print("[");

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    out.print(",");
                }

                out.print("{");

                out.print(
                        "\"dentist_id\":" +
                        rs.getInt("dentist_id") + ","
                );

                out.print(
                        "\"dentist_code\":\"" +
                        escapeJson(rs.getString("dentist_code")) +
                        "\","
                );

                out.print(
                        "\"dentist_name\":\"" +
                        escapeJson(rs.getString("dentist_name")) +
                        "\","
                );

                out.print(
                        "\"specialization\":\"" +
                        escapeJson(rs.getString("specialization")) +
                        "\","
                );

                out.print(
                        "\"contact_number\":\"" +
                        escapeJson(rs.getString("contact_number")) +
                        "\","
                );

                out.print(
                        "\"status\":\"" +
                        escapeJson(rs.getString("status")) +
                        "\""
                );

                out.print("}");

                first = false;
            }

            out.print("]");

            rs.close();
            ps.close();
            connection.close();

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"success\":false," +
                    "\"message\":\"Database error: " +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // ==========================================
    // POST - ADD / EDIT / DEACTIVATE
    // ==========================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // Get action
        String action = request.getParameter("action");

        // Debug
        System.out.println(
                "================================="
        );

        System.out.println(
                "DentistServlet POST request received"
        );

        System.out.println(
                "Action received: [" + action + "]"
        );

        System.out.println(
                "================================="
        );


        // ==========================================
        // ACTION MISSING
        // ==========================================

        if (action == null || action.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false," +
                    "\"message\":\"Action is missing. Please send action=add.\"}"
            );

            return;
        }


        // Remove unnecessary spaces
        action = action.trim();


        // ==========================================
        // ADD
        // ==========================================

        if ("add".equalsIgnoreCase(action)) {

            addDentist(
                    request,
                    response,
                    out
            );

            return;
        }


        // ==========================================
        // EDIT
        // ==========================================

        if ("edit".equalsIgnoreCase(action)) {

            editDentist(
                    request,
                    response,
                    out
            );

            return;
        }


        // ==========================================
        // DEACTIVATE
        // ==========================================

        if ("deactivate".equalsIgnoreCase(action)) {

            deactivateDentist(
                    request,
                    response,
                    out
            );

            return;
        }


        // ==========================================
        // INVALID ACTION
        // ==========================================

        response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
        );

        out.print(
                "{\"success\":false," +
                "\"message\":\"Invalid action. Received: " +
                escapeJson(action) +
                "\"}"
        );
    }


    // ==========================================
    // ADD DENTIST
    // ==========================================

    private void addDentist(HttpServletRequest request,
                            HttpServletResponse response,
                            PrintWriter out) {

        String code =
                request.getParameter("dentist_code");

        String name =
                request.getParameter("dentist_name");

        String specialization =
                request.getParameter("specialization");

        String contact =
                request.getParameter("contact_number");


        // ==========================================
        // VALIDATION
        // ==========================================

        if (code == null ||
            name == null ||
            code.trim().isEmpty() ||
            name.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false," +
                    "\"message\":\"Dentist code and name are required.\"}"
            );

            return;
        }


        try {

            Connection connection =
                    DBConnection.getConnection();


            // ==========================================
            // CHECK DUPLICATE CODE
            // ==========================================

            String checkSql =
                    "SELECT dentist_id " +
                    "FROM dentists " +
                    "WHERE dentist_code = ?";

            PreparedStatement checkPs =
                    connection.prepareStatement(checkSql);

            checkPs.setString(
                    1,
                    code.trim()
            );

            ResultSet checkRs =
                    checkPs.executeQuery();


            if (checkRs.next()) {

                checkRs.close();
                checkPs.close();
                connection.close();

                response.setStatus(
                        HttpServletResponse.SC_CONFLICT
                );

                out.print(
                        "{\"success\":false," +
                        "\"message\":\"Dentist code already exists.\"}"
                );

                return;
            }


            checkRs.close();
            checkPs.close();


            // ==========================================
            // INSERT DENTIST
            // ==========================================

            String sql =
                    "INSERT INTO dentists " +
                    "(dentist_code, dentist_name, " +
                    "specialization, contact_number, status) " +
                    "VALUES (?, ?, ?, ?, 'ACTIVE')";


            PreparedStatement ps =
                    connection.prepareStatement(sql);


            ps.setString(
                    1,
                    code.trim()
            );

            ps.setString(
                    2,
                    name.trim()
            );

            ps.setString(
                    3,
                    specialization == null
                            ? ""
                            : specialization.trim()
            );

            ps.setString(
                    4,
                    contact == null
                            ? ""
                            : contact.trim()
            );


            int rows =
                    ps.executeUpdate();


            ps.close();
            connection.close();


            // ==========================================
            // SUCCESS
            // ==========================================

            if (rows > 0) {

                out.print(
                        "{\"success\":true," +
                        "\"message\":\"Dentist added successfully.\"}"
                );

            } else {

                out.print(
                        "{\"success\":false," +
                        "\"message\":\"Dentist was not added.\"}"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"success\":false," +
                    "\"message\":\"Database error: " +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // ==========================================
    // EDIT DENTIST
    // ==========================================

    private void editDentist(HttpServletRequest request,
                             HttpServletResponse response,
                             PrintWriter out) {

        String id =
                request.getParameter("dentist_id");

        String code =
                request.getParameter("dentist_code");

        String name =
                request.getParameter("dentist_name");

        String specialization =
                request.getParameter("specialization");

        String contact =
                request.getParameter("contact_number");

        String status =
                request.getParameter("status");


        // Validation

        if (id == null ||
            code == null ||
            name == null ||
            code.trim().isEmpty() ||
            name.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false," +
                    "\"message\":\"Required fields are missing.\"}"
            );

            return;
        }


        try {

            Connection connection =
                    DBConnection.getConnection();


            String sql =
                    "UPDATE dentists SET " +
                    "dentist_code = ?, " +
                    "dentist_name = ?, " +
                    "specialization = ?, " +
                    "contact_number = ?, " +
                    "status = ? " +
                    "WHERE dentist_id = ?";


            PreparedStatement ps =
                    connection.prepareStatement(sql);


            ps.setString(
                    1,
                    code.trim()
            );

            ps.setString(
                    2,
                    name.trim()
            );

            ps.setString(
                    3,
                    specialization == null
                            ? ""
                            : specialization.trim()
            );

            ps.setString(
                    4,
                    contact == null
                            ? ""
                            : contact.trim()
            );

            ps.setString(
                    5,
                    status == null
                            ? "ACTIVE"
                            : status
            );

            ps.setInt(
                    6,
                    Integer.parseInt(id)
            );


            int rows =
                    ps.executeUpdate();


            ps.close();
            connection.close();


            if (rows > 0) {

                out.print(
                        "{\"success\":true," +
                        "\"message\":\"Dentist updated successfully.\"}"
                );

            } else {

                out.print(
                        "{\"success\":false," +
                        "\"message\":\"Dentist not found.\"}"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"success\":false," +
                    "\"message\":\"Database error: " +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // ==========================================
    // DEACTIVATE DENTIST
    // ==========================================

    private void deactivateDentist(HttpServletRequest request,
                                   HttpServletResponse response,
                                   PrintWriter out) {

        String id =
                request.getParameter("dentist_id");


        if (id == null ||
            id.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false," +
                    "\"message\":\"Dentist ID is required.\"}"
            );

            return;
        }


        try {

            Connection connection =
                    DBConnection.getConnection();


            String sql =
                    "UPDATE dentists " +
                    "SET status = 'INACTIVE' " +
                    "WHERE dentist_id = ?";


            PreparedStatement ps =
                    connection.prepareStatement(sql);


            ps.setInt(
                    1,
                    Integer.parseInt(id)
            );


            int rows =
                    ps.executeUpdate();


            ps.close();
            connection.close();


            if (rows > 0) {

                out.print(
                        "{\"success\":true," +
                        "\"message\":\"Dentist deactivated successfully.\"}"
                );

            } else {

                out.print(
                        "{\"success\":false," +
                        "\"message\":\"Dentist not found.\"}"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"success\":false," +
                    "\"message\":\"Database error: " +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // ==========================================
    // JSON ESCAPE
    // ==========================================

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

