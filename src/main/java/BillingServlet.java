
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

@WebServlet("/billing")
public class BillingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("list".equals(action)) {

            loadAppointments(request, response);

        } else if ("details".equals(action)) {

            loadAppointmentDetails(request, response);

        } else if ("print".equals(action)) {

            printBill(request, response);

        } else {

            response.sendRedirect("billing.html");
        }
    }


    // =====================================================
    // LOAD COMPLETED APPOINTMENTS
    // =====================================================

    private void loadAppointments(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        PrintWriter out = response.getWriter();

        String sql =
                "SELECT " +
                "a.appointment_id, " +
                "a.appointment_number, " +
                "p.full_name, " +
                "a.treatment_id " +
                "FROM appointments a " +
                "INNER JOIN patients p " +
                "ON a.patient_id = p.patient_id " +
                "WHERE a.status = 'COMPLETED' " +
                "ORDER BY a.appointment_date DESC";

        try {

            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet result =
                    statement.executeQuery();

            out.print("[");

            boolean first = true;

            while (result.next()) {

                if (!first) {
                    out.print(",");
                }

                first = false;

                int appointmentId =
                        result.getInt("appointment_id");

                String appointmentNumber =
                        result.getString(
                                "appointment_number"
                        );

                String patientName =
                        result.getString("full_name");

                int treatmentId =
                        result.getInt("treatment_id");

                out.print("{");

                out.print(
                        "\"appointmentId\":" +
                        appointmentId +
                        ","
                );

                out.print(
                        "\"appointmentNumber\":\"" +
                        escapeJson(appointmentNumber) +
                        "\","
                );

                out.print(
                        "\"patientName\":\"" +
                        escapeJson(patientName) +
                        "\","
                );

                out.print(
                        "\"treatmentName\":\"" +
                        escapeJson(
                                getTreatmentName(treatmentId)
                        ) +
                        "\""
                );

                out.print("}");
            }

            out.print("]");

            result.close();
            statement.close();
            connection.close();

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse
                            .SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"error\":\"" +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // =====================================================
    // LOAD APPOINTMENT DETAILS
    // =====================================================

    private void loadAppointmentDetails(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();

        String id =
                request.getParameter("id");

        if (id == null || id.trim().isEmpty()) {

            out.print(
                    "{\"error\":\"Appointment ID is required\"}"
            );

            return;
        }

        String sql =
                "SELECT " +
                "a.appointment_id, " +
                "a.appointment_number, " +
                "a.patient_id, " +
                "p.full_name, " +
                "a.treatment_id " +
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

                int patientId =
                        result.getInt("patient_id");

                String patientName =
                        result.getString("full_name");

                String appointmentNumber =
                        result.getString(
                                "appointment_number"
                        );

                int treatmentId =
                        result.getInt("treatment_id");

                double amount =
                        getTreatmentAmount(treatmentId);

                out.print("{");

                out.print(
                        "\"patientId\":" +
                        patientId +
                        ","
                );

                out.print(
                        "\"patientName\":\"" +
                        escapeJson(patientName) +
                        "\","
                );

                out.print(
                        "\"appointmentNumber\":\"" +
                        escapeJson(
                                appointmentNumber
                        ) +
                        "\","
                );

                out.print(
                        "\"treatmentId\":" +
                        treatmentId +
                        ","
                );

                out.print(
                        "\"treatmentName\":\"" +
                        escapeJson(
                                getTreatmentName(
                                        treatmentId
                                )
                        ) +
                        "\","
                );

                out.print(
                        "\"amount\":" +
                        amount
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

            response.setStatus(
                    HttpServletResponse
                            .SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                    "{\"error\":\"" +
                    escapeJson(e.getMessage()) +
                    "\"}"
            );
        }
    }


    // =====================================================
    // POST - GENERATE BILL
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String appointmentId =
                request.getParameter(
                        "appointmentId"
                );

        String patientId =
                request.getParameter(
                        "patientId"
                );

        String treatmentId =
                request.getParameter(
                        "treatmentId"
                );

        String amount =
                request.getParameter(
                        "amount"
                );

        String paymentStatus =
                request.getParameter(
                        "paymentStatus"
                );


        // =================================================
        // VALIDATION
        // =================================================

        if (appointmentId == null ||
            patientId == null ||
            treatmentId == null ||
            amount == null ||
            paymentStatus == null ||
            appointmentId.trim().isEmpty() ||
            patientId.trim().isEmpty() ||
            treatmentId.trim().isEmpty() ||
            amount.trim().isEmpty()) {

            response.setContentType(
                    "text/html;charset=UTF-8"
            );

            PrintWriter out =
                    response.getWriter();

            out.println(
                    "<h2>Invalid bill details.</h2>"
            );

            out.println(
                    "<a href='billing.html'>Back to Billing</a>"
            );

            return;
        }


        String sql =
                "INSERT INTO bills " +
                "(appointment_id, patient_id, " +
                "treatment_id, amount, " +
                "payment_status, bill_date) " +
                "VALUES (?, ?, ?, ?, ?, CURDATE())";


        try {

            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(
                            sql,
                            PreparedStatement
                                    .RETURN_GENERATED_KEYS
                    );


            statement.setInt(
                    1,
                    Integer.parseInt(appointmentId)
            );

            statement.setInt(
                    2,
                    Integer.parseInt(patientId)
            );

            statement.setInt(
                    3,
                    Integer.parseInt(treatmentId)
            );

            statement.setDouble(
                    4,
                    Double.parseDouble(amount)
            );

            statement.setString(
                    5,
                    paymentStatus
            );


            int rows =
                    statement.executeUpdate();


            if (rows > 0) {

                int billId = 0;

                ResultSet keys =
                        statement.getGeneratedKeys();

                if (keys.next()) {

                    billId =
                            keys.getInt(1);
                }

                keys.close();
                statement.close();
                connection.close();


                // =========================================
                // OPEN PRINTABLE BILL
                // =========================================

                printBillPage(
                        response,
                        billId,
                        appointmentId,
                        patientId,
                        treatmentId,
                        amount,
                        paymentStatus
                );


            } else {

                statement.close();
                connection.close();

                response.setContentType(
                        "text/html;charset=UTF-8"
                );

                PrintWriter out =
                        response.getWriter();

                out.println(
                        "<h2>Bill was not generated.</h2>"
                );

                out.println(
                        "<a href='billing.html'>" +
                        "Back to Billing" +
                        "</a>"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType(
                    "text/html;charset=UTF-8"
            );

            PrintWriter out =
                    response.getWriter();

            out.println(
                    "<h2>Database Error</h2>"
            );

            out.println(
                    "<p>" +
                    escapeHtml(e.getMessage()) +
                    "</p>"
            );

            out.println(
                    "<a href='billing.html'>" +
                    "Back to Billing" +
                    "</a>"
            );
        }
    }


    // =====================================================
    // PRINT BILL PAGE
    // =====================================================

    private void printBillPage(
            HttpServletResponse response,
            int billId,
            String appointmentId,
            String patientId,
            String treatmentId,
            String amount,
            String paymentStatus)
            throws IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();


        String patientName = "";
        String appointmentNumber = "";
        String treatmentName = "";


        // ================================================
        // GET BILL DETAILS
        // ================================================

        String sql =
                "SELECT " +
                "a.appointment_number, " +
                "p.full_name, " +
                "a.treatment_id " +
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
                    Integer.parseInt(appointmentId)
            );

            ResultSet result =
                    statement.executeQuery();

            if (result.next()) {

                appointmentNumber =
                        result.getString(
                                "appointment_number"
                        );

                patientName =
                        result.getString(
                                "full_name"
                        );

                int treatment =
                        result.getInt(
                                "treatment_id"
                        );

                treatmentName =
                        getTreatmentName(treatment);
            }

            result.close();
            statement.close();
            connection.close();


        } catch (Exception e) {

            e.printStackTrace();
        }


        // ================================================
        // PRINTABLE HTML
        // ================================================

        out.println("<!DOCTYPE html>");

        out.println("<html>");

        out.println("<head>");

        out.println(
                "<meta charset='UTF-8'>"
        );

        out.println(
                "<meta name='viewport' " +
                "content='width=device-width, " +
                "initial-scale=1.0'>"
        );

        out.println(
                "<title>Sunrise Dental Clinic - Bill</title>"
        );


        out.println("<style>");

        out.println(
                "body{" +
                "font-family:Arial,Helvetica,sans-serif;" +
                "background:#f4f8fb;" +
                "margin:0;" +
                "padding:40px;" +
                "}"
        );

        out.println(
                ".bill{" +
                "background:white;" +
                "width:700px;" +
                "max-width:100%;" +
                "margin:auto;" +
                "padding:40px;" +
                "border-radius:12px;" +
                "box-shadow:0 5px 20px " +
                "rgba(0,0,0,0.12);" +
                "}"
        );

        out.println(
                ".header{" +
                "text-align:center;" +
                "border-bottom:2px solid #075985;" +
                "padding-bottom:20px;" +
                "margin-bottom:25px;" +
                "}"
        );

        out.println(
                ".header h1{" +
                "color:#075985;" +
                "margin:0;" +
                "}"
        );

        out.println(
                ".header p{" +
                "color:#666;" +
                "margin:6px 0;" +
                "}"
        );

        out.println(
                ".bill-title{" +
                "text-align:center;" +
                "font-size:24px;" +
                "font-weight:bold;" +
                "margin:20px 0;" +
                "color:#333;" +
                "}"
        );

        out.println(
                ".row{" +
                "display:flex;" +
                "justify-content:space-between;" +
                "padding:12px 0;" +
                "border-bottom:1px solid #eee;" +
                "}"
        );

        out.println(
                ".label{" +
                "font-weight:bold;" +
                "color:#555;" +
                "}"
        );

        out.println(
                ".total{" +
                "display:flex;" +
                "justify-content:space-between;" +
                "font-size:24px;" +
                "font-weight:bold;" +
                "color:#075985;" +
                "padding:20px 0;" +
                "}"
        );

        out.println(
                ".status{" +
                "text-align:center;" +
                "font-weight:bold;" +
                "margin:15px 0;" +
                "}"
        );

        out.println(
                ".buttons{" +
                "text-align:center;" +
                "margin-top:30px;" +
                "}"
        );

        out.println(
                "button,a{" +
                "display:inline-block;" +
                "padding:12px 22px;" +
                "margin:5px;" +
                "border:none;" +
                "border-radius:7px;" +
                "text-decoration:none;" +
                "font-weight:bold;" +
                "cursor:pointer;" +
                "font-size:15px;" +
                "}"
        );

        out.println(
                ".print-btn{" +
                "background:#087ea4;" +
                "color:white;" +
                "}"
        );

        out.println(
                ".back-btn{" +
                "background:#6b7280;" +
                "color:white;" +
                "}"
        );

        out.println(
                "@media print{" +
                "body{" +
                "background:white;" +
                "padding:0;" +
                "}" +
                ".bill{" +
                "width:100%;" +
                "box-shadow:none;" +
                "padding:20px;" +
                "}" +
                ".buttons{" +
                "display:none;" +
                "}" +
                "}"
        );

        out.println("</style>");

        out.println("</head>");

        out.println("<body>");


        // ================================================
        // BILL
        // ================================================

        out.println(
                "<div class='bill'>"
        );


        out.println(
                "<div class='header'>"
        );

        out.println(
                "<h1>🦷 Sunrise Dental Clinic</h1>"
        );

        out.println(
                "<p>Dental Care & Treatment</p>"
        );

        out.println(
                "<p>Colombo, Sri Lanka</p>"
        );

        out.println("</div>");


        out.println(
                "<div class='bill-title'>" +
                "OFFICIAL BILL" +
                "</div>"
        );


        out.println(
                "<div class='row'>" +
                "<span class='label'>Bill ID</span>" +
                "<span>" + billId + "</span>" +
                "</div>"
        );


        out.println(
                "<div class='row'>" +
                "<span class='label'>Appointment No</span>" +
                "<span>" +
                escapeHtml(appointmentNumber) +
                "</span>" +
                "</div>"
        );


        out.println(
                "<div class='row'>" +
                "<span class='label'>Patient Name</span>" +
                "<span>" +
                escapeHtml(patientName) +
                "</span>" +
                "</div>"
        );


        out.println(
                "<div class='row'>" +
                "<span class='label'>Treatment</span>" +
                "<span>" +
                escapeHtml(treatmentName) +
                "</span>" +
                "</div>"
        );


        out.println(
                "<div class='row'>" +
                "<span class='label'>Payment Status</span>" +
                "<span>" +
                escapeHtml(paymentStatus) +
                "</span>" +
                "</div>"
        );


        out.println(
                "<div class='total'>" +
                "<span>Total Amount</span>" +
                "<span>Rs. " +
                amount +
                "</span>" +
                "</div>"
        );


        out.println(
                "<div class='status'>" +
                "Thank you for choosing Sunrise Dental Clinic!" +
                "</div>"
        );


        // ================================================
        // BUTTONS
        // ================================================

        out.println(
                "<div class='buttons'>"
        );


        out.println(
                "<button " +
                "class='print-btn' " +
                "onclick='window.print()'>" +
                "🖨 Print Bill" +
                "</button>"
        );


        out.println(
                "<a " +
                "class='back-btn' " +
                "href='billing.html'>" +
                "Back to Billing" +
                "</a>"
        );


        out.println(
                "<a " +
                "class='back-btn' " +
                "href='receptionist-dashboard.html'>" +
                "Dashboard" +
                "</a>"
        );


        out.println(
                "</div>"
        );


        out.println("</div>");

        out.println("</body>");

        out.println("</html>");
    }


    // =====================================================
    // PRINT EXISTING BILL
    // =====================================================

    private void printBill(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String billId =
                request.getParameter("billId");

        if (billId == null ||
            billId.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Bill ID is required"
            );

            return;
        }


        String sql =
                "SELECT " +
                "b.bill_id, " +
                "b.appointment_id, " +
                "b.amount, " +
                "b.payment_status, " +
                "a.appointment_number, " +
                "p.full_name, " +
                "a.treatment_id " +
                "FROM bills b " +
                "INNER JOIN appointments a " +
                "ON b.appointment_id = a.appointment_id " +
                "INNER JOIN patients p " +
                "ON b.patient_id = p.patient_id " +
                "WHERE b.bill_id = ?";


        try {

            Connection connection =
                    DBConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            statement.setInt(
                    1,
                    Integer.parseInt(billId)
            );

            ResultSet result =
                    statement.executeQuery();


            if (result.next()) {

                String appointmentId =
                        String.valueOf(
                                result.getInt(
                                        "appointment_id"
                                )
                        );

                String amount =
                        String.valueOf(
                                result.getDouble("amount")
                        );

                String paymentStatus =
                        result.getString(
                                "payment_status"
                        );

                String patientId =
                        "0";

                String treatmentId =
                        String.valueOf(
                                result.getInt(
                                        "treatment_id"
                                )
                        );


                result.close();
                statement.close();
                connection.close();


                printBillPage(
                        response,
                        Integer.parseInt(billId),
                        appointmentId,
                        patientId,
                        treatmentId,
                        amount,
                        paymentStatus
                );


            } else {

                result.close();
                statement.close();
                connection.close();

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Bill not found"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Database error: " +
                    e.getMessage()
            );
        }
    }


    // =====================================================
    // TREATMENT NAME
    // =====================================================

    private String getTreatmentName(
            int treatmentId) {

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


    // =====================================================
    // TREATMENT AMOUNT
    // =====================================================

    private double getTreatmentAmount(
            int treatmentId) {

        switch (treatmentId) {

            case 1:
                return 2500.00;

            case 2:
                return 5000.00;

            case 3:
                return 7500.00;

            case 4:
                return 10000.00;

            case 5:
                return 25000.00;

            case 6:
                return 20000.00;

            default:
                return 0.00;
        }
    }


    // =====================================================
    // JSON ESCAPE
    // =====================================================

    private String escapeJson(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }


    // =====================================================
    // HTML ESCAPE
    // =====================================================

    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

