import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Sharetea {
    private static Connection conn = null;

    public static void main(String args[]) {
        dbSetup my = new dbSetup();
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_21_db",
                    my.user, my.pswd);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        JFrame mainFrame = new JFrame("Sharetea POS System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 600);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manager Dashboard", new ManagerGUI(conn));
        tabbedPane.addTab("Cashier Dashboard", new CashierGUI(conn));

        mainFrame.add(tabbedPane, BorderLayout.CENTER);
        mainFrame.setVisible(true);

        // Close DB
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (conn != null) {
                    try {
                        conn.close();
                        System.out.println("Database connection closed.");
                    }
                    catch (SQLException ex) {
                        System.out.println("Error closing connection: " + ex.getMessage());
                    }
                }
            }
        });
    }
}