/**
 * This file is used to generate the orders window which shows previous orders sorted by date in increments of 20.
 *
 * @author Vikrum
 * @version 1.0
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * The Orders class extends JDialog to create a dialog for displaying and managing orders.
 * It fetches and displays orders from a PostgreSQL database in a JTable, allowing users to
 * load more orders in increments of 20.
 *
 * @author Vikrum
 * @version 1.0
 */
public class Orders extends JDialog {
    private JTable ordersTable; // Table to display orders
    private DefaultTableModel tableModel; // Table model for managing data
    private Connection connection; // Database connection
    private int numLoadedOrders = 0; // Tracks how many orders have been loaded
    private final int LIMIT = 20; // Number of orders to get per button-press
    private JButton loadMoreButton; // Button to load more orders

    /**
     * Constructs an Orders dialog.
     *
     * @param parentFrame The parent JFrame that owns this dialog.
     */
    public Orders(JFrame parentFrame) {
        super(parentFrame, "Orders", true);
        setSize(600, 600);
        setLayout(new BorderLayout());

        // Initialize table
        String[] columnNames = {"Order #", "Time", "Date", "Total Cost ($)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        ordersTable = new JTable(tableModel);
        
        // Add table to scrollPane for scrolling
        JScrollPane scrollPane = new JScrollPane(ordersTable);

        // Load more button
        loadMoreButton = new JButton("Load 20 More Orders");
        loadMoreButton.addActionListener(new ActionListener() { // Add listener to load more orders when button is pressed
            @Override
            public void actionPerformed(ActionEvent e) {
                getOrders();
            }
        });

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose()); // Close dialog

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadMoreButton);
        buttonPanel.add(closeButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load the first 20 orders
        getOrders();
    }

    /**
     * Retrieves and loads orders from the database in increments of 20.
     * Orders are fetched in descending order of date and time.
     * If no more orders are available, the load button is disabled.
     */
    private void getOrders() {
        try {
            connection = DriverManager.getConnection(
                String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", "team_24_db"),
                 dbSetup.user, dbSetup.pswd);
            String query = "SELECT id, time, date, cost FROM orders ORDER BY date DESC, time DESC LIMIT ? OFFSET ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, LIMIT); // Get 20 Rows
            statement.setInt(2, numLoadedOrders); // Starts from last loaded
            
            ResultSet resultSet = statement.executeQuery();
            boolean hasOrders = false; // Flag to see if there are any more orders to add
            
            // Iterate over the resultSet to load orders into the table
            while(resultSet.next()) {
                hasOrders = true;
                int orderId = resultSet.getInt("id");
                String time = resultSet.getString("time");
                String date = resultSet.getString("date");
                double cost = resultSet.getDouble("cost");
                
                tableModel.addRow(new Object[]{orderId, time, date, cost});
            }
            
            resultSet.close();
            statement.close();
            connection.close();

            // If orders are found and added, numLoadedOrders should increase
            if(hasOrders)
                numLoadedOrders += LIMIT;
            // Disable the button if no orders left to load
            else
                loadMoreButton.setEnabled(false);

        } catch(SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error getting orders.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
