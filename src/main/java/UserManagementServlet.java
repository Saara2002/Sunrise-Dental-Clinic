
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

@WebServlet("/user-management")
public class UserManagementServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ==========================================
    // GET - LOAD ALL USERS
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

            String sql = "SELECT user_id, username, role, created_at "
                       + "FROM users ORDER BY user_id";

            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            out.print("[");

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    out.print(",");
                }

                out.print("{");

                out.print("\"user_id\":"
                        + rs.getInt("user_id") + ",");

                out.print("\"username\":\""
                        + rs.getString("username") + "\",");

                out.print("\"role\":\""
                        + rs.getString("role") + "\",");

                out.print("\"created_at\":\""
                        + rs.getTimestamp("created_at") + "\"");

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
                    "{\"success\":false,"
                    + "\"message\":\"Database error: "
                    + e.getMessage()
                    + "\"}"
            );
        }
    }


    // ==========================================
    // POST - ADD / EDIT / DELETE
    // ==========================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");

        if ("add".equals(action)) {

            addUser(request, response, out);
            return;
        }

        if ("edit".equals(action)) {

            editUser(request, response, out);
            return;
        }

        if ("delete".equals(action)) {

            deleteUser(request, response, out);
            return;
        }

        response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
        );

        out.print(
                "{\"success\":false,"
                + "\"message\":\"Invalid action.\"}"
        );
    }


    // ==========================================
    // ADD USER
    // ==========================================

    private void addUser(HttpServletRequest request,
                         HttpServletResponse response,
                         PrintWriter out) {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        if (username == null ||
            password == null ||
            role == null ||
            username.trim().isEmpty() ||
            password.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false,"
                    + "\"message\":\"All fields are required.\"}"
            );

            return;
        }

        if (!role.equals("ADMIN") &&
            !role.equals("RECEPTIONIST")) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false,"
                    + "\"message\":\"Invalid role.\"}"
            );

            return;
        }

        try {

            Connection connection = DBConnection.getConnection();

            // Check duplicate username

            String checkSql =
                    "SELECT user_id FROM users WHERE username = ?";

            PreparedStatement checkPs =
                    connection.prepareStatement(checkSql);

            checkPs.setString(1, username.trim());

            ResultSet checkRs = checkPs.executeQuery();

            if (checkRs.next()) {

                checkRs.close();
                checkPs.close();
                connection.close();

                response.setStatus(
                        HttpServletResponse.SC_CONFLICT
                );

                out.print(
                        "{\"success\":false,"
                        + "\"message\":\"Username already exists.\"}"
                );

                return;
            }

            checkRs.close();
            checkPs.close();

            // Insert user

            String sql =
                    "INSERT INTO users "
                    + "(username, password, role) "
                    + "VALUES (?, ?, ?)";

            PreparedStatement ps =
                    connection.prepareStatement(sql);

            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, role);

            int rows = ps.executeUpdate();

            ps.close();
            connection.close();

            if (rows > 0) {

                out.print(
                        "{\"success\":true,"
                        + "\"message\":\"User added successfully.\"}"
                );

            } else {

                out.print(
                        "{\"success\":false,"
                        + "\"message\":\"User was not added.\"}"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"success\":false,"
                    + "\"message\":\"Database error: "
                    + e.getMessage()
                    + "\"}"
            );
        }
    }


    // ==========================================
    // EDIT USER
    // ==========================================

    private void editUser(HttpServletRequest request,
                          HttpServletResponse response,
                          PrintWriter out) {

        String userId = request.getParameter("user_id");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        if (userId == null ||
            username == null ||
            role == null ||
            username.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false,"
                    + "\"message\":\"Required fields are missing.\"}"
            );

            return;
        }

        if (!role.equals("ADMIN") &&
            !role.equals("RECEPTIONIST")) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false,"
                    + "\"message\":\"Invalid role.\"}"
            );

            return;
        }

        try {

            Connection connection = DBConnection.getConnection();

            // Check duplicate username

            String checkSql =
                    "SELECT user_id FROM users "
                    + "WHERE username = ? "
                    + "AND user_id <> ?";

            PreparedStatement checkPs =
                    connection.prepareStatement(checkSql);

            checkPs.setString(1, username.trim());
            checkPs.setInt(2, Integer.parseInt(userId));

            ResultSet checkRs = checkPs.executeQuery();

            if (checkRs.next()) {

                checkRs.close();
                checkPs.close();
                connection.close();

                response.setStatus(
                        HttpServletResponse.SC_CONFLICT
                );

                out.print(
                        "{\"success\":false,"
                        + "\"message\":\"Username already exists.\"}"
                );

                return;
            }

            checkRs.close();
            checkPs.close();

            int rows;

            // Password empty = keep old password

            if (password == null ||
                password.trim().isEmpty()) {

                String sql =
                        "UPDATE users "
                        + "SET username = ?, role = ? "
                        + "WHERE user_id = ?";

                PreparedStatement ps =
                        connection.prepareStatement(sql);

                ps.setString(1, username.trim());
                ps.setString(2, role);
                ps.setInt(3, Integer.parseInt(userId));

                rows = ps.executeUpdate();

                ps.close();

            } else {

                String sql =
                        "UPDATE users "
                        + "SET username = ?, password = ?, role = ? "
                        + "WHERE user_id = ?";

                PreparedStatement ps =
                        connection.prepareStatement(sql);

                ps.setString(1, username.trim());
                ps.setString(2, password);
                ps.setString(3, role);
                ps.setInt(4, Integer.parseInt(userId));

                rows = ps.executeUpdate();

                ps.close();
            }

            connection.close();

            if (rows > 0) {

                out.print(
                        "{\"success\":true,"
                        + "\"message\":\"User updated successfully.\"}"
                );

            } else {

                out.print(
                        "{\"success\":false,"
                        + "\"message\":\"User not found.\"}"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"success\":false,"
                    + "\"message\":\"Database error: "
                    + e.getMessage()
                    + "\"}"
            );
        }
    }


    // ==========================================
    // DELETE USER
    // ==========================================

    private void deleteUser(HttpServletRequest request,
                            HttpServletResponse response,
                            PrintWriter out) {

        String userId = request.getParameter("user_id");

        if (userId == null ||
            userId.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print(
                    "{\"success\":false,"
                    + "\"message\":\"User ID is required.\"}"
            );

            return;
            
        }
           

        try {

            Connection connection = DBConnection.getConnection();

            String sql =
                    "DELETE FROM users WHERE user_id = ?";

            PreparedStatement ps =
                    connection.prepareStatement(sql);

            ps.setInt(
                    1,
                    Integer.parseInt(userId)
            );

            int rows = ps.executeUpdate();

            ps.close();
            connection.close();

            if (rows > 0) {

                out.print(
                        "{\"success\":true,"
                        + "\"message\":\"User deleted successfully.\"}"
                );

            } else {

                out.print(
                        "{\"success\":false,"
                        + "\"message\":\"User not found.\"}"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"success\":false,"
                    + "\"message\":\"Database error: "
                    + e.getMessage()
                    + "\"}"
            );
        }
    }
}


