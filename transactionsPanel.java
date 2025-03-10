/**
* This file is used to generate and display the transactions panel for displaying total order history.
* The file is also created for easy GUI implementaion.
*
* @author Anuraag
*/

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
* The transactionsPanel class extends JDialog to create a dialog for displaying the total orders history.
* It fetches information about every order from a PostgreSQL database and then displays this in a JTable,
* allowing users to view all 125000+ orders in order from latest to oldest.
* 
* @author Anuraag
*/
public class transactionsPanel extends JDialog {
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    /**
     * Constructs an transactionsPanel dialog and fetches order information from a PostgreSQL database.
     *
     * @author Anuraag
     * @param parentFrame The parent JFrame that owns this dialog.
     */
    public transactionsPanel(JFrame parentFrame) {
        super(parentFrame, "Transactions", true);
        setSize(600, 600);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Transactions", SwingConstants.CENTER);

        JTextArea searchBar = new JTextArea(2, 10);
        searchBar.setEditable(false);
        searchBar.setText("Search Bar");

        String[] columnNames = {"Order #", "Total Cost ($)", "Date", "Time", "Menu Item(s)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        ordersTable = new JTable(tableModel);
        ordersTable.setDefaultEditor(Object.class, null);

        Connection conn = null;
        //STEP 1 (see line 7)
        String database_name = "team_24_db";
        String database_user = "team_24";
        String database_password = "tEaM_24!@";
        String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);
        try {
            conn = DriverManager.getConnection(database_url, database_user, database_password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        //JOptionPane.showMessageDialog(null,"Opened database successfully");

        //String name = "";
        try{
            //create a statement object
            Statement stmt = conn.createStatement();
            //create a SQL statement
            //Step 2 (see line 8)
            String sqlStatement = "SELECT * FROM orders ORDER BY date DESC, time DESC";
            //send statement to DBMS
            ResultSet result = stmt.executeQuery(sqlStatement);

            /*
            name += String.format("%-18s %-12s %-15s %-10s\n", "Order Number:", "Cost:", "Date:", "Time:");
            name += "-------------------------------------------------------------\n";
            */
            while (result.next()) {
            // you probably need to change the column name that you are retrieving
            // this command gets the data from the "flavor" attribute
                //name += "Order Number: " + result.getString("order_number") + "     Cost: " + result.getString("cost") + "     date: " + result.getString("date") + "     time: " + result.getString("time") + "\n";
                int orderNum = result.getInt("order_number");
                double cost = result.getDouble("cost");
                String date = result.getString("date");
                String time = result.getString("time");
                String menuItem = result.getString("quantity") + " " + result.getString("flavor") + " " + result.getString("tea_type");
                
                tableModel.addRow(new Object[]{orderNum, cost, date, time, menuItem});

                /*
                name += String.format("%-18s %-12s %-15s %-10s\n",
                    result.getString("order_number"),
                    result.getString("cost"),
                    result.getString("date"),
                    result.getString("time"));
                */
            }
        } catch (Exception e){
            JOptionPane.showMessageDialog(null,"Error accessing Database.");
        }

        JScrollPane scrollPane = new JScrollPane(ordersTable);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose()); // Close dialog

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        // Add components to dialog
        add(titleLabel, BorderLayout.NORTH);
        add(searchBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
