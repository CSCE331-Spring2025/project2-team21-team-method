import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
//import java.sql.DriverManager;
/*
CSCE 315
9-25-2019
 */
public class jdbcpostgreSQLGUI {
  //Building the connection
  private static Connection conn= null; 
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
     }//end try catch
     JOptionPane.showMessageDialog(null,"Opened database successfully");
     String cus_lname = "";

    /*
      Below section is for creating the manager dashboard 
     */

    JFrame managerDashboard = new JFrame("Manager Dashboard");

    managerDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    managerDashboard.setSize(600,400);

    JTabbedPane tabsPane = new JTabbedPane();

    JPanel orderPanel = new JPanel();
    tabsPane.addTab("Order Page",orderPanel);
  
    JPanel trackingPanel = new JPanel();
    tabsPane.addTab("Tracking Page",trackingPanel);
    

    JPanel trendsPanel = new JPanel();
    tabsPane.addTab("Trends Page",trendsPanel);

      JPanel employeePanel = new JPanel();
      tabsPane.addTab("Employee Page",trendsPanel);




    
    managerDashboard.add(tabsPane,BorderLayout.CENTER);
    managerDashboard.setVisible(true);
     try{
     
     // building the order panel 
      buildOrderPanel(conn,orderPanel);
      buildEmployeePanel(conn,employeePanel);

   } catch (Exception e){
     JOptionPane.showMessageDialog(null,"Error accessing Database.");
   }

    //closing the connection
    managerDashboard.addWindowListener(new WindowAdapter(){
      @Override
      public void windowClosing(WindowEvent e){
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
  }//end main

  private static void buildOrderPanel(Connection conn, JPanel orderPanel) {
      DefaultTableModel inventoryTableModel = new DefaultTableModel(new String [] {"Item ID","Item Name", "Amount Left"},0);
      JTable inventoryTable = new JTable(inventoryTableModel);
      JScrollPane inventoryTableScrollPane = new JScrollPane(inventoryTable);

      buildInventoryTable(conn, inventoryTableModel);


      // creating an area wehre the manager can add update and delete items 

      JPanel modifyItemsPanel = new JPanel(new FlowLayout());
      JTextField id = new JTextField(3);
      JTextField name = new JTextField(20);
      JTextField amount = new JTextField(5);
      JTextField transactionId = new JTextField(20);

      modifyItemsPanel.add(new JLabel("Item ID"));
      modifyItemsPanel.add(id);
      
      modifyItemsPanel.add(new JLabel("Item name"));
      modifyItemsPanel.add(name);
      
      modifyItemsPanel.add(new JLabel("Item amount"));
      modifyItemsPanel.add(amount);
      
      modifyItemsPanel.add(new JLabel("Transaction Number"));
      modifyItemsPanel.add(transactionId);
      

      JButton addButton = new JButton("ADD");
      addButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e){
          String temp_id = id.getText().trim();
          String temp_name = name.getText().trim();
          String temp_amount = amount.getText().trim();
          String temp_transactionId= transactionId.getText().trim();
          if (temp_id.isEmpty() || temp_name.isEmpty() || temp_amount.isEmpty() || temp_transactionId.isEmpty()  ) {
            JOptionPane.showMessageDialog(null, "Please fill all fields (Id, Name, Amount, Transaction ID).");
            return;
          } 
          int num_id = Integer.parseInt(temp_id);
          int num_amount = Integer.parseInt(temp_amount);
          int num_transactionId = Integer.parseInt(temp_transactionId);
          
          // should I add checks for empty and value checking ?

          insertValueIntoDatabase(conn,num_id,temp_name,num_amount,num_transactionId);
          buildInventoryTable(conn, inventoryTableModel);
        }
      });
      modifyItemsPanel.add(addButton);

      JButton updateButton = new JButton("UPDATE");
      updateButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e){
          String temp_id = id.getText().trim();
          String temp_name = name.getText().trim();
          String temp_amount = amount.getText().trim();
          String temp_transactionId= transactionId.getText().trim();
          if (temp_id.isEmpty()){
            JOptionPane.showMessageDialog(null, "Please the ID field.");
            return;
          }
          int num_id = Integer.parseInt(temp_id);
          int num_amount= -1;
          int num_transactionId=-1;
          if(!temp_amount.isEmpty()){
             num_amount = Integer.parseInt(temp_amount);
          }
          if(!temp_transactionId.isEmpty()){
           num_transactionId = Integer.parseInt(temp_transactionId);
          }
          // should I add checks for empty and value checking ?
          updateValueIntoDatabase(conn,num_id,temp_name,num_amount,num_transactionId);
          buildInventoryTable(conn, inventoryTableModel);
          //buildInventoryTable(conn, inventoryTableModel);
        }
      });
      modifyItemsPanel.add(updateButton);


      JButton deleteButton = new JButton("DELETE");
      deleteButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e){
          String temp_id = id.getText().trim();
          String temp_name = name.getText().trim();
          String temp_amount = amount.getText().trim();
          String temp_transactionId= transactionId.getText().trim();
          if (temp_id.isEmpty()){
            JOptionPane.showMessageDialog(null, "Please the ID field.");
            return;
          }
          int num_id = Integer.parseInt(temp_id);
          int num_amount;
          int num_transactionId;
          if(!temp_amount.isEmpty()){
             num_amount = Integer.parseInt(temp_amount);
          }
          if(!temp_transactionId.isEmpty()){
           num_transactionId = Integer.parseInt(temp_transactionId);
          }
          
          // should I add checks for empty and value checking ?
          deleteValueIntoDatabase(conn,num_id);
          buildInventoryTable(conn, inventoryTableModel);
        }
      });
      modifyItemsPanel.add(deleteButton);

      orderPanel.add(inventoryTableScrollPane, BorderLayout.CENTER);
      orderPanel.add(modifyItemsPanel, BorderLayout.SOUTH);
  }

  private static void buildInventoryTable(Connection conn, DefaultTableModel inventoryTableModel){
    inventoryTableModel.setRowCount(0);
    String query = "SELECT item_id, item_name, amount FROM inventory";
    String id;
    String name;
    String amount;
    try(Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)){
          while(rs.next()){
            id = String.valueOf(rs.getInt(1));
            name = rs.getString(2);
            amount = String.valueOf(rs.getInt(3));

            String [] temp_row = new String[]{id, name , amount};
            inventoryTableModel.addRow(temp_row);
          }
    } 
    catch(SQLException e){
      JOptionPane.showMessageDialog(null, "Error laoding the inventory table");
    }
  }
  private static void insertValueIntoDatabase(Connection conn, int id, String name, int amount, int transactionId){
    try(
        PreparedStatement ps = conn.prepareStatement(
            "insert into inventory(item_id, item_name, amount,transaction_id) values (?,?,?,?)"
    )){

    ps.setInt(1, id);
    ps.setString(2, name);
    ps.setInt(3, amount);
    ps.setInt(4, transactionId);
    ps.execute();
    JOptionPane.showMessageDialog(null, " Item adding successfully");
    }
    catch(SQLException e){
        JOptionPane.showMessageDialog(null, "Error adding inventory item: " + e.getMessage());
    }
  } 
  private static void updateValueIntoDatabase(Connection conn, int id, String name, int amount, int transactionId){
    
    String query = "SELECT  employee_id, emp_email,emp_phone FROM employee where employee_id = ?";
    
    String prev_name="";
    int prev_amount=-1;
    int prev_transaction=-1;
    try(PreparedStatement ps = conn.prepareStatement(query)){
    ps.setInt(1, id); 

    try(ResultSet rs = ps.executeQuery()){
        if(rs.next()){ 
            prev_name = rs.getString(1);
            prev_amount = rs.getInt(2);
            prev_transaction = rs.getInt(3);
        } 
        else{
            JOptionPane.showMessageDialog(null, "No item found with ID: " + id);
        }
    }
} catch (SQLException e) {
    JOptionPane.showMessageDialog(null, "Error loading the inventory table: " + e.getMessage());
}
    if (name.isEmpty()){
      name = prev_name;
    }
    if(amount ==-1){
      amount = prev_amount;
    }
    if(transactionId == -1){
      transactionId = prev_transaction;
    }
    try(
        PreparedStatement ps = conn.prepareStatement(
             "UPDATE inventory SET item_name = ?, amount = ?, transaction_id = ? WHERE item_id = ?"
    )){

    ps.setString(1, name);
    ps.setInt(2, amount);
    ps.setInt(3, transactionId);
    ps.setInt(4, id);
    ps.executeUpdate();
    JOptionPane.showMessageDialog(null, " Item updated successfully");
    }
    catch(SQLException e){
        JOptionPane.showMessageDialog(null, "Error updating inventory item: " + e.getMessage());
    }
  } 


  private static void deleteValueIntoDatabase(Connection conn, int id){
      // we need ti check if the value exists in menu_items_inventory as well adn delte form there as well
    try(
        PreparedStatement ps = conn.prepareStatement(
             "DELETE FROM inventory WHERE item_id = ?"
    )){

    ps.setInt(1, id);
    //ps.setString(2, name);
    //ps.setInt(3, amount);
    //ps.setInt(4, transactionId);
    ps.executeUpdate();
    JOptionPane.showMessageDialog(null, " Item deleted successfully");
    }
    catch(SQLException e){
        JOptionPane.showMessageDialog(null, "Error deleting inventory item: " + e.getMessage());
    }
  }



  //
    // AVI CHANGE FROM THE PART BELOW
    ///

    private static void buildEmployeePanel(Connection conn, JPanel orderPanel) {
        DefaultTableModel inventoryTableModel = new DefaultTableModel(new String [] {"Item ID","Item Name", "Amount Left"},0);
        JTable inventoryTable = new JTable(inventoryTableModel);
        JScrollPane inventoryTableScrollPane = new JScrollPane(inventoryTable);

        buildInventoryTable(conn, inventoryTableModel);


        // creating an area wehre the manager can add update and delete items

        JPanel modifyItemsPanel = new JPanel(new FlowLayout());
        JTextField id = new JTextField(3);
        JTextField name = new JTextField(20);
        JTextField amount = new JTextField(5);
        JTextField transactionId = new JTextField(20);

        modifyItemsPanel.add(new JLabel("Item ID"));
        modifyItemsPanel.add(id);

        modifyItemsPanel.add(new JLabel("Item name"));
        modifyItemsPanel.add(name);

        modifyItemsPanel.add(new JLabel("Item amount"));
        modifyItemsPanel.add(amount);

        modifyItemsPanel.add(new JLabel("Transaction Number"));
        modifyItemsPanel.add(transactionId);


        JButton addButton = new JButton("ADD");
        addButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String temp_id = id.getText().trim();
                String temp_name = name.getText().trim();
                String temp_amount = amount.getText().trim();
                String temp_transactionId= transactionId.getText().trim();
                if (temp_id.isEmpty() || temp_name.isEmpty() || temp_amount.isEmpty() || temp_transactionId.isEmpty()  ) {
                    JOptionPane.showMessageDialog(null, "Please fill all fields (Id, Name, Amount, Transaction ID).");
                    return;
                }
                int num_id = Integer.parseInt(temp_id);
                int num_amount = Integer.parseInt(temp_amount);
                int num_transactionId = Integer.parseInt(temp_transactionId);

                // should I add checks for empty and value checking ?

                insertValueIntoDatabase(conn,num_id,temp_name,num_amount,num_transactionId);
                buildInventoryTable(conn, inventoryTableModel);
            }
        });
        modifyItemsPanel.add(addButton);

        JButton updateButton = new JButton("UPDATE");
        updateButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String temp_id = id.getText().trim();
                String temp_name = name.getText().trim();
                String temp_amount = amount.getText().trim();
                String temp_transactionId= transactionId.getText().trim();
                if (temp_id.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Please the ID field.");
                    return;
                }
                int num_id = Integer.parseInt(temp_id);
                int num_amount= -1;
                int num_transactionId=-1;
                if(!temp_amount.isEmpty()){
                    num_amount = Integer.parseInt(temp_amount);
                }
                if(!temp_transactionId.isEmpty()){
                    num_transactionId = Integer.parseInt(temp_transactionId);
                }
                // should I add checks for empty and value checking ?
                updateValueIntoDatabase(conn,num_id,temp_name,num_amount,num_transactionId);
                buildInventoryTable(conn, inventoryTableModel);
                //buildInventoryTable(conn, inventoryTableModel);
            }
        });
        modifyItemsPanel.add(updateButton);


        JButton deleteButton = new JButton("DELETE");
        deleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String temp_id = id.getText().trim();
                String temp_name = name.getText().trim();
                String temp_amount = amount.getText().trim();
                String temp_transactionId= transactionId.getText().trim();
                if (temp_id.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Please the ID field.");
                    return;
                }
                int num_id = Integer.parseInt(temp_id);
                int num_amount;
                int num_transactionId;
                if(!temp_amount.isEmpty()){
                    num_amount = Integer.parseInt(temp_amount);
                }
                if(!temp_transactionId.isEmpty()){
                    num_transactionId = Integer.parseInt(temp_transactionId);
                }

                // should I add checks for empty and value checking ?
                deleteValueIntoDatabase(conn,num_id);
                buildInventoryTable(conn, inventoryTableModel);
            }
        });
        modifyItemsPanel.add(deleteButton);

        orderPanel.add(inventoryTableScrollPane, BorderLayout.CENTER);
        orderPanel.add(modifyItemsPanel, BorderLayout.SOUTH);
    }

    private static void buildEmployeeTable(Connection conn, DefaultTableModel inventoryTableModel){
        inventoryTableModel.setRowCount(0);
        String query = "SELECT item_id, item_name, amount FROM inventory";
        String id;
        String name;
        String amount;
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query)){
            while(rs.next()){
                id = String.valueOf(rs.getInt(1));
                name = rs.getString(2);
                amount = String.valueOf(rs.getInt(3));

                String [] temp_row = new String[]{id, name , amount};
                inventoryTableModel.addRow(temp_row);
            }
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, "Error laoding the inventory table");
        }
    }
    private static void insertEmpIntoDatabase(Connection conn, int id, String name, int amount, int transactionId){
        try(
                PreparedStatement ps = conn.prepareStatement(
                        "insert into inventory(item_id, item_name, amount,transaction_id) values (?,?,?,?)"
                )){

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setInt(3, amount);
            ps.setInt(4, transactionId);
            ps.execute();
            JOptionPane.showMessageDialog(null, " Item adding successfully");
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, "Error adding inventory item: " + e.getMessage());
        }
    }
    private static void updateEmpIntoDatabase(Connection conn, int id, String name, int amount, int transactionId){

        String query = "SELECT  item_name, amount,transaction_id FROM inventory where item_id = ?";

        String prev_name="";
        int prev_amount=-1;
        int prev_transaction=-1;
        try(PreparedStatement ps = conn.prepareStatement(query)){
            ps.setInt(1, id);

            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    prev_name = rs.getString(1);
                    prev_amount = rs.getInt(2);
                    prev_transaction = rs.getInt(3);
                }
                else{
                    JOptionPane.showMessageDialog(null, "No item found with ID: " + id);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading the inventory table: " + e.getMessage());
        }
        if (name.isEmpty()){
            name = prev_name;
        }
        if(amount ==-1){
            amount = prev_amount;
        }
        if(transactionId == -1){
            transactionId = prev_transaction;
        }
        try(
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE inventory SET item_name = ?, amount = ?, transaction_id = ? WHERE item_id = ?"
                )){

            ps.setString(1, name);
            ps.setInt(2, amount);
            ps.setInt(3, transactionId);
            ps.setInt(4, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, " Item updated successfully");
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, "Error updating inventory item: " + e.getMessage());
        }
    }


    private static void deleteEmpIntoDatabase(Connection conn, int id){
        // we need ti check if the value exists in menu_items_inventory as well adn delte form there as well
        try(
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM inventory WHERE item_id = ?"
                )){

            ps.setInt(1, id);
            //ps.setString(2, name);
            //ps.setInt(3, amount);
            //ps.setInt(4, transactionId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, " Item deleted successfully");
        }
        catch(SQLException e){
            JOptionPane.showMessageDialog(null, "Error deleting inventory item: " + e.getMessage());
        }
    }

}//end Class
