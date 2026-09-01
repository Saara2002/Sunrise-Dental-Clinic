\# Sunrise Dental Clinic Management System



\## Project Overview



The Sunrise Dental Clinic Management System is a web-based application developed to computerize the appointment and patient management activities of a private dental clinic.



The system provides authorized staff with facilities to manage appointments, patients, dentists, billing, reports and user accounts through a simple web-based interface.



\## Main Features



\* User authentication and login

\* Receptionist dashboard

\* Admin dashboard

\* Register new appointments

\* Search and display appointment details

\* Edit and manage appointments

\* Manage patients

\* Manage dentists

\* Calculate and generate bills

\* Generate reports

\* User management

\* Help section

\* Appointment status management



\## Technologies Used



\* \*\*Java\*\*

\* \*\*Jakarta Servlets\*\*

\* \*\*HTML5\*\*

\* \*\*CSS3\*\*

\* \*\*JavaScript\*\*

\* \*\*MySQL\*\*

\* \*\*JDBC\*\*

\* \*\*Apache Tomcat\*\*

\* \*\*Maven\*\*

\* \*\*Eclipse IDE\*\*

\* \*\*Git \& GitHub\*\*



\## Appointment Management



The system allows reception staff to register appointments with information such as:



\* Appointment number

\* Patient name

\* Address

\* Contact number

\* Dentist

\* Treatment type

\* Appointment date

\* Appointment time

\* Appointment status



\## Billing



The billing module calculates the patient's bill based on the selected treatment and applicable consultation charges.



\## User Roles



The system supports different user roles, including:



\* \*\*Administrator\*\*

\* \*\*Receptionist\*\*



Administrators can manage system users and other administrative functions, while receptionists can manage appointments, patients and billing activities.



\## Database



The application uses \*\*MySQL\*\* as the database management system. Java Database Connectivity (JDBC) is used to connect the application with the database.



\## Project Structure



```text

Sunrise-Dental-Clinic

│

├── pom.xml

├── .gitignore

│

└── src

&#x20;   └── main

&#x20;       ├── java

&#x20;       │   ├── LoginServlet.java

&#x20;       │   ├── AppointmentServlet.java

&#x20;       │   ├── AppointmentsServlet.java

&#x20;       │   ├── AppointmentActionServlet.java

&#x20;       │   ├── EditAppointmentServlet.java

&#x20;       │   ├── BillingServlet.java

&#x20;       │   ├── DentistServlet.java

&#x20;       │   ├── PatientsServlet.java

&#x20;       │   ├── UserManagementServlet.java

&#x20;       │   ├── ReportsServlet.java

&#x20;       │   └── DBConnection.java

&#x20;       │

&#x20;       └── webapp

&#x20;           ├── login.html

&#x20;           ├── receptionist-dashboard.html

&#x20;           ├── admin-dashboard.html

&#x20;           ├── appointment.html

&#x20;           ├── appointments.html

&#x20;           ├── billing.html

&#x20;           ├── dentists.html

&#x20;           ├── patients.html

&#x20;           ├── reports.html

&#x20;           ├── user-management.html

&#x20;           ├── edit-appointment.html

&#x20;           └── help.html

```



\## How to Run the Project



1\. Clone the repository.

2\. Import the project into Eclipse as a Maven project.

3\. Configure the MySQL database.

4\. Update the database connection details in `DBConnection.java`.

5\. Configure Apache Tomcat.

6\. Run the application on the Tomcat server.

7\. Open the application through a web browser.

8\. Login using an authorized user account.



\## Version Control



Git and GitHub are used for source code management and version control. Project development is maintained through Git commits so that changes can be tracked throughout the development process.



\## Author



\*\*Sunrise Dental Clinic Management System\*\*



University Academic Project



