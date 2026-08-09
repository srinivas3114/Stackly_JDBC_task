import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class ComplaintManagement {

    static Scanner sc = new Scanner(System.in);

    // ==========================================
    // REGISTER USER
    // ==========================================

    static void registerUser() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("User Name : ");
            String name = sc.nextLine();

            System.out.print("Phone : ");
            String phone = sc.nextLine();

            System.out.print("Email : ");
            String email = sc.nextLine();

            String sql =
                    "INSERT INTO users(user_name, phone, email) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);

            ps.executeUpdate();

            System.out.println(
                    "User Registered Successfully!"
            );

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // VIEW USERS
    // ==========================================

    static void viewUsers() {

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM users";

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n========== USERS ==========");

            while (rs.next()) {

                System.out.println(
                        "ID : " + rs.getInt("user_id")
                        + " | Name : " + rs.getString("user_name")
                        + " | Phone : " + rs.getString("phone")
                        + " | Email : " + rs.getString("email")
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // ADD OFFICER
    // ==========================================

    static void addOfficer() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Officer Name : ");
            String name = sc.nextLine();

            System.out.print("Department : ");
            String department = sc.nextLine();

            System.out.print("Phone : ");
            String phone = sc.nextLine();

            String sql =
                    "INSERT INTO officers " +
                    "(officer_name, department, phone) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, department);
            ps.setString(3, phone);

            ps.executeUpdate();

            System.out.println(
                    "Officer Added Successfully!"
            );

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // VIEW OFFICERS
    // ==========================================

    static void viewOfficers() {

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM officers";

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n========== OFFICERS ==========");

            while (rs.next()) {

                System.out.println(
                        "ID : " + rs.getInt("officer_id")
                        + " | Name : "
                        + rs.getString("officer_name")
                        + " | Department : "
                        + rs.getString("department")
                        + " | Phone : "
                        + rs.getString("phone")
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // REGISTER COMPLAINT
    // ==========================================

    static void registerComplaint() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("User ID : ");
            int userId = sc.nextInt();

            sc.nextLine();

            System.out.print("Complaint : ");
            String complaint = sc.nextLine();

            LocalDate date = LocalDate.now();

            String sql =
                    "INSERT INTO complaints " +
                    "(user_id, complaint_text, complaint_date, status) " +
                    "VALUES (?, ?, ?, 'Pending')";

            PreparedStatement ps =
                    con.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            ps.setInt(1, userId);

            ps.setString(2, complaint);

            ps.setDate(
                    3,
                    Date.valueOf(date)
            );

            ps.executeUpdate();

            ResultSet rs =
                    ps.getGeneratedKeys();

            if (rs.next()) {

                System.out.println(
                        "Complaint Registered Successfully!"
                );

                System.out.println(
                        "Complaint ID : " + rs.getInt(1)
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // VIEW ALL COMPLAINTS
    // ==========================================

    static void viewComplaints() {

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT c.complaint_id, " +
                    "u.user_name, " +
                    "o.officer_name, " +
                    "c.complaint_text, " +
                    "c.complaint_date, " +
                    "c.status, " +
                    "c.resolution " +
                    "FROM complaints c " +
                    "JOIN users u ON c.user_id = u.user_id " +
                    "LEFT JOIN officers o " +
                    "ON c.officer_id = o.officer_id";

            Statement st =
                    con.createStatement();

            ResultSet rs =
                    st.executeQuery(sql);

            System.out.println(
                    "\n========== COMPLAINTS =========="
            );

            while (rs.next()) {

                System.out.println(
                        "\nComplaint ID : "
                        + rs.getInt("complaint_id")
                );

                System.out.println(
                        "User : "
                        + rs.getString("user_name")
                );

                System.out.println(
                        "Officer : "
                        + rs.getString("officer_name")
                );

                System.out.println(
                        "Complaint : "
                        + rs.getString("complaint_text")
                );

                System.out.println(
                        "Date : "
                        + rs.getDate("complaint_date")
                );

                System.out.println(
                        "Status : "
                        + rs.getString("status")
                );

                System.out.println(
                        "Resolution : "
                        + rs.getString("resolution")
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // ASSIGN OFFICER
    // ==========================================

    static void assignOfficer() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Complaint ID : ");
            int complaintId = sc.nextInt();

            System.out.print("Officer ID : ");
            int officerId = sc.nextInt();

            String sql =
                    "UPDATE complaints " +
                    "SET officer_id = ?, " +
                    "status = 'Assigned' " +
                    "WHERE complaint_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, officerId);

            ps.setInt(2, complaintId);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Officer Assigned Successfully!"
                );

            } else {

                System.out.println(
                        "Complaint Not Found!"
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // UPDATE STATUS
    // ==========================================

    static void updateStatus() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Complaint ID : ");
            int complaintId = sc.nextInt();

            sc.nextLine();

            System.out.print(
                    "Enter Status " +
                    "(Pending/In Progress/Resolved/Rejected) : "
            );

            String status = sc.nextLine();

            String sql =
                    "UPDATE complaints " +
                    "SET status = ? " +
                    "WHERE complaint_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setString(1, status);

            ps.setInt(2, complaintId);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Complaint Status Updated Successfully!"
                );

            } else {

                System.out.println(
                        "Complaint Not Found!"
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // ADD RESOLUTION
    // ==========================================

    static void addResolution() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Complaint ID : ");
            int complaintId = sc.nextInt();

            sc.nextLine();

            System.out.print("Enter Resolution : ");
            String resolution = sc.nextLine();

            String sql =
                    "UPDATE complaints " +
                    "SET resolution = ?, " +
                    "status = 'Resolved' " +
                    "WHERE complaint_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setString(1, resolution);

            ps.setInt(2, complaintId);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Resolution Added Successfully!"
                );

            } else {

                System.out.println(
                        "Complaint Not Found!"
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // VIEW RESOLUTION
    // ==========================================

    static void viewResolution() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Complaint ID : ");
            int complaintId = sc.nextInt();

            String sql =
                    "SELECT c.complaint_id, " +
                    "u.user_name, " +
                    "c.status, " +
                    "c.resolution " +
                    "FROM complaints c " +
                    "JOIN users u " +
                    "ON c.user_id = u.user_id " +
                    "WHERE c.complaint_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, complaintId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                System.out.println(
                        "\n========== COMPLAINT RESOLUTION =========="
                );

                System.out.println(
                        "Complaint ID : "
                        + rs.getInt("complaint_id")
                );

                System.out.println(
                        "User : "
                        + rs.getString("user_name")
                );

                System.out.println(
                        "Status : "
                        + rs.getString("status")
                );

                System.out.println(
                        "Resolution : "
                        + rs.getString("resolution")
                );

            } else {

                System.out.println(
                        "Complaint Not Found!"
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // DELETE COMPLAINT
    // ==========================================

    static void deleteComplaint() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Complaint ID : ");
            int complaintId = sc.nextInt();

            String sql =
                    "DELETE FROM complaints " +
                    "WHERE complaint_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, complaintId);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Complaint Deleted Successfully!"
                );

            } else {

                System.out.println(
                        "Complaint Not Found!"
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // MAIN MENU
    // ==========================================

    public static void main(String[] args) {

        while (true) {

            System.out.println(
                    "\n======================================"
            );

            System.out.println(
                    "     COMPLAINT MANAGEMENT SYSTEM"
            );

            System.out.println(
                    "======================================"
            );

            System.out.println("1. Register User");
            System.out.println("2. View Users");

            System.out.println("3. Add Officer");
            System.out.println("4. View Officers");

            System.out.println("5. Register Complaint");
            System.out.println("6. View Complaints");

            System.out.println("7. Assign Officer");
            System.out.println("8. Update Status");

            System.out.println("9. Add Resolution");
            System.out.println("10. View Resolution");

            System.out.println("11. Delete Complaint");

            System.out.println("12. Exit");

            System.out.print("Enter Choice : ");

            int choice = sc.nextInt();

            sc.nextLine();

            switch (choice) {

                case 1:
                    registerUser();
                    break;

                case 2:
                    viewUsers();
                    break;

                case 3:
                    addOfficer();
                    break;

                case 4:
                    viewOfficers();
                    break;

                case 5:
                    registerComplaint();
                    break;

                case 6:
                    viewComplaints();
                    break;

                case 7:
                    assignOfficer();
                    break;

                case 8:
                    updateStatus();
                    break;

                case 9:
                    addResolution();
                    break;

                case 10:
                    viewResolution();
                    break;

                case 11:
                    deleteComplaint();
                    break;

                case 12:

                    System.out.println(
                            "Thank You!"
                    );

                    System.exit(0);

                    break;

                default:

                    System.out.println(
                            "Invalid Choice!"
                    );
            }
        }
    }
}