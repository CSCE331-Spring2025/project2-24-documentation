/**
 * This file is used to create customer orders, and navigate to other important pages including manager view, orders, and
 * transactions.
 *     
 * 
 * @author team_24, Elias Meza
 */

import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Checkout class extends JFrame and implements ActionListener. Contains all methods needed for customer/cashier checkout.
 * 
 * @author team_24, Elias Meza
 * @version 1.0
 */

public class Checkout extends JFrame implements ActionListener {
    /** Main application frame */
    static JFrame main_frame;
    /** List of valid add-ons for tea */
    static List<String> valid_addons = new ArrayList<String>();
    /** List of valid flavors for tea */
    static List<String> valid_flavors = new ArrayList<String>();
    /** List of valid tea types */
    static List<String> valid_tea_types = new ArrayList<String>();
    /** List of prices for each tea type */
    static List<Float> tea_type_prices = new ArrayList<Float>();
    /** List of prices for add-ons */
    static List<Float> addon_prices = new ArrayList<Float>();
    /** Array of valid sugar levels */
    static String[] validSugarLevels = {"100%", "0%", "20%", "50%", "80%",  "120%"};
    /** Array of valid ice levels */
    static String[] validIceLevels ={"Regular","No Ice", "Light", "Extra"};
    /** Array of possible quantities */
    static String[] possibleQuantities = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}; 
    /** Panel displaying checkout details */
    static JPanel checkout_display = new JPanel();
    /** Panel for tab buttons */
    static JPanel tabs_panel = new JPanel();
    /** Panel displaying available items */
    static JPanel items_panel = new JPanel();
    /** Layout manager for checkout display */
    static BoxLayout checkoutLayout = new BoxLayout(checkout_display, BoxLayout.Y_AXIS);
    /** Flag indicating manager access */
    static boolean managerAccess = true;
    /** Database connection */
    static Connection conn = null;
    /** Current order number */
    static int currOrderNum = 1;

    /**
     * Main entry point of the application.
     * Initializes the checkout system and sets up GUI components.
     * 
     * @param args Command-line arguments
     */
    public static void main(String[] args)
    {
    Checkout checkout = new Checkout();
    checkout.connectToDatabase();
    /**
     * ActionListener for selecting tea items.
     * Opens a dialog to modify the tea order with add-ons, sugar, ice levels, and flavors.
     */
    ActionListener teaButtonActionListner = new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent e){
        if(e.getSource() instanceof JButton){
          String teaName = ((JButton) e.getSource()).getText();

          JLabel promptSugar = new JLabel("Sugar level:");
          JComboBox sugarLevels = new JComboBox(validSugarLevels);

          JLabel promptIce = new JLabel("Ice level:");
          JComboBox iceLevels = new JComboBox(validIceLevels);

          JLabel promptFlavor = new JLabel("Flavor:");
          JComboBox allFlavors = new JComboBox(valid_flavors.toArray());

          JLabel promptQuantity = new JLabel("Quantity");
          JComboBox allQuantities = new JComboBox(possibleQuantities);
          JLabel promptAddons = new JLabel("What addons would you like?");
          JList<String> addonsList = new JList<>(valid_addons.toArray(new String[0]));
          addonsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          JScrollPane addonScrollPane = new JScrollPane(addonsList);

          JLabel promptCustomRequest = new JLabel("Personal requests");
          JTextField customerRequest = new JTextField(20);
          
          Object[] modificationOptions = new Object[] {promptCustomRequest, customerRequest,
                        promptSugar, sugarLevels,
                        promptIce, iceLevels,
                        promptFlavor, allFlavors,
                        promptQuantity, allQuantities,
                        promptAddons, addonScrollPane};
          int option = JOptionPane.showConfirmDialog(null, modificationOptions, "Modify your " + teaName, JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        String selectedSugar = (String) sugarLevels.getSelectedItem();
                        String selectedIce = (String) iceLevels.getSelectedItem();
                        String selectedFlavor = (String) allFlavors.getSelectedItem();
                        String selectedQuantity = (String) allQuantities.getSelectedItem();
                        String specialRequest = customerRequest.getText();
                        java.util.List<String> selectedAddons = addonsList.getSelectedValuesList();
                        
                        String[] orderDetails = {teaName, selectedSugar, selectedIce, selectedFlavor, selectedQuantity, specialRequest, String.join(", ", selectedAddons)};
                        System.out.println("Order details: " + java.util.Arrays.toString(orderDetails));
                        updateCheckoutDisplay(orderDetails);
                    }
        }
      }
    };
    /**
     * ActionListener for changing panels based on user selection.
     * Handles navigation between Checkout, Order History, and Manager View.
     */
    ActionListener changePanelActionListner = new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent e){
        if(e.getSource() instanceof JButton){
          String command = ((JButton) e.getSource()).getText();

          if(command.equals("Checkout")){
            items_panel.setVisible(true);
            tabs_panel.setVisible(true);
            checkout_display.setVisible(true);
            fetchAddons();
            fetchFlavors();
            fetchTeaTypes();
            items_panel.revalidate();
            items_panel.repaint();
          }else{
            items_panel.setVisible(false);
            checkout_display.setVisible(false);
          }

          if(command.equals("Order History")){
             SwingUtilities.invokeLater(() -> {
                    Orders ordersDialog = new Orders(main_frame);
                    ordersDialog.setVisible(true);
                });
            }

          if(command.equals("Transaction History")){
            SwingUtilities.invokeLater(() -> {
                    transactionsPanel transactionsDialog = new transactionsPanel(main_frame);
                    transactionsDialog.setVisible(true);
                });
            }
      
          if(command.equals("Manager View")){
            new manager();
          }
          else{
            main_frame.setVisible(true);
          }
        }
      }
    };
      //inital instantiation of the window
      int default_frame_width = 500;
      int default_frame_height = 500;
      fetchAddons();
      fetchFlavors();
      fetchTeaTypes();
      String[] tabButtonNames = {"Checkout", "Transaction History", "Order History", "Notifications", "Manager View", "Logout"};
      // create a new frame
      main_frame = new JFrame("DB GUI");
      main_frame.setLayout(new BorderLayout());
      main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      main_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                checkout.closeDatabase();
            }
        });


      // Panels used for the Checkout
      items_panel.setLayout(new GridBagLayout());
      items_panel.setPreferredSize(new Dimension((int)(default_frame_width*0.8), (int)(default_frame_height*0.8)));
      
      tabs_panel.setPreferredSize(new Dimension(default_frame_width, (int)(default_frame_height*0.2)));
      tabs_panel.setLayout(new GridLayout(1, tabButtonNames.length,20,20));
      
      checkout_display.setLayout(checkoutLayout); 
      checkout_display.setPreferredSize(new Dimension((int)(default_frame_width*0.2), (int)(default_frame_height*0.8)));
      checkout_display.setBackground(Color.lightGray);
      JButton checkoutButton = new JButton("Checkout");
      /**
       * ActionListener for handling checkout button.
       * Processes the current order, calculates total cost, and saves order details to the database.
       * Displays a confirmation message upon successful order placement.
       */
      checkoutButton.addActionListener(e -> {
    try {
        if (conn == null) {
            JOptionPane.showMessageDialog(null, "Database connection is null.");
            return;
        }
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM orders");
        int newOrderID = 1;
        if (rs.next()) {
            newOrderID = rs.getInt(1) + 1;
        }

        conn.setAutoCommit(false); 
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(now.toLocalDate());
        java.sql.Time sqlTime = java.sql.Time.valueOf(now.toLocalTime().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        String sTime = String.valueOf(sqlTime);
        String sDate = String.valueOf(sqlDate);

        Component[] components = checkout_display.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel orderPanel = (JPanel) component;
                for (Component orderComponent : orderPanel.getComponents()) {
                    if (orderComponent instanceof JLabel) {
                        String orderText = ((JLabel) orderComponent).getText();

                        String totalCostStr = orderText.split("Cost: ")[1].replace("</html>", "").trim();
                        float totalCost = Float.parseFloat(totalCostStr);

                        String[] details = orderText.split("<br>");
                        String menuItem = details[0].replace("<html><b>", "").replace("</b>", "").trim();
                        String flavor = details[1].split(": ")[1].trim();
                        String sugarLevel = details[2].split(": ")[1].trim();
                        String iceLevel = details[3].split(": ")[1].trim();
                        int quantity = Integer.parseInt(details[4].split(": ")[1].trim());
                        String addons = details[5].split(": ")[1].trim();
                        String customerRequests = details[6].split(": ")[0].trim();

                        float menuPrice = tea_type_prices.get(valid_tea_types.indexOf(menuItem));
                        float addonPrice = addon_prices.get(valid_addons.indexOf(addons));

                        String orderQuery = "INSERT INTO orders (id, cost, tip, date, time, menu_item_price, addon_price, flavor, tea_type, quantity, order_number) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        PreparedStatement orderStmt = conn.prepareStatement(orderQuery);
                        orderStmt.setInt(1, newOrderID);
                        orderStmt.setFloat(2, totalCost);
                        orderStmt.setFloat(3, 0.00f);
                        orderStmt.setString(4, sDate);
                        orderStmt.setString(5, sTime);
                        orderStmt.setFloat(6, menuPrice);
                        orderStmt.setFloat(7, addonPrice);
                        orderStmt.setString(8, flavor);
                        orderStmt.setString(9, menuItem);
                        orderStmt.setInt(10, quantity);
                        orderStmt.setInt(11, currOrderNum);

                        orderStmt.executeUpdate();

                        String menuItemQuery = "INSERT INTO menu_item (item_id, flavor, quantity, tea_type, sugar_level, ice_level, customer_requests, menu_item_price) " +
                                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                        PreparedStatement menuItemStmt = conn.prepareStatement(menuItemQuery);
                        menuItemStmt.setInt(1, newOrderID);
                        menuItemStmt.setString(2, flavor);
                        menuItemStmt.setInt(3, quantity);
                        menuItemStmt.setString(4, menuItem);
                        menuItemStmt.setString(5, sugarLevel);
                        menuItemStmt.setString(6, iceLevel);
                        menuItemStmt.setString(7, customerRequests);
                        menuItemStmt.setFloat(8, menuPrice);

                        menuItemStmt.executeUpdate();

                        boolean pearls = addons.contains("Pearls");
                        boolean pudding = addons.contains("Pudding");
                        boolean jelly = addons.contains("Jelly");
                        boolean iceCream = addons.contains("Ice Cream");
                        boolean creama = addons.contains("Creama");
                        boolean boba = addons.contains("Boba");

                        String addonQuery = "INSERT INTO addon (item_id, pearls, pudding, jelly, ice_cream, creama, boba, addon_price) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                        PreparedStatement addonStmt = conn.prepareStatement(addonQuery);
                        addonStmt.setInt(1, newOrderID);
                        addonStmt.setBoolean(2, pearls);
                        addonStmt.setBoolean(3, pudding);
                        addonStmt.setBoolean(4, jelly);
                        addonStmt.setBoolean(5, iceCream);
                        addonStmt.setBoolean(6, creama);
                        addonStmt.setBoolean(7, boba);
                        addonStmt.setFloat(8, addonPrice);

                        addonStmt.executeUpdate();
                        
                        newOrderID++; 
                    }
                }
            }
        }

        currOrderNum++; 
        conn.commit(); 

        JOptionPane.showMessageDialog(null, "Order placed successfully!");
    } catch (Exception ex) {
        try {
            conn.rollback(); // Rollback if an error occurs
        } catch (SQLException rollbackEx) {
            rollbackEx.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Error processing checkout: " + ex.getMessage());
        ex.printStackTrace();
    }
});

      checkoutButton.setBackground(Color.blue);
      checkout_display.add(checkoutButton, BorderLayout.SOUTH);


      //Buttons used for items_panel
      JButton[] menuItemButtons = new JButton[valid_tea_types.size()];
      /** 
       *Creates all possible tea types based on valid tea types and
       *assign each button a tea button action listener 
       */
      for(int i = 0; i<valid_tea_types.size(); i++){
        menuItemButtons[i] = new JButton(valid_tea_types.get(i));
        menuItemButtons[i].setActionCommand("Add tea");
        menuItemButtons[i].addActionListener(teaButtonActionListner);
      }

      //Buttons used for tabs_panel
      JButton[] tabButtons = new JButton[tabButtonNames.length];
      /**
       *Creates all possible tabs based on tab buttons array and
       *assign each button a change panel action listener 
       */
      for(int i = 0; i<tabButtonNames.length; i++){
        tabButtons[i] = new JButton(tabButtonNames[i]);
        tabButtons[i].setActionCommand("Change panel");
        tabButtons[i].addActionListener(changePanelActionListner);
        tabs_panel.add(tabButtons[i]);
      }
      
      // Create GridBagConstraints
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.BOTH; // Make the button expand both horizontally and vertically
      gbc.insets = new Insets(10, 10, 10, 10); // Add padding around each button

      // Add buttons to the panel with GridBagLayout constraints
      for(int i =0; i< valid_tea_types.size(); i++){
        gbc.gridx = i%2; gbc.gridy = i/2; items_panel.add(menuItemButtons[i], gbc);
      }

      // add panel to frame
      main_frame.getContentPane().add(items_panel, BorderLayout.CENTER);
      main_frame.getContentPane().add(checkout_display, BorderLayout.EAST);
      main_frame.getContentPane().add(tabs_panel, BorderLayout.SOUTH);

      // set the size of frame
      
      main_frame.setVisible(true);
      main_frame.pack();

    }
    /**
     * Establishes a connection to the PostgreSQL database.
     */
public void connectToDatabase() {
        dbSetup dbInfo = new dbSetup();
        String database_name = "team_24_db";
        String database_user = dbInfo.user;
        String database_password = dbInfo.pswd;
        String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);
        
        try {
            conn = DriverManager.getConnection(database_url, database_user, database_password);
            JOptionPane.showMessageDialog(null, "Opened database successfully");
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error connecting to database.");
        }
        
    }
    /**
    * Closes the database connection.
    */
    public void closeDatabase() {
        try {
            if (conn != null) {
                conn.close();
                JOptionPane.showMessageDialog(null, "Connection Closed.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection NOT Closed.");
        }
    }
    /**
     * Fetches available tea types from the database.
     */
    public static void fetchTeaTypes() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM valid_tea_types");

            while (result.next()) {
              valid_tea_types.add(result.getString("name"));
              tea_type_prices.add(result.getFloat("price"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }
    /** 
     * Fetches available flavors from the database.
     */
    public static void fetchFlavors() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM valid_flavors");
            valid_flavors.add("Default");
            
            while (result.next()) {
              valid_flavors.add(result.getString("name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }
    /**
     * Fetches available add-ons from the database.
     */
    public static void fetchAddons() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM valid_addons");
            valid_addons.add("None");
            addon_prices.add((float)0.0);
            while (result.next()) {
                valid_addons.add(result.getString("name"));
                addon_prices.add(result.getFloat("price"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }
    /**
     * Handles action events triggered by user interactions.
     * 
     * @param e Action event containing source information
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, e.getActionCommand());
    }
/**
  * Updates the checkout display with order details.
  * 
  * @param orderDetails Array containing order information
  */
public static void updateCheckoutDisplay(String[] orderDetails) {
    JPanel orderPanel = new JPanel();
    orderPanel.setLayout(new BorderLayout());
    orderPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    String orderSummary = "<html><b>" + orderDetails[0] + "</b><br>" +  
                          "Flavor: " + orderDetails[3] + "<br>" +  
                          "Sugar: " + orderDetails[1] + "<br>" +
                          "Ice: " + orderDetails[2] + "<br>" +
                          "Qty: " + orderDetails[4] + "<br>" +
                          "Add-ons: " + orderDetails[6] + "<br>" +
                          "Request: " + orderDetails[5] + "<br>"+
                          "Cost: "+ ((tea_type_prices.get(valid_tea_types.indexOf(orderDetails[0]))
                                      + addon_prices.get(valid_addons.indexOf(orderDetails[6])))*(Integer.parseInt(orderDetails[4])) ) +"</html>";
    JLabel orderLabel = new JLabel(orderSummary);
    JButton removeButton = new JButton("X");
    removeButton.setPreferredSize(new Dimension(90, 30));
    removeButton.addActionListener(e -> {
        checkout_display.remove(orderPanel);
        checkout_display.revalidate();
        checkout_display.repaint();
    });

    orderPanel.add(orderLabel, BorderLayout.CENTER);
    orderPanel.add(removeButton, BorderLayout.EAST);

    checkout_display.add(orderPanel);
    checkout_display.setPreferredSize(new Dimension((int) (main_frame.getWidth() * 0.2), main_frame.getHeight()));
    checkout_display.revalidate();
    checkout_display.repaint();
  }
}
