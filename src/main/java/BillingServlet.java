
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

@WebServlet("/billing")
public class BillingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action =
                request.getParameter("action");

        if ("list".equals(action)) {

            loadAppointments(response);

        } else if ("details".equals(action)) {

            loadAppointmentDetails(
                    request,
                    response
            );

        } else if ("print".equals(action)) {

            printBill(
                    request,
                    response
            );

        } else {

            response.sendRedirect(
                    "billing.html"
            );
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();


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


        Connection connection = null;


        try {

            connection =
                    DBConnection.getConnection();


            String sql =
                    "INSERT INTO bills " +
                    "(appointment_id, patient_id, treatment_id, " +
                    "amount, payment_status, bill_date) " +
                    "VALUES (?, ?, ?, ?, ?, NOW())";


            int billId;


            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 sql,
                                 Statement.RETURN_GENERATED_KEYS
                         )) {

                statement.setInt(
                        1,
                        Integer.parseInt(
                                appointmentId
                        )
                );

                statement.setInt(
                        2,
                        Integer.parseInt(
                                patientId
                        )
                );

                statement.setInt(
                        3,
                        Integer.parseInt(
                                treatmentId
                        )
                );

                statement.setDouble(
                        4,
                        Double.parseDouble(
                                amount
                        )
                );

                statement.setString(
                        5,
                        paymentStatus
                );


                statement.executeUpdate();


                try (ResultSet keys =
                             statement.getGeneratedKeys()) {

                    if (keys.next()) {

                        billId =
                                keys.getInt(1);

                    } else {

                        throw new Exception(
                                "Bill ID could not be generated."
                        );
                    }
                }
            }


            printBillPage(
                    response,
                    billId
            );


        } catch (Exception e) {

            e.printStackTrace();


            out.println(
                    "<h2>Database Error</h2>"
            );


            out.println(
                    "<p>" +
                    escapeHtml(
                            e.getMessage()
                    ) +
                    "</p>"
            );


        } finally {

            try {

                if (connection != null) {
                    connection.close();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }


    /* =====================================================
       LOAD COMPLETED APPOINTMENTS
       ===================================================== */

    private void loadAppointments(
            HttpServletResponse response)
            throws IOException {

        response.setContentType(
                "application/json;charset=UTF-8"
        );


        PrintWriter out =
                response.getWriter();


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


        StringBuilder json =
                new StringBuilder("[");


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {

            boolean first = true;


            while (result.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;


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


                int treatmentId =
                        result.getInt(
                                "treatment_id"
                        );


                String treatmentName =
                        getTreatmentName(
                                treatmentId
                        );


                json.append("{");


                json.append(
                        "\"appointmentId\":" +
                        appointmentId +
                        ","
                );


                json.append(
                        "\"appointmentNumber\":\"" +
                        escapeJson(
                                appointmentNumber
                        ) +
                        "\","
                );


                json.append(
                        "\"patientName\":\"" +
                        escapeJson(
                                patientName
                        ) +
                        "\","
                );


                json.append(
                        "\"treatmentName\":\"" +
                        escapeJson(
                                treatmentName
                        ) +
                        "\""
                );


                json.append("}");
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        json.append("]");


        out.print(
                json.toString()
        );
    }


    /* =====================================================
       LOAD APPOINTMENT DETAILS
       ===================================================== */

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


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    Integer.parseInt(id)
            );


            try (ResultSet result =
                         statement.executeQuery()) {

                if (result.next()) {

                    String appointmentNumber =
                            result.getString(
                                    "appointment_number"
                            );


                    String patientName =
                            result.getString(
                                    "full_name"
                            );


                    int patientId =
                            result.getInt(
                                    "patient_id"
                            );


                    int treatmentId =
                            result.getInt(
                                    "treatment_id"
                            );


                    String treatmentName =
                            getTreatmentName(
                                    treatmentId
                            );


                    double amount =
                            getTreatmentAmount(
                                    treatmentId
                            );


                    out.print("{");


                    out.print(
                            "\"appointmentId\":" +
                            result.getInt(
                                    "appointment_id"
                            ) +
                            ","
                    );


                    out.print(
                            "\"appointmentNumber\":\"" +
                            escapeJson(
                                    appointmentNumber
                            ) +
                            "\","
                    );


                    out.print(
                            "\"patientId\":" +
                            patientId +
                            ","
                    );


                    out.print(
                            "\"patientName\":\"" +
                            escapeJson(
                                    patientName
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
                                    treatmentName
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
            }


        } catch (Exception e) {

            e.printStackTrace();


            out.print(
                    "{\"error\":\"Unable to load appointment details\"}"
            );
        }
    }


    /* =====================================================
       PRINT EXISTING BILL
       ===================================================== */

    private void printBill(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String billId =
                request.getParameter(
                        "billId"
                );


        if (billId == null ||
                billId.trim().isEmpty()) {

            response.sendRedirect(
                    "billing.html"
            );

            return;
        }


        printBillPage(
                response,
                Integer.parseInt(billId)
        );
    }


    /* =====================================================
       THERMAL RECEIPT
       ===================================================== */

    private void printBillPage(
            HttpServletResponse response,
            int billId)
            throws IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );


        PrintWriter out =
                response.getWriter();


        String sql =
                "SELECT " +
                "b.bill_id, " +
                "b.amount, " +
                "b.payment_status, " +
                "b.bill_date, " +
                "a.appointment_number, " +
                "a.patient_id, " +
                "p.full_name, " +
                "a.treatment_id " +
                "FROM bills b " +
                "INNER JOIN appointments a " +
                "ON b.appointment_id = a.appointment_id " +
                "INNER JOIN patients p " +
                "ON b.patient_id = p.patient_id " +
                "WHERE b.bill_id = ?";


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    billId
            );


            try (ResultSet result =
                         statement.executeQuery()) {

                if (!result.next()) {

                    out.println(
                            "<h2>Bill not found.</h2>"
                    );

                    return;
                }


                double amount =
                        result.getDouble(
                                "amount"
                        );


                String paymentStatus =
                        result.getString(
                                "payment_status"
                        );


                String appointmentNumber =
                        result.getString(
                                "appointment_number"
                        );


                int patientId =
                        result.getInt(
                                "patient_id"
                        );


                String patientName =
                        result.getString(
                                "full_name"
                        );


                int treatmentId =
                        result.getInt(
                                "treatment_id"
                        );


                String treatmentName =
                        getTreatmentName(
                                treatmentId
                        );


                String billDate =
                        result.getString(
                                "bill_date"
                        );


                /* =========================================
                   HTML
                   ========================================= */

                out.println(
                        "<!DOCTYPE html>"
                );

                out.println(
                        "<html lang='en'>"
                );

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
                        "<title>Payment Receipt</title>"
                );


                /* =========================================
                   THERMAL RECEIPT CSS
                   ========================================= */

                out.println("<style>");

                out.println(
                        "* {" +
                        "box-sizing: border-box;" +
                        "}"
                );


                out.println(
                        "html, body {" +
                        "margin: 0;" +
                        "padding: 0;" +
                        "background: #eeeeee;" +
                        "font-family: 'Courier New', monospace;" +
                        "}"
                );


                out.println(
                        "body {" +
                        "display: flex;" +
                        "justify-content: center;" +
                        "padding: 30px 10px;" +
                        "}"
                );


                out.println(
                        ".receipt {" +
                        "width: 80mm;" +
                        "max-width: 80mm;" +
                        "background: white;" +
                        "padding: 10px;" +
                        "color: #000;" +
                        "font-size: 12px;" +
                        "}"
                );


                out.println(
                        ".center {" +
                        "text-align: center;" +
                        "}"
                );


                out.println(
                        ".clinic-name {" +
                        "font-size: 19px;" +
                        "font-weight: bold;" +
                        "color: #0b4f6c;" +
                        "}"
                );


                out.println(
                        ".subtitle {" +
                        "font-size: 11px;" +
                        "margin-top: 3px;" +
                        "}"
                );


                out.println(
                        ".receipt-title {" +
                        "font-size: 15px;" +
                        "font-weight: bold;" +
                        "margin-top: 10px;" +
                        "color: #0b4f6c;" +
                        "}"
                );


                out.println(
                        ".line {" +
                        "border-top: 1px dashed #000;" +
                        "margin: 9px 0;" +
                        "}"
                );


                out.println(
                        ".row {" +
                        "display: flex;" +
                        "justify-content: space-between;" +
                        "gap: 10px;" +
                        "padding: 3px 0;" +
                        "}"
                );


                out.println(
                        ".label {" +
                        "font-weight: bold;" +
                        "}"
                );


                out.println(
                        ".treatment {" +
                        "padding: 7px 0;" +
                        "}"
                );


                out.println(
                        ".treatment-name {" +
                        "font-weight: bold;" +
                        "max-width: 55%;" +
                        "}"
                );


                out.println(
                        ".amount {" +
                        "text-align: right;" +
                        "white-space: nowrap;" +
                        "}"
                );


                out.println(
                        ".total {" +
                        "font-size: 17px;" +
                        "font-weight: bold;" +
                        "padding: 8px 0;" +
                        "color: #0b4f6c;" +
                        "}"
                );


                out.println(
                        ".status {" +
                        "text-align: center;" +
                        "font-weight: bold;" +
                        "font-size: 13px;" +
                        "padding: 6px 0;" +
                        "color: #0b4f6c;" +
                        "}"
                );


                out.println(
                        ".thank-you {" +
                        "text-align: center;" +
                        "font-weight: bold;" +
                        "margin-top: 12px;" +
                        "line-height: 1.5;" +
                        "}"
                );


                /* =========================================
                   BUTTONS - CLINIC THEME
                   ========================================= */

                out.println(
                        ".buttons {" +
                        "margin-top: 20px;" +
                        "display: flex;" +
                        "gap: 8px;" +
                        "font-family: Arial, sans-serif;" +
                        "}"
                );


                out.println(
                        ".buttons button, " +
                        ".buttons a {" +
                        "flex: 1;" +
                        "padding: 10px;" +
                        "border: none;" +
                        "border-radius: 5px;" +
                        "text-align: center;" +
                        "text-decoration: none;" +
                        "cursor: pointer;" +
                        "font-size: 12px;" +
                        "font-weight: bold;" +
                        "}"
                );


                /* DARK BLUE */

                out.println(
                        ".print-btn {" +
                        "background: #0b4f6c;" +
                        "color: white;" +
                        "}"
                );


                /* LIGHT BLUE HOVER */

                out.println(
                        ".print-btn:hover {" +
                        "background: #087ea4;" +
                        "}"
                );


                /* LIGHT BLUE */

                out.println(
                        ".back-btn {" +
                        "background: #087ea4;" +
                        "color: white;" +
                        "}"
                );


                /* DARK BLUE HOVER */

                out.println(
                        ".back-btn:hover {" +
                        "background: #0b4f6c;" +
                        "}"
                );


                /* =========================================
                   PRINT SETTINGS - 80MM
                   ========================================= */

                out.println(
                        "@page {" +
                        "size: 80mm auto;" +
                        "margin: 0;" +
                        "}"
                );


                out.println(
                        "@media print {" +

                        "html, body {" +
                        "background: white;" +
                        "width: 80mm;" +
                        "margin: 0;" +
                        "padding: 0;" +
                        "}" +

                        ".receipt {" +
                        "width: 80mm;" +
                        "max-width: 80mm;" +
                        "padding: 5mm;" +
                        "box-shadow: none;" +
                        "}" +

                        ".buttons {" +
                        "display: none !important;" +
                        "}" +

                        "}"
                );


                out.println("</style>");

                out.println("</head>");


                out.println("<body>");


                out.println(
                        "<div class='receipt'>"
                );


                /* =========================================
                   CLINIC HEADER
                   ========================================= */

                out.println(
                        "<div class='center clinic-name'>" +
                        "SUNRISE DENTAL CLINIC" +
                        "</div>"
                );


                out.println(
                        "<div class='center subtitle'>" +
                        "Dental Care & Treatment" +
                        "</div>"
                );


                out.println(
                        "<div class='center subtitle'>" +
                        "Colombo, Sri Lanka" +
                        "</div>"
                );


                out.println(
                        "<div class='center subtitle'>" +
                        "Tel: 011 234 5678" +
                        "</div>"
                );


                out.println(
                        "<div class='line'></div>"
                );


                /* =========================================
                   RECEIPT TITLE
                   ========================================= */

                out.println(
                        "<div class='center receipt-title'>" +
                        "PAYMENT RECEIPT" +
                        "</div>"
                );


                out.println(
                        "<div class='line'></div>"
                );


                /* =========================================
                   RECEIPT DETAILS
                   ========================================= */

                out.println(
                        "<div class='row'>" +
                        "<span class='label'>Receipt No</span>" +
                        "<span>" +
                        String.format(
                                "%06d",
                                billId
                        ) +
                        "</span>" +
                        "</div>"
                );


                out.println(
                        "<div class='row'>" +
                        "<span class='label'>Date</span>" +
                        "<span>" +
                        escapeHtml(
                                billDate
                        ) +
                        "</span>" +
                        "</div>"
                );


                out.println(
                        "<div class='row'>" +
                        "<span class='label'>Appointment</span>" +
                        "<span>" +
                        escapeHtml(
                                appointmentNumber
                        ) +
                        "</span>" +
                        "</div>"
                );


                out.println(
                        "<div class='row'>" +
                        "<span class='label'>Patient ID</span>" +
                        "<span>" +
                        patientId +
                        "</span>" +
                        "</div>"
                );


                out.println(
                        "<div class='row'>" +
                        "<span class='label'>Patient</span>" +
                        "<span>" +
                        escapeHtml(
                                patientName
                        ) +
                        "</span>" +
                        "</div>"
                );


                out.println(
                        "<div class='line'></div>"
                );


                /* =========================================
                   TREATMENT
                   ========================================= */

                out.println(
                        "<div class='treatment'>"
                );


                out.println(
                        "<div class='row'>" +
                        "<span class='treatment-name'>" +
                        escapeHtml(
                                treatmentName
                        ) +
                        "</span>" +
                        "<span class='amount'>" +
                        "Rs. " +
                        String.format(
                                "%,.2f",
                                amount
                        ) +
                        "</span>" +
                        "</div>"
                );


                out.println("</div>");


                out.println(
                        "<div class='line'></div>"
                );


                /* =========================================
                   TOTAL
                   ========================================= */

                out.println(
                        "<div class='row total'>" +
                        "<span>TOTAL</span>" +
                        "<span>Rs. " +
                        String.format(
                                "%,.2f",
                                amount
                        ) +
                        "</span>" +
                        "</div>"
                );


                out.println(
                        "<div class='line'></div>"
                );


                /* =========================================
                   PAYMENT STATUS
                   ========================================= */

                out.println(
                        "<div class='status'>" +
                        "PAYMENT STATUS: " +
                        escapeHtml(
                                paymentStatus
                        ) +
                        "</div>"
                );


                out.println(
                        "<div class='line'></div>"
                );


                /* =========================================
                   FOOTER
                   ========================================= */

                out.println(
                        "<div class='thank-you'>" +
                        "Thank You!<br>" +
                        "Visit Us Again<br>" +
                        "<br>" +
                        "SUNRISE DENTAL CLINIC" +
                        "</div>"
                );


                out.println(
                        "<div class='line'></div>"
                );


                /* =========================================
                   BUTTONS
                   ========================================= */

                out.println(
                        "<div class='buttons'>"
                );


                out.println(
                        "<button " +
                        "class='print-btn' " +
                        "onclick='window.print()'>" +
                        "Print Receipt" +
                        "</button>"
                );


                out.println(
                        "<a " +
                        "href='billing.html' " +
                        "class='back-btn'>" +
                        "Back" +
                        "</a>"
                );


                out.println("</div>");


                out.println("</div>");

                out.println("</body>");

                out.println("</html>");
            }


        } catch (Exception e) {

            e.printStackTrace();


            response.getWriter().println(
                    "<h2>Error loading receipt</h2>"
            );


            response.getWriter().println(
                    "<p>" +
                    escapeHtml(
                            e.getMessage()
                    ) +
                    "</p>"
            );
        }
    }


    /* =====================================================
       TREATMENT NAME
       ===================================================== */

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


    /* =====================================================
       TREATMENT AMOUNT
       ===================================================== */

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


    /* =====================================================
       JSON ESCAPE
       ===================================================== */

    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }


        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }


    /* =====================================================
       HTML ESCAPE
       ===================================================== */

    private String escapeHtml(
            String value) {

        if (value == null) {
            return "";
        }


        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

