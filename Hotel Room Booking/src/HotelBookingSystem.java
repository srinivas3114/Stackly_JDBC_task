import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class HotelBookingSystem {

    static Scanner sc = new Scanner(System.in);

    // ==========================================
    // VIEW AVAILABLE ROOMS
    // ==========================================

    static void viewAvailableRooms() {

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT * FROM rooms " +
                    "WHERE status = 'Available'";

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n========== AVAILABLE ROOMS ==========");

            while (rs.next()) {

                System.out.println(
                        "Room ID : " + rs.getInt("room_id")
                        + " | Room No : "
                        + rs.getString("room_number")
                        + " | Type : "
                        + rs.getString("room_type")
                        + " | Price : "
                        + rs.getDouble("price")
                        + " | Status : "
                        + rs.getString("status")
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // REGISTER CUSTOMER
    // ==========================================

    static void registerCustomer() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Customer Name : ");
            String name = sc.nextLine();

            System.out.print("Phone : ");
            String phone = sc.nextLine();

            System.out.print("Email : ");
            String email = sc.nextLine();

            String sql =
                    "INSERT INTO customers " +
                    "(customer_name, phone, email) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement ps =
                    con.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {

                System.out.println(
                        "Customer Registered Successfully!"
                );

                System.out.println(
                        "Customer ID : " + rs.getInt(1)
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // VIEW CUSTOMERS
    // ==========================================

    static void viewCustomers() {

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM customers";

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n========== CUSTOMERS ==========");

            while (rs.next()) {

                System.out.println(
                        "ID : " + rs.getInt("customer_id")
                        + " | Name : "
                        + rs.getString("customer_name")
                        + " | Phone : "
                        + rs.getString("phone")
                        + " | Email : "
                        + rs.getString("email")
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // BOOK ROOM
    // ==========================================

    static void bookRoom() {

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            // Start transaction
            con.setAutoCommit(false);

            System.out.print("Customer ID : ");
            int customerId = sc.nextInt();

            System.out.print("Room ID : ");
            int roomId = sc.nextInt();

            sc.nextLine();

            System.out.print(
                    "Check-In Date (YYYY-MM-DD) : "
            );

            LocalDate checkIn =
                    LocalDate.parse(sc.nextLine());

            System.out.print(
                    "Check-Out Date (YYYY-MM-DD) : "
            );

            LocalDate checkOut =
                    LocalDate.parse(sc.nextLine());

            // Validate dates

            if (!checkOut.isAfter(checkIn)) {

                System.out.println(
                        "Check-Out date must be after Check-In date!"
                );

                con.rollback();
                con.close();
                return;
            }

            // Check customer

            String customerSql =
                    "SELECT * FROM customers " +
                    "WHERE customer_id = ?";

            PreparedStatement customerPs =
                    con.prepareStatement(customerSql);

            customerPs.setInt(1, customerId);

            ResultSet customerRs =
                    customerPs.executeQuery();

            if (!customerRs.next()) {

                System.out.println(
                        "Customer Not Found!"
                );

                con.rollback();
                con.close();
                return;
            }

            // Check room

            String roomSql =
                    "SELECT * FROM rooms " +
                    "WHERE room_id = ? " +
                    "AND status = 'Available'";

            PreparedStatement roomPs =
                    con.prepareStatement(roomSql);

            roomPs.setInt(1, roomId);

            ResultSet roomRs =
                    roomPs.executeQuery();

            if (!roomRs.next()) {

                System.out.println(
                        "Room is not available!"
                );

                con.rollback();
                con.close();
                return;
            }

            double price =
                    roomRs.getDouble("price");

            // Calculate number of days

            long days =
                    ChronoUnit.DAYS.between(
                            checkIn,
                            checkOut
                    );

            double totalAmount =
                    days * price;

            // Insert booking

            String bookingSql =
                    "INSERT INTO bookings " +
                    "(customer_id, room_id, check_in, " +
                    "check_out, total_amount, status) " +
                    "VALUES (?, ?, ?, ?, ?, 'Booked')";

            PreparedStatement bookingPs =
                    con.prepareStatement(
                            bookingSql,
                            Statement.RETURN_GENERATED_KEYS
                    );

            bookingPs.setInt(1, customerId);

            bookingPs.setInt(2, roomId);

            bookingPs.setDate(
                    3,
                    Date.valueOf(checkIn)
            );

            bookingPs.setDate(
                    4,
                    Date.valueOf(checkOut)
            );

            bookingPs.setDouble(
                    5,
                    totalAmount
            );

            bookingPs.executeUpdate();

            // Update room status

            String updateRoom =
                    "UPDATE rooms " +
                    "SET status = 'Booked' " +
                    "WHERE room_id = ?";

            PreparedStatement updatePs =
                    con.prepareStatement(updateRoom);

            updatePs.setInt(1, roomId);

            updatePs.executeUpdate();

            // Commit transaction

            con.commit();

            ResultSet generated =
                    bookingPs.getGeneratedKeys();

            if (generated.next()) {

                System.out.println(
                        "\nBooking Successful!"
                );

                System.out.println(
                        "Booking ID : "
                        + generated.getInt(1)
                );
            }

            System.out.println(
                    "Number of Days : " + days
            );

            System.out.println(
                    "Total Amount : ₹" + totalAmount
            );

            con.close();

        } catch (Exception e) {

            try {

                if (con != null) {
                    con.rollback();
                }

            } catch (Exception ex) {

                System.out.println(
                        "Rollback Error : " + ex
                );
            }

            System.out.println(
                    "Booking Failed : " + e
            );
        }
    }


    // ==========================================
    // CHECK-IN
    // ==========================================

    static void checkIn() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Booking ID : ");
            int bookingId = sc.nextInt();

            String sql =
                    "UPDATE bookings " +
                    "SET status = 'Checked-In' " +
                    "WHERE booking_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, bookingId);

            int rows =
                    ps.executeUpdate();

            if (rows > 0) {

                System.out.println(
                        "Customer Checked-In Successfully!"
                );

            } else {

                System.out.println(
                        "Booking Not Found!"
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // CHECK-OUT
    // ==========================================

    static void checkOut() {

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            con.setAutoCommit(false);

            System.out.print("Booking ID : ");
            int bookingId = sc.nextInt();

            // Get room ID

            String selectSql =
                    "SELECT room_id, total_amount " +
                    "FROM bookings " +
                    "WHERE booking_id = ?";

            PreparedStatement selectPs =
                    con.prepareStatement(selectSql);

            selectPs.setInt(1, bookingId);

            ResultSet rs =
                    selectPs.executeQuery();

            if (!rs.next()) {

                System.out.println(
                        "Booking Not Found!"
                );

                con.rollback();
                con.close();
                return;
            }

            int roomId =
                    rs.getInt("room_id");

            double amount =
                    rs.getDouble("total_amount");

            // Update booking

            String bookingSql =
                    "UPDATE bookings " +
                    "SET status = 'Checked-Out' " +
                    "WHERE booking_id = ?";

            PreparedStatement bookingPs =
                    con.prepareStatement(bookingSql);

            bookingPs.setInt(1, bookingId);

            bookingPs.executeUpdate();

            // Make room available

            String roomSql =
                    "UPDATE rooms " +
                    "SET status = 'Available' " +
                    "WHERE room_id = ?";

            PreparedStatement roomPs =
                    con.prepareStatement(roomSql);

            roomPs.setInt(1, roomId);

            roomPs.executeUpdate();

            con.commit();

            System.out.println(
                    "\nCheck-Out Successful!"
            );

            System.out.println(
                    "Final Bill : ₹" + amount
            );

            con.close();

        } catch (Exception e) {

            try {

                if (con != null) {
                    con.rollback();
                }

            } catch (Exception ex) {

                System.out.println(
                        "Rollback Error : " + ex
                );
            }

            System.out.println(
                    "Check-Out Failed : " + e
            );
        }
    }


    // ==========================================
    // BILLING
    // ==========================================

    static void generateBill() {

        try {

            Connection con = DBConnection.getConnection();

            System.out.print("Booking ID : ");
            int bookingId = sc.nextInt();

            String sql =
                    "SELECT b.booking_id, " +
                    "c.customer_name, " +
                    "r.room_number, " +
                    "r.room_type, " +
                    "b.check_in, " +
                    "b.check_out, " +
                    "b.total_amount, " +
                    "b.status " +
                    "FROM bookings b " +
                    "JOIN customers c " +
                    "ON b.customer_id = c.customer_id " +
                    "JOIN rooms r " +
                    "ON b.room_id = r.room_id " +
                    "WHERE b.booking_id = ?";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            ps.setInt(1, bookingId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                System.out.println(
                        "\n================================"
                );

                System.out.println(
                        "          HOTEL BILL"
                );

                System.out.println(
                        "================================"
                );

                System.out.println(
                        "Booking ID : "
                        + rs.getInt("booking_id")
                );

                System.out.println(
                        "Customer : "
                        + rs.getString("customer_name")
                );

                System.out.println(
                        "Room Number : "
                        + rs.getString("room_number")
                );

                System.out.println(
                        "Room Type : "
                        + rs.getString("room_type")
                );

                System.out.println(
                        "Check-In : "
                        + rs.getDate("check_in")
                );

                System.out.println(
                        "Check-Out : "
                        + rs.getDate("check_out")
                );

                System.out.println(
                        "Amount : ₹"
                        + rs.getDouble("total_amount")
                );

                System.out.println(
                        "Status : "
                        + rs.getString("status")
                );

                System.out.println(
                        "================================"
                );

            } else {

                System.out.println(
                        "Booking Not Found!"
                );
            }

            con.close();

        } catch (Exception e) {

            System.out.println("Error : " + e);
        }
    }


    // ==========================================
    // VIEW BOOKINGS
    // ==========================================

    static void viewBookings() {

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT b.booking_id, " +
                    "c.customer_name, " +
                    "r.room_number, " +
                    "r.room_type, " +
                    "b.check_in, " +
                    "b.check_out, " +
                    "b.total_amount, " +
                    "b.status " +
                    "FROM bookings b " +
                    "JOIN customers c " +
                    "ON b.customer_id = c.customer_id " +
                    "JOIN rooms r " +
                    "ON b.room_id = r.room_id";

            Statement st =
                    con.createStatement();

            ResultSet rs =
                    st.executeQuery(sql);

            System.out.println(
                    "\n========== BOOKINGS =========="
            );

            while (rs.next()) {

                System.out.println(
                        "\nBooking ID : "
                        + rs.getInt("booking_id")
                );

                System.out.println(
                        "Customer : "
                        + rs.getString("customer_name")
                );

                System.out.println(
                        "Room : "
                        + rs.getString("room_number")
                );

                System.out.println(
                        "Type : "
                        + rs.getString("room_type")
                );

                System.out.println(
                        "Check-In : "
                        + rs.getDate("check_in")
                );

                System.out.println(
                        "Check-Out : "
                        + rs.getDate("check_out")
                );

                System.out.println(
                        "Amount : ₹"
                        + rs.getDouble("total_amount")
                );

                System.out.println(
                        "Status : "
                        + rs.getString("status")
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
                    "\n===================================="
            );

            System.out.println(
                    "      HOTEL ROOM BOOKING SYSTEM"
            );

            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "1. View Available Rooms"
            );

            System.out.println(
                    "2. Register Customer"
            );

            System.out.println(
                    "3. View Customers"
            );

            System.out.println(
                    "4. Book Room"
            );

            System.out.println(
                    "5. View Bookings"
            );

            System.out.println(
                    "6. Check-In"
            );

            System.out.println(
                    "7. Check-Out"
            );

            System.out.println(
                    "8. Generate Bill"
            );

            System.out.println(
                    "9. Exit"
            );

            System.out.print(
                    "Enter Choice : "
            );

            int choice = sc.nextInt();

            sc.nextLine();

            switch (choice) {

                case 1:
                    viewAvailableRooms();
                    break;

                case 2:
                    registerCustomer();
                    break;

                case 3:
                    viewCustomers();
                    break;

                case 4:
                    bookRoom();
                    break;

                case 5:
                    viewBookings();
                    break;

                case 6:
                    checkIn();
                    break;

                case 7:
                    checkOut();
                    break;

                case 8:
                    generateBill();
                    break;

                case 9:

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