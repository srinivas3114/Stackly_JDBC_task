import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class EmployeeLeaveManagement {

    static Scanner sc = new Scanner(System.in);

    // ================= EMPLOYEE REGISTRATION =================

    static void registerEmployee() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.println("\n===== Employee Registration =====");

            System.out.print("Employee Name: ");
            String name = sc.nextLine();

            System.out.print("Department: ");
            String department = sc.nextLine();

            System.out.print("Email: ");
            String email = sc.nextLine();

            System.out.print("Phone: ");
            String phone = sc.nextLine();

            String sql = "INSERT INTO employees " +
                    "(employee_name, department, email, phone) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, name);
            ps.setString(2, department);
            ps.setString(3, email);
            ps.setString(4, phone);

            int result = ps.executeUpdate();

            if (result > 0) {

                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {

                    int employeeId = rs.getInt(1);

                    String balanceSql =
                            "INSERT INTO leave_balance " +
                            "(employee_id, total_leave, used_leave, remaining_leave) " +
                            "VALUES (?, 20, 0, 20)";

                    PreparedStatement balancePs =
                            con.prepareStatement(balanceSql);

                    balancePs.setInt(1, employeeId);

                    balancePs.executeUpdate();

                    System.out.println(
                            "Employee Registered Successfully!"
                    );

                    System.out.println(
                            "Employee ID: " + employeeId
                    );
                }
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error: " + e);
        }
    }


    // ================= VIEW EMPLOYEES =================

    static void viewEmployees() {

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM employees";

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n===== Employee List =====");

            while (rs.next()) {

                System.out.println(
                        "ID: " + rs.getInt("employee_id")
                );

                System.out.println(
                        "Name: " + rs.getString("employee_name")
                );

                System.out.println(
                        "Department: " + rs.getString("department")
                );

                System.out.println(
                        "Email: " + rs.getString("email")
                );

                System.out.println(
                        "Phone: " + rs.getString("phone")
                );

                System.out.println("----------------------------");
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error: " + e);
        }
    }


    // ================= APPLY LEAVE =================

    static void applyLeave() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.println("\n===== Apply Leave =====");

            System.out.print("Employee ID: ");
            int employeeId = Integer.parseInt(sc.nextLine());

            // Check employee
            String checkSql =
                    "SELECT employee_id FROM employees WHERE employee_id = ?";

            PreparedStatement checkPs =
                    con.prepareStatement(checkSql);

            checkPs.setInt(1, employeeId);

            ResultSet checkRs = checkPs.executeQuery();

            if (!checkRs.next()) {

                System.out.println("Employee not found.");

                con.close();

                return;
            }

            System.out.print("Leave Type (Casual/Sick/Earned): ");
            String leaveType = sc.nextLine();

            System.out.print("Start Date (YYYY-MM-DD): ");
            LocalDate startDate =
                    LocalDate.parse(sc.nextLine());

            System.out.print("End Date (YYYY-MM-DD): ");
            LocalDate endDate =
                    LocalDate.parse(sc.nextLine());

            if (endDate.isBefore(startDate)) {

                System.out.println(
                        "End date cannot be before start date."
                );

                con.close();

                return;
            }

            int days = (int) ChronoUnit.DAYS.between(
                    startDate,
                    endDate
            ) + 1;

            System.out.println("Number of Leave Days: " + days);

            System.out.print("Reason: ");
            String reason = sc.nextLine();

            // Check leave balance
            String balanceSql =
                    "SELECT remaining_leave FROM leave_balance " +
                    "WHERE employee_id = ?";

            PreparedStatement balancePs =
                    con.prepareStatement(balanceSql);

            balancePs.setInt(1, employeeId);

            ResultSet balanceRs =
                    balancePs.executeQuery();

            if (balanceRs.next()) {

                int remaining =
                        balanceRs.getInt("remaining_leave");

                if (days > remaining) {

                    System.out.println(
                            "Insufficient leave balance."
                    );

                    con.close();

                    return;
                }

            } else {

                System.out.println(
                        "Leave balance not found."
                );

                con.close();

                return;
            }

            String sql =
                    "INSERT INTO leave_requests " +
                    "(employee_id, leave_type, start_date, " +
                    "end_date, days, reason, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'Pending')";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, employeeId);
            ps.setString(2, leaveType);
            ps.setDate(3, Date.valueOf(startDate));
            ps.setDate(4, Date.valueOf(endDate));
            ps.setInt(5, days);
            ps.setString(6, reason);

            ps.executeUpdate();

            System.out.println(
                    "Leave Applied Successfully!"
            );

            con.close();

        } catch (Exception e) {

            System.out.println("Error: " + e);
        }
    }


    // ================= VIEW LEAVE REQUESTS =================

    static void viewLeaveRequests() {

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT lr.leave_id, e.employee_name, " +
                    "lr.leave_type, lr.start_date, lr.end_date, " +
                    "lr.days, lr.reason, lr.status " +
                    "FROM leave_requests lr " +
                    "JOIN employees e " +
                    "ON lr.employee_id = e.employee_id";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n===== Leave Requests =====");

            while (rs.next()) {

                System.out.println(
                        "Leave ID: " +
                        rs.getInt("leave_id")
                );

                System.out.println(
                        "Employee: " +
                        rs.getString("employee_name")
                );

                System.out.println(
                        "Leave Type: " +
                        rs.getString("leave_type")
                );

                System.out.println(
                        "Start Date: " +
                        rs.getDate("start_date")
                );

                System.out.println(
                        "End Date: " +
                        rs.getDate("end_date")
                );

                System.out.println(
                        "Days: " +
                        rs.getInt("days")
                );

                System.out.println(
                        "Reason: " +
                        rs.getString("reason")
                );

                System.out.println(
                        "Status: " +
                        rs.getString("status")
                );

                System.out.println("----------------------------");
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error: " + e);
        }
    }


    // ================= APPROVE / REJECT LEAVE =================

    static void updateLeaveStatus() {

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            System.out.println(
                    "\n===== Approve / Reject Leave ====="
            );

            System.out.print("Leave ID: ");

            int leaveId =
                    Integer.parseInt(sc.nextLine());

            System.out.print(
                    "Enter Status (Approved/Rejected): "
            );

            String newStatus =
                    sc.nextLine();

            if (!newStatus.equalsIgnoreCase("Approved")
                    && !newStatus.equalsIgnoreCase("Rejected")) {

                System.out.println(
                        "Invalid status."
                );

                return;
            }

            con.setAutoCommit(false);

            // Get leave request
            String selectSql =
                    "SELECT employee_id, days, status " +
                    "FROM leave_requests " +
                    "WHERE leave_id = ? FOR UPDATE";

            PreparedStatement selectPs =
                    con.prepareStatement(selectSql);

            selectPs.setInt(1, leaveId);

            ResultSet rs =
                    selectPs.executeQuery();

            if (!rs.next()) {

                System.out.println(
                        "Leave request not found."
                );

                con.rollback();

                return;
            }

            int employeeId =
                    rs.getInt("employee_id");

            int days =
                    rs.getInt("days");

            String oldStatus =
                    rs.getString("status");

            // Prevent changing already processed leave
            if (!oldStatus.equalsIgnoreCase("Pending")) {

                System.out.println(
                        "This leave is already " +
                        oldStatus + "."
                );

                con.rollback();

                return;
            }

            // Update leave status
            String updateSql =
                    "UPDATE leave_requests " +
                    "SET status = ? " +
                    "WHERE leave_id = ?";

            PreparedStatement updatePs =
                    con.prepareStatement(updateSql);

            updatePs.setString(1, newStatus);
            updatePs.setInt(2, leaveId);

            updatePs.executeUpdate();

            // If approved, update balance
            if (newStatus.equalsIgnoreCase("Approved")) {

                String balanceSql =
                        "UPDATE leave_balance " +
                        "SET used_leave = used_leave + ?, " +
                        "remaining_leave = remaining_leave - ? " +
                        "WHERE employee_id = ? " +
                        "AND remaining_leave >= ?";

                PreparedStatement balancePs =
                        con.prepareStatement(balanceSql);

                balancePs.setInt(1, days);
                balancePs.setInt(2, days);
                balancePs.setInt(3, employeeId);
                balancePs.setInt(4, days);

                int updated =
                        balancePs.executeUpdate();

                if (updated == 0) {

                    con.rollback();

                    System.out.println(
                            "Insufficient leave balance."
                    );

                    return;
                }
            }

            con.commit();

            System.out.println(
                    "Leave " + newStatus +
                    " successfully."
            );

        } catch (Exception e) {

            try {

                if (con != null) {
                    con.rollback();
                }

            } catch (SQLException ex) {

                System.out.println(ex);
            }

            System.out.println(
                    "Transaction Failed: " + e
            );

        } finally {

            try {

                if (con != null) {
                    con.close();
                }

            } catch (SQLException e) {

                System.out.println(e);
            }
        }
    }


    // ================= VIEW LEAVE BALANCE =================

    static void viewLeaveBalance() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.println(
                    "\n===== Leave Balance ====="
            );

            System.out.print("Employee ID: ");

            int employeeId =
                    Integer.parseInt(sc.nextLine());

            String sql =
                    "SELECT e.employee_name, " +
                    "lb.total_leave, " +
                    "lb.used_leave, " +
                    "lb.remaining_leave " +
                    "FROM employees e " +
                    "JOIN leave_balance lb " +
                    "ON e.employee_id = lb.employee_id " +
                    "WHERE e.employee_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, employeeId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                System.out.println(
                        "Employee Name: " +
                        rs.getString("employee_name")
                );

                System.out.println(
                        "Total Leave: " +
                        rs.getInt("total_leave")
                );

                System.out.println(
                        "Used Leave: " +
                        rs.getInt("used_leave")
                );

                System.out.println(
                        "Remaining Leave: " +
                        rs.getInt("remaining_leave")
                );

            } else {

                System.out.println(
                        "Employee not found."
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error: " + e);
        }
    }


    // ================= MAIN METHOD =================

    public static void main(String[] args) {

        while (true) {

            System.out.println(
                    "\n===================================="
            );

            System.out.println(
                    "   EMPLOYEE LEAVE MANAGEMENT SYSTEM"
            );

            System.out.println(
                    "===================================="
            );

            System.out.println("1. Employee Registration");
            System.out.println("2. View Employees");
            System.out.println("3. Apply Leave");
            System.out.println("4. View Leave Requests");
            System.out.println("5. Approve / Reject Leave");
            System.out.println("6. View Leave Balance");
            System.out.println("7. Exit");

            System.out.print("Enter Choice: ");

            String input = sc.nextLine();

            int choice;

            try {

                choice = Integer.parseInt(input);

            } catch (Exception e) {

                System.out.println(
                        "Please enter a valid number."
                );

                continue;
            }

            switch (choice) {

                case 1:
                    registerEmployee();
                    break;

                case 2:
                    viewEmployees();
                    break;

                case 3:
                    applyLeave();
                    break;

                case 4:
                    viewLeaveRequests();
                    break;

                case 5:
                    updateLeaveStatus();
                    break;

                case 6:
                    viewLeaveBalance();
                    break;

                case 7:

                    System.out.println(
                            "Thank you for using the system."
                    );

                    sc.close();

                    System.exit(0);

                default:

                    System.out.println(
                            "Invalid Choice."
                    );
            }
        }
    }
}