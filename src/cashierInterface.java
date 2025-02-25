import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import java.util.*;

/* TODO (LINE 82): IF AN ORDER IS CONFIRMED, TAKE THE PANEL BACK TO MAIN MENU */

public class cashierInterface {

    private static Connection conn = null;
    private static JPanel mainPanel;
    private static JPanel selectPanel;
    private static JPanel customizePanel;
    private static CardLayout panelSwitcher;
    private static String currentDrink;

    /* Getting column values from database */
    private static ArrayList<String> getColVal(String query, String... params) {
        ArrayList<String> values = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                values.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error.");
        }
        return values;
    }

    /* Getting drink type from database */
    private static ArrayList<String> getDrinkTypesFromDB() {
        return getColVal("SELECT DISTINCT product_type FROM product");
    }

    /* Getting drink name from database */
    private static ArrayList<String> getDrinksByType(String drinkType) {
        return getColVal("SELECT product_name FROM product WHERE product_type = ?", drinkType);
    }

    /* Getting customization options from database */
    private static ArrayList<String[]> getCustomizeOptions() {
        ArrayList<String[]> options = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT item_name, amount FROM inventory WHERE item_id BETWEEN 22 AND 30;");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                options.add(new String[]{rs.getString("item_name"), rs.getString("amount")});
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error.");
        }
        return options;
    }

    /* Setting up buttons for specific drinks */
    private static void showSpecificDrinks(String drinkType) {
        selectPanel.removeAll();

        ArrayList<String> drinks = getDrinksByType(drinkType);
        for (String drink : drinks) {
            JButton drinkButton = new JButton(drink);
            drinkButton.setPreferredSize(new Dimension (150, 50));
            drinkButton.addActionListener(e -> showCustomizeDrink(drink));
            selectPanel.add(drinkButton);
        }
        addBackButton(selectPanel, "General Drinks");
        selectPanel.revalidate();
        selectPanel.repaint();
        panelSwitcher.show(mainPanel, "Specific Drink Selection");
    }

    /* Once drink has been selected, customization (milk & type, sugar & type, ice) */
    /* TODO: if an order is confirmed, take it back to the main menu*/
    private static void showCustomizeDrink(String drink) {
        customizePanel.removeAll();
        currentDrink = drink;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        customizePanel.add(new JLabel("Customize " + drink), gbc);
        gbc.gridy++;

        ArrayList<String[]> customOptions = getCustomizeOptions();
        for (String[] option : customOptions) {
            gbc.gridx = 0;
            customizePanel.add(new JLabel(option[0] + " (Available: " + option[1] + ")"), gbc);
            gbc.gridx = 1;
            customizePanel.add(new JTextField(5), gbc);
            gbc.gridy++;
        }

        JButton confirmButton = new JButton("Confirm Selection");
        confirmButton.addActionListener(e -> JOptionPane.showMessageDialog(null, "Order placed: " + currentDrink));
        gbc.gridx = 0;
        gbc.gridy++;
        customizePanel.add(confirmButton, gbc);

        addBackButton(customizePanel, "Specific Drink Selection");
        customizePanel.revalidate();
        customizePanel.repaint();
        panelSwitcher.show(mainPanel, "Customize Drink");

    }

    private static void addBackButton(JPanel panel, String previousPanel) {
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> panelSwitcher.show(mainPanel, previousPanel));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(backButton, gbc);
    }

    public static void main(String args[]) {
        dbSetup my = new dbSetup();
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/team_21_db",
                    my.user, my.pswd);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

//        JOptionPane.showMessageDialog(null,"Opened database successfully");
//        String cus_lname = "";

        /* Cashier page */
        JFrame cashierDashboard = new JFrame("Cashier Dashboard");
        cashierDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cashierDashboard.setSize(800,600);

        panelSwitcher = new CardLayout();
        mainPanel = new JPanel(panelSwitcher); //mainPanel will now host all the different panels

        JPanel mainInterPanel = new JPanel();
        mainInterPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        /* Drink selection for general types of drink */
        ArrayList<String> drinkTypes = getDrinkTypesFromDB();
        for (String drinks : drinkTypes) {
            JButton drinkButton = new JButton(drinks);
            drinkButton.setPreferredSize(new Dimension(200, 50));
            drinkButton.addActionListener(e -> showSpecificDrinks(drinks));
            mainInterPanel.add(drinkButton);
        }

        mainPanel.add(mainInterPanel, "General Drinks");

        /* Drink selection for specific types of drink */
        selectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        mainPanel.add (selectPanel, "Specific Drink Selection");

        /* Drink customizer after specific drink selection */
        customizePanel = new JPanel(new GridBagLayout());
        mainPanel.add(customizePanel, "Customize Drink");

        /* Main panels */
        cashierDashboard.add(mainPanel);
        cashierDashboard.setVisible(true);

        cashierDashboard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(conn != null){
                    try{
                        conn.close();
                        System.out.println("Database connection closed.");
                    }
                    catch(SQLException ex){
                        System.out.println("Error closing connection: " + ex.getMessage());
                    }
                }
            }
        });
    }

}