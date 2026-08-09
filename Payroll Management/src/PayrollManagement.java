import java.sql.*;
import java.util.Scanner;

public class PayrollManagement {

    static Scanner sc = new Scanner(System.in);

    // ================= EMPLOYEE MANAGEMENT =================

    static void addEmployee() {

        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Employee Name : ");
            String name = sc.nextLine();

            System.out.print("Department : ");
            String department = sc.nextLine();

            System.out.print("Designation : ");
            String designation = sc.nextLine();

            System.out.print("Basic Salary : ");
            double salary = sc.nextDouble();
            sc.nextLine();

            String sql = "INSERT INTO employees " +
                    "(emp_name, department, designation, basic_salary) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, department);
            ps.setString(3, designation);
            ps.setDouble(4, salary);

            ps.executeUpdate();

            System.out.println("Employee Added Successfully!");

            con.close();

        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    // ================= VIEW EMPLOYEES =================

    static void viewEmployees() {

        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM employees";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n===== EMPLOYEE DETAILS =====");

            while (rs.next()) {

                System.out.println(
                        "ID : " + rs.getInt("emp_id") +
                        " | Name : " + rs.getString("emp_name") +
                        " | Department : " + rs.getString("department") +
                        " | Designation : " + rs.getString("designation") +
                        " | Salary : " + rs.getDouble("basic_salary")
                );
            }

            con.close();

        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    // ================= SALARY CALCULATION =================

    static void calculateSalary() {

        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter Employee ID : ");
            int empId = sc.nextInt();
            sc.nextLine();

            System.out.print("Salary Month : ");
            String month = sc.nextLine();

            String sql = "SELECT basic_salary FROM employees WHERE emp_id=?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, empId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Employee Not Found!");
                con.close();
                return;
            }

            double basic = rs.getDouble("basic_salary");

            // Allowance = 20% of basic salary
            double allowance = basic * 0.20;

            // Deduction = 10% of basic salary
            double deduction = basic * 0.10;

            double netSalary = basic + allowance - deduction;

            String insert = "INSERT INTO salaries " +
                    "(emp_id, basic_salary, allowance, deduction, net_salary, salary_month) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps2 = con.prepareStatement(insert);

            ps2.setInt(1, empId);
            ps2.setDouble(2, basic);
            ps2.setDouble(3, allowance);
            ps2.setDouble(4, deduction);
            ps2.setDouble(5, netSalary);
            ps2.setString(6, month);

            ps2.executeUpdate();

            System.out.println("\n===== SALARY CALCULATION =====");
            System.out.println("Basic Salary : " + basic);
            System.out.println("Allowance    : " + allowance);
            System.out.println("Deduction    : " + deduction);
            System.out.println("Net Salary   : " + netSalary);

            con.close();

        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    // ================= TAX CALCULATION =================

    static void calculateTax() {

        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter Employee ID : ");
            int empId = sc.nextInt();
            sc.nextLine();

            System.out.print("Payroll Month : ");
            String month = sc.nextLine();

            String sql = "SELECT basic_salary FROM employees WHERE emp_id=?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, empId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Employee Not Found!");
                con.close();
                return;
            }

            double basic = rs.getDouble("basic_salary");

            double allowance = basic * 0.20;

            double grossSalary = basic + allowance;

            double tax;

            // Simple example tax calculation
            if (grossSalary <= 25000) {
                tax = 0;
            } else if (grossSalary <= 50000) {
                tax = grossSalary * 0.05;
            } else if (grossSalary <= 100000) {
                tax = grossSalary * 0.10;
            } else {
                tax = grossSalary * 0.15;
            }

            double netSalary = grossSalary - tax;

            String insert = "INSERT INTO payroll " +
                    "(emp_id, gross_salary, tax, net_salary, payroll_month) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps2 = con.prepareStatement(insert);

            ps2.setInt(1, empId);
            ps2.setDouble(2, grossSalary);
            ps2.setDouble(3, tax);
            ps2.setDouble(4, netSalary);
            ps2.setString(5, month);

            ps2.executeUpdate();

            System.out.println("\n===== TAX DETAILS =====");
            System.out.println("Gross Salary : " + grossSalary);
            System.out.println("Tax          : " + tax);
            System.out.println("Net Salary   : " + netSalary);

            con.close();

        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    // ================= PAYSLIP =================

    static void generatePayslip() {

        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter Employee ID : ");
            int empId = sc.nextInt();

            String sql =
                    "SELECT e.emp_name, e.department, e.designation, " +
                    "p.gross_salary, p.tax, p.net_salary, p.payroll_month " +
                    "FROM employees e " +
                    "JOIN payroll p ON e.emp_id = p.emp_id " +
                    "WHERE e.emp_id = ? " +
                    "ORDER BY p.payroll_id DESC LIMIT 1";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, empId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println("\n================================");
                System.out.println("          PAYSLIP");
                System.out.println("================================");

                System.out.println("Employee Name : "
                        + rs.getString("emp_name"));

                System.out.println("Department    : "
                        + rs.getString("department"));

                System.out.println("Designation   : "
                        + rs.getString("designation"));

                System.out.println("Month         : "
                        + rs.getString("payroll_month"));

                System.out.println("--------------------------------");

                System.out.println("Gross Salary  : "
                        + rs.getDouble("gross_salary"));

                System.out.println("Tax Deduction : "
                        + rs.getDouble("tax"));

                System.out.println("Net Salary    : "
                        + rs.getDouble("net_salary"));

                System.out.println("================================");

            } else {
                System.out.println("Payroll Record Not Found!");
            }

            con.close();

        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    // ================= AGGREGATE FUNCTION =================

    static void salaryReport() {

        try {
            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT COUNT(*) AS total_employees, " +
                    "SUM(gross_salary) AS total_salary, " +
                    "AVG(gross_salary) AS average_salary, " +
                    "MAX(gross_salary) AS highest_salary, " +
                    "MIN(gross_salary) AS lowest_salary " +
                    "FROM payroll";

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {

                System.out.println("\n===== SALARY REPORT =====");

                System.out.println(
                        "Total Employees : "
                        + rs.getInt("total_employees"));

                System.out.println(
                        "Total Salary    : "
                        + rs.getDouble("total_salary"));

                System.out.println(
                        "Average Salary  : "
                        + rs.getDouble("average_salary"));

                System.out.println(
                        "Highest Salary  : "
                        + rs.getDouble("highest_salary"));

                System.out.println(
                        "Lowest Salary   : "
                        + rs.getDouble("lowest_salary"));
            }

            con.close();

        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    // ================= BATCH PROCESSING =================

    static void batchSalaryUpdate() {

        try {
            Connection con = DBConnection.getConnection();

            String sql = "UPDATE employees " +
                    "SET basic_salary = basic_salary + ? " +
                    "WHERE emp_id = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            System.out.print("Enter increment amount : ");
            double increment = sc.nextDouble();

            System.out.print("Enter number of employees : ");
            int n = sc.nextInt();

            for (int i = 1; i <= n; i++) {

                System.out.print("Enter Employee ID " + i + " : ");
                int empId = sc.nextInt();

                ps.setDouble(1, increment);
                ps.setInt(2, empId);

                ps.addBatch();
            }

            int[] result = ps.executeBatch();

            System.out.println(
                    result.length + " employee salaries updated using batch processing."
            );

            con.close();

        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    // ================= MAIN MENU =================

    public static void main(String[] args) {

        while (true) {

            System.out.println("\n==================================");
            System.out.println("     PAYROLL MANAGEMENT SYSTEM");
            System.out.println("==================================");

            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Calculate Salary");
            System.out.println("4. Calculate Tax");
            System.out.println("5. Generate Payslip");
            System.out.println("6. Salary Report");
            System.out.println("7. Batch Salary Update");
            System.out.println("8. Exit");

            System.out.print("Enter Choice : ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    addEmployee();
                    break;

                case 2:
                    viewEmployees();
                    break;

                case 3:
                    calculateSalary();
                    break;

                case 4:
                    calculateTax();
                    break;

                case 5:
                    generatePayslip();
                    break;

                case 6:
                    salaryReport();
                    break;

                case 7:
                    batchSalaryUpdate();
                    break;

                case 8:
                    System.out.println("Thank You!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }
}