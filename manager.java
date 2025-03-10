/**
* This file is used to generate and display the manager view window for manager specific operations,
* and allows the manager to add, modify, or remove; employees, inventory items, and menu items. The
* GUI also allows the manger to generate reports based on database data quickly and easily.
*
* @author team_24
*/

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.format.DateTimeFormatter;

/**
* manager class extends JFrame, and contains all methods needed for displaying GUI and interaction with
* database for manager functions.
* 
* @author zach
*/
public class manager extends JFrame {
    static JFrame managerFrame = new JFrame("manager view");
    private ArrayList<String> inventoryText = new ArrayList<String>(25);
    private ArrayList<JButton> inventoryButtons = new ArrayList<JButton>(25);
    private ArrayList<String> employeeText = new ArrayList<String>(25);
    private ArrayList<JButton> employeeButtons = new ArrayList<JButton>(25);
    private Connection conn = null;
    private JPanel inventoryPanel = new JPanel();
    private JPanel employeeButtonPanel = new JPanel();

    // global text entry
    private JTextField ingredientValue = new JTextField("");
    private JTextField amountValue = new JTextField("");
    private JTextField costPerAmountValue = new JTextField("");
    private JTextField employeeIdValue = new JTextField("");
    private JTextField hoursWorkedValue = new JTextField("");
    private JTextField managerValue = new JTextField("");
    private JTextField passwordValue = new JTextField("");

    String ingredientName = "";
    String employeeId = "";

    // x-report
    private HashMap<Integer, Integer> salesPerHour = new HashMap<>();
    private HashMap<Integer, Double> revenuePerHour = new HashMap<>();
    private JTextArea xReportTextArea;

    // z-report
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JTable teaOrdersTable;
    private DefaultTableModel teaTableModel;
    JTextArea searchBar;
    private LocalDate testDate = LocalDate.now(); //today's date

    /**
     * default constructor
     * 
     * @author team_24
     */
    public manager() {
        connectToDatabase();
        makeGUI();
        
        scheduleEndOfDayReset();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * runs when manager.java runs
     * 
     * @author zach
     * @param args list of arguments for main function
     */
    public static void main(String[] args) {
        manager obj = new manager();
        obj.connectToDatabase();
        obj.makeGUI();
    }

    // gui functions

    /**
     * Creates main gui of manager page
     * 
     * @author zach Lawrence, Elias Meza
     */
    public void makeGUI() {
        getInventoryInfo();
        getEmployeeInfo();

        JPanel employeePanel = new JPanel();

        // change layout frame
        managerFrame.setLayout(new BorderLayout());
        managerFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                System.out.println("Window has been opened.");
            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Window is closing.");
                closeDataBase();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("Window has been closed.");
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        // gui stuff
        inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
        inventoryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        employeeButtonPanel.setLayout(new BoxLayout(employeeButtonPanel, BoxLayout.Y_AXIS));
        employeeButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create components
        JButton xReport = new JButton("X-Report");
        JButton zReport = new JButton("Z-Report");
        JButton productUsageReport = new JButton("Product Usage");
        JButton addInventoryItem = new JButton("Add Inventory");
        JButton addEmployee = new JButton("Add Employee");

        JButton testItemRemoval = new JButton("Edit Item Offerings");
        Checkout.tabs_panel.add(testItemRemoval);
        testItemRemoval.addActionListener(e -> {
            // will open a JDialogue that will have a sentence with 2 drop downs and 2 fill
            // in the blanks:
            // One with "Remove" or "Add"
            // Second with "Item", "Flavor", or "addon"
            // The two fill in the blanks will be name: and price:
            // based on the answers, it will create a query and execute it.
            JPanel removeItemPanel = new JPanel();
            String[] firstOptionArray = { "Add", "Remove" };
            JComboBox<String> addRemoveDropdown = new JComboBox<>(firstOptionArray);
            removeItemPanel.add(addRemoveDropdown);
            String[] secondOptionArray = { "tea_type", "flavor", "addon" };
            JComboBox<String> itemDropdown = new JComboBox<>(secondOptionArray);
            JTextField nameToUse = new JTextField(20);
            JTextField priceToUse = new JTextField(20);
            Object[] removeOptions = new Object[] { addRemoveDropdown, itemDropdown, nameToUse, priceToUse };
            int option = JOptionPane.showConfirmDialog(null, removeOptions, null, JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String firstOption = (String) addRemoveDropdown.getSelectedItem();
                String secondOption = (String) itemDropdown.getSelectedItem();
                String nameToUseQuery = nameToUse.getText().trim();
                String priceToUseQuery = priceToUse.getText().trim();
                try {
                    conn.setAutoCommit(false);
                    String query = "";
                    PreparedStatement addItemStmt = null;
                    if (!(firstOption.isEmpty()) && !(secondOption.isEmpty()) && !(nameToUseQuery.isEmpty())) {
                        if (firstOption.equals("Add")) {
                            Statement stmt = conn.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM valid_" + secondOption + "s");
                            int newOrderID = 1;
                            if (rs.next()) {
                                newOrderID = rs.getInt(1) + 1;
                                System.out.println(newOrderID);
                            }
                            if (!(priceToUseQuery.isEmpty())) {
                                if (secondOption.equals("flavor")) {
                                    query = "INSERT INTO valid_" + secondOption + "s (id, name) VALUES (?, ?)";
                                    addItemStmt = conn.prepareStatement(query);
                                    addItemStmt.setInt(1, newOrderID);
                                    addItemStmt.setString(2, nameToUseQuery);
                                } else {
                                    if (!(priceToUseQuery.isEmpty())) {
                                        query = "INSERT INTO valid_" + secondOption
                                                + "s (id, name, price) VALUES (?, ?, ?)";
                                        addItemStmt = conn.prepareStatement(query);
                                        addItemStmt.setInt(1, newOrderID);
                                        addItemStmt.setString(2, nameToUseQuery);
                                        addItemStmt.setFloat(3, Float.parseFloat(priceToUseQuery));
                                    }
                                }
                            }
                        } else if (firstOption.equals("Remove")) {
                            query = "DELETE FROM valid_" + secondOption + "s WHERE name=?";
                            addItemStmt = conn.prepareStatement(query);
                            addItemStmt.setString(1, nameToUseQuery);
                        }
                        if (addItemStmt != null) {
                            addItemStmt.executeUpdate();
                            conn.commit();
                            JOptionPane.showMessageDialog(null, firstOption + " operation successful.");
                        }
                    }
                } catch (SQLException ex) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        rollbackEx.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(null, "Error modifying database: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    try {
                        conn.setAutoCommit(true); // Restore auto-commit
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Add buttons to the inventory panel
        for (int i = 0; i < inventoryText.size(); i++) {
            JButton button = new JButton(inventoryText.get(i));

            button.setMinimumSize(new Dimension(200, 200));
            button.setMaximumSize(new Dimension(500, 200));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("modify item");
                    modifyInventoryWindow(button.getText());
                }
            });

            inventoryButtons.add(button);
            inventoryPanel.add(button);
        }

        // Add buttons to the employee panel
        for (int i = 0; i < employeeText.size(); i++) {
            JButton button = new JButton(employeeText.get(i));

            button.setMinimumSize(new Dimension(200, 200));
            button.setMaximumSize(new Dimension(500, 200));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("modify employee");
                    modifyEmployeeWindow(button.getText());
                }
            });

            employeeButtons.add(button);
            employeeButtonPanel.add(button);
        }

        JScrollPane inventoryScroll = new JScrollPane(inventoryPanel);
        inventoryScroll.setPreferredSize(new Dimension(750, 200));

        JScrollPane employeeScroll = new JScrollPane(employeeButtonPanel);
        employeeScroll.setPreferredSize(new Dimension(400, 300));

        xReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("xreport");
                XReport();
            }
        });

        zReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("z");
                showZReportDialog();
            }
        });

        productUsageReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("pick dates");
                getDatesWindow();
            }
        });

        addInventoryItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("add item");
                addInventoryWindow();
            }
        });

        addEmployee.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("add employee");
                addEmployeeWindow();
            }
        });

        // add necessary items to pane
        employeePanel.add(xReport);
        employeePanel.add(zReport);
        employeePanel.add(productUsageReport);
        employeePanel.add(addInventoryItem);
        employeePanel.add(addEmployee);
        employeePanel.add(testItemRemoval);
        employeePanel.add(employeeScroll);

        // Add the panels to the frame
        managerFrame.add(employeePanel);
        managerFrame.add(inventoryScroll, BorderLayout.SOUTH);

        // Make sure the frame automatically resizes based on its content
        managerFrame.setSize(750, 500);

        // uncomment if running by itself:
        // managerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        managerFrame.setVisible(true);
        managerFrame.revalidate();
        managerFrame.repaint();
    }

    /**
     * Creates a window used for adding a new inventory item
     * 
     * @author zach
     */
    public void addInventoryWindow() {
        JFrame inventoryModFrame = new JFrame("inventory addition");

        // change layout frame
        inventoryModFrame.setLayout(new BorderLayout());

        // gui stuff
        JPanel inventoryP = new JPanel();
        inventoryP.setLayout(new BoxLayout(inventoryP, BoxLayout.Y_AXIS));
        inventoryP.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create components
        JButton addButton = new JButton("Add item");
        JLabel ingredientLabel = new JLabel("ingredient name:");
        ingredientValue = new JTextField("");
        JLabel amountLabel = new JLabel("amount:");
        amountValue = new JTextField("");
        JLabel costPerAmountLabel = new JLabel("cost per amount:");
        costPerAmountValue = new JTextField("");

        // set text area, panel, and text field dimensions
        ingredientLabel.setMinimumSize(new Dimension(200, 30));
        ingredientValue.setMaximumSize(new Dimension(200, 30));
        amountLabel.setMinimumSize(new Dimension(200, 30));
        amountValue.setMaximumSize(new Dimension(200, 30));
        costPerAmountLabel.setMinimumSize(new Dimension(200, 30));
        costPerAmountValue.setMaximumSize(new Dimension(200, 30));
        inventoryP.setPreferredSize(new Dimension(750, 200));

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("will add: ");
                System.out.println(ingredientLabel.getText());
                System.out.println(amountLabel.getText());
                System.out.println(costPerAmountLabel.getText());
                addInventory();
                inventoryModFrame.dispatchEvent(new WindowEvent(inventoryModFrame, WindowEvent.WINDOW_CLOSING));
            }
        });

        // add necessary items to panel
        inventoryP.add(ingredientLabel);
        inventoryP.add(ingredientValue);
        inventoryP.add(amountLabel);
        inventoryP.add(amountValue);
        inventoryP.add(costPerAmountLabel);
        inventoryP.add(costPerAmountValue);
        inventoryP.add(addButton);

        // Add the panels to the frame
        inventoryModFrame.add(inventoryP);

        // Make sure the frame automatically resizes based on its content
        inventoryModFrame.setSize(750, 500);
        inventoryModFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inventoryModFrame.setVisible(true);
    }

    /**
     * Creates a window used for adding a new employee
     * 
     * @author zach
     */
    public void addEmployeeWindow() {
        JFrame employeeModFrame = new JFrame("employee addition");

        // change layout frame
        employeeModFrame.setLayout(new BorderLayout());

        // gui stuff
        JPanel employeePanel = new JPanel();
        employeePanel.setLayout(new BoxLayout(employeePanel, BoxLayout.Y_AXIS));
        employeePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create components
        JButton addButton = new JButton("Add employee");
        JLabel employeeHoursLabel = new JLabel("weekly hours worked:");
        hoursWorkedValue = new JTextField("");
        JLabel managerLabel = new JLabel("manager access:");
        managerValue = new JTextField("");
        JLabel passwordLabel = new JLabel("password:");
        passwordValue = new JTextField("");

        employeeHoursLabel.setMinimumSize(new Dimension(200, 30));
        hoursWorkedValue.setMaximumSize(new Dimension(200, 30));
        managerLabel.setMinimumSize(new Dimension(200, 30));
        managerValue.setMaximumSize(new Dimension(200, 30));
        passwordLabel.setMinimumSize(new Dimension(200, 30));
        passwordValue.setMaximumSize(new Dimension(200, 30));

        employeePanel.setPreferredSize(new Dimension(750, 200));

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("added employee");
                addEmployee();
                employeeModFrame.dispatchEvent(new WindowEvent(employeeModFrame, WindowEvent.WINDOW_CLOSING));
            }
        });

        // add necessary items to panel
        employeePanel.add(employeeHoursLabel);
        employeePanel.add(hoursWorkedValue);
        employeePanel.add(managerLabel);
        employeePanel.add(managerValue);
        employeePanel.add(passwordLabel);
        employeePanel.add(passwordValue);

        employeePanel.add(addButton);

        // Add the panels to the frame
        employeeModFrame.add(employeePanel);

        // Make sure the frame automatically resizes based on its content
        employeeModFrame.setSize(750, 500);
        employeeModFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        employeeModFrame.setVisible(true);
    }

    /**
     * Creates a window used for modifying inventory items
     * 
     * @author zach
     * @param buttonText used to extract an ingredient name from the button
     */
    public void modifyInventoryWindow(String buttonText) {
        JFrame inventoryModFrame = new JFrame("inventory modification");

        // change layout frame
        inventoryModFrame.setLayout(new BorderLayout());

        // gui stuff
        JPanel inventoryPanelModify = new JPanel();
        inventoryPanelModify.setLayout(new BoxLayout(inventoryPanelModify, BoxLayout.Y_AXIS));
        inventoryPanelModify.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create components
        JButton modifyButton = new JButton("Overwrite item");
        JButton removeButton = new JButton("Remove item");
        JLabel ingredientLabel = new JLabel("ingredient name:");
        JLabel amountLabel = new JLabel("amount:");
        JLabel costPerAmountLabel = new JLabel("cost per amount:");

        int index = buttonText.indexOf(" amount");

        if (index != -1) {
            ingredientName = buttonText.substring(0, index);
        }

        getInventoryInfoFromIngredient(ingredientName);

        ingredientLabel.setMinimumSize(new Dimension(200, 30));
        ingredientValue.setMaximumSize(new Dimension(200, 30));
        amountLabel.setMinimumSize(new Dimension(200, 30));
        amountValue.setMaximumSize(new Dimension(200, 30));
        costPerAmountLabel.setMinimumSize(new Dimension(200, 30));
        costPerAmountValue.setMaximumSize(new Dimension(200, 30));

        inventoryPanelModify.setPreferredSize(new Dimension(750, 200));

        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("modified");
                modifyInventory(ingredientName);
                modifyInventoryButtonText(buttonText);
                inventoryModFrame.dispatchEvent(new WindowEvent(inventoryModFrame, WindowEvent.WINDOW_CLOSING));
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("removed");
                removeInventory(ingredientName);
                removeInventoryButton(buttonText);
                inventoryModFrame.dispatchEvent(new WindowEvent(inventoryModFrame, WindowEvent.WINDOW_CLOSING));
            }
        });

        // add necessary items to panel
        inventoryPanelModify.add(ingredientLabel);
        inventoryPanelModify.add(ingredientValue);
        inventoryPanelModify.add(amountLabel);
        inventoryPanelModify.add(amountValue);
        inventoryPanelModify.add(costPerAmountLabel);
        inventoryPanelModify.add(costPerAmountValue);

        inventoryPanelModify.add(modifyButton);
        inventoryPanelModify.add(removeButton);

        // Add the panels to the frame
        inventoryModFrame.add(inventoryPanelModify);

        // Make sure the frame automatically resizes based on its content
        inventoryModFrame.setSize(750, 500); // Set an initial size for the frame
        inventoryModFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inventoryModFrame.setVisible(true);
    }

    /**
     * Creates a window used for modifying employee data
     * 
     * @author zach
     * @param buttonText used to extract an employee id from the button
     */
    public void modifyEmployeeWindow(String buttonText) {
        JFrame employeeModFrame = new JFrame("employee modification");

        // change layout frame
        employeeModFrame.setLayout(new BorderLayout());

        // gui stuff
        JPanel employeePanelModify = new JPanel();
        employeePanelModify.setLayout(new BoxLayout(employeePanelModify, BoxLayout.Y_AXIS));
        employeePanelModify.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton modifyButton = new JButton("Overwrite item");
        JButton removeButton = new JButton("Remove item");
        JLabel employeeIdLabel = new JLabel("employee id:");
        JLabel hoursWorkedLabel = new JLabel("hours worked:");
        JLabel managerLabel = new JLabel("manager access:");
        JLabel passwordLabel = new JLabel("password:");

        int frontIndex = buttonText.indexOf("#");
        int endIndex = buttonText.indexOf(" worked");

        if (frontIndex != -1 && endIndex != -1) {
            employeeId = buttonText.substring(frontIndex + 1, endIndex);
            System.out.println("calculated ID: " + employeeId);
        }

        getEmployeeInfoFromId(employeeId);

        employeeIdLabel.setMinimumSize(new Dimension(200, 30));
        employeeIdValue.setMaximumSize(new Dimension(200, 30));
        hoursWorkedLabel.setMinimumSize(new Dimension(200, 30));
        hoursWorkedValue.setMaximumSize(new Dimension(200, 30));
        managerLabel.setMinimumSize(new Dimension(200, 30));
        managerValue.setMaximumSize(new Dimension(200, 30));
        passwordLabel.setMinimumSize(new Dimension(200, 30));
        passwordValue.setMaximumSize(new Dimension(200, 30));

        employeePanelModify.setPreferredSize(new Dimension(750, 200));

        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("modified employee");
                modifyEmployee(employeeId);
                modifyEmployeeButtonText(buttonText);
                employeeModFrame.dispatchEvent(new WindowEvent(employeeModFrame, WindowEvent.WINDOW_CLOSING));
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("removed");
                removeEmployee(employeeId);
                removeEmployeeButton(buttonText);
                employeeModFrame.dispatchEvent(new WindowEvent(employeeModFrame, WindowEvent.WINDOW_CLOSING));
            }
        });

        // add necessary items to panel
        employeePanelModify.add(employeeIdLabel);
        employeePanelModify.add(employeeIdValue);
        employeePanelModify.add(hoursWorkedLabel);
        employeePanelModify.add(hoursWorkedValue);
        employeePanelModify.add(managerLabel);
        employeePanelModify.add(managerValue);
        employeePanelModify.add(passwordLabel);
        employeePanelModify.add(passwordValue);

        employeePanelModify.add(modifyButton);
        employeePanelModify.add(removeButton);

        // Add the panels to the frame
        employeeModFrame.add(employeePanelModify);

        // Make sure the frame automatically resizes based on its content
        employeeModFrame.setSize(750, 500); // Set an initial size for the frame
        employeeModFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        employeeModFrame.setVisible(true);
    }

    /**
     * Creates a window used for picking dates used in the product usage chart
     * 
     * @author zach
     */
    public void getDatesWindow() {
        JFrame dateFrame = new JFrame("pick dates");

        // change layout frame
        dateFrame.setLayout(new BorderLayout());

        // gui stuff
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
        datePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton graphButton = new JButton("graph item");
        JLabel date1Label = new JLabel("first date in YYYY-MM-DD format:");
        JTextField date1Value = new JTextField("");
        JLabel date2Label = new JLabel("second date in YYYY-MM-DD format:");
        JTextField date2Value = new JTextField("");

        date1Label.setMinimumSize(new Dimension(200, 30));
        date1Value.setMaximumSize(new Dimension(200, 30));
        date2Label.setMinimumSize(new Dimension(200, 30));
        date2Value.setMaximumSize(new Dimension(200, 30));

        datePanel.setPreferredSize(new Dimension(750, 200));

        graphButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("add dates");
                dateFrame.dispatchEvent(new WindowEvent(dateFrame, WindowEvent.WINDOW_CLOSING));

                if (date1Value.getText().equals("")) {
                    date1Value.setText("2024-05-22");
                }
                if (date2Value.getText().equals("")) {
                    date2Value.setText("2025-02-18");
                }

                String[] str = { date1Value.getText(), date2Value.getText() };
                BarGraph.main(str);
            }
        });

        // add necessary items to panel
        datePanel.add(date1Label);
        datePanel.add(date1Value);
        datePanel.add(date2Label);
        datePanel.add(date2Value);

        datePanel.add(graphButton);

        // Add the panels to the frame
        dateFrame.add(datePanel);

        // Make sure the frame automatically resizes based on its content
        dateFrame.setSize(750, 500); // Set an initial size for the frame
        dateFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dateFrame.setVisible(true);
    }

    /**
     * Modifies inventory button text from the array of buttons for gui
     * 
     * @author zach
     * @param originalButtonText the old button text before the query was ran
     */
    public void modifyInventoryButtonText(String originalButtonText) {
        String newButtonText = ingredientValue.getText() + " amount: " + amountValue.getText();

        for (int i = 0; i < inventoryButtons.size(); i++) {
            if (inventoryButtons.get(i).getText() == originalButtonText) {
                inventoryButtons.get(i).setText(newButtonText);
            }
        }
    }

    /**
     * Modifies employee button text from the array of buttons for gui
     * 
     * @author zach
     * @param originalButtonText the old button text before the query was ran
     */
    public void modifyEmployeeButtonText(String originalButtonText) {
        String newButtonText = "employee #" + employeeIdValue.getText() + " worked: " + hoursWorkedValue.getText()
                + ", manager access: " + managerValue.getText();

        for (int i = 0; i < employeeButtons.size(); i++) {
            if (employeeButtons.get(i).getText() == originalButtonText) {
                employeeButtons.get(i).setText(newButtonText);
            }
        }
    }

    /**
     * Removes specified inventory button from gui
     * 
     * @author zach
     * @param originalButtonText the old button text before the query was ran
     */
    public void removeInventoryButton(String originalButtonText) {
        for (int i = 0; i < inventoryButtons.size(); i++) {
            if (inventoryButtons.get(i).getText() == originalButtonText) {
                inventoryPanel.remove(inventoryButtons.get(i));
                inventoryButtons.remove(i);
            }
        }

        System.out.println("new size: " + inventoryButtons.size());
    }

    /**
     * Removes specified employee button from gui
     * 
     * @author zach
     * @param originalButtonText the old button text before the query was ran
     */
    public void removeEmployeeButton(String originalButtonText) {
        for (int i = 0; i < employeeButtons.size(); i++) {
            if (employeeButtons.get(i).getText() == originalButtonText) {
                employeeButtonPanel.remove(employeeButtons.get(i));
                employeeButtons.remove(i);
            }
        }

        System.out.println("new size: " + employeeButtons.size());
    }

    // database establishment and banishment

    /**
     * Connects to database when called
     * 
     * @author zach
     */
    public void connectToDatabase() {
        dbSetup dbInfo = new dbSetup();

        String database_name = "team_24_db";
        String database_user = dbInfo.user;
        String database_password = dbInfo.pswd;
        String database_url = String.format("jdbc:postgresql://csce-315-db.engr.tamu.edu/%s", database_name);
        try {
            conn = DriverManager.getConnection(database_url, database_user, database_password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        JOptionPane.showMessageDialog(null, "Opened database successfully");
    }

    /**
     * Closes connection to database when called
     * 
     * @author zach
     */
    public void closeDataBase() {
        try {
            conn.close();
            JOptionPane.showMessageDialog(null, "Connection Closed.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection NOT Closed.");
        }
    }

    // query functions

    /**
     * Gets all current inventory items and attributes from database
     * 
     * @author zach
     */
    public void getInventoryInfo() {
        try {
            // create a statement object
            Statement stmt = conn.createStatement();

            // create a SQL statement
            String sqlStatement = "SELECT * FROM inventory_items";

            // send statement to DBMS
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next()) {
                String ingredient = result.getString("ingredient");
                int amount = result.getInt("amount");
                inventoryText.add(ingredient + " amount: " + String.valueOf(amount));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }

    /**
     * Gets all current employee attributes from database
     * 
     * @author zach
     */
    public void getEmployeeInfo() {
        try {
            // create a statement object
            Statement stmt = conn.createStatement();

            // create a SQL statement
            String sqlStatement = "SELECT * FROM employees";

            // send statement to DBMS
            ResultSet result = stmt.executeQuery(sqlStatement);
            while (result.next()) {
                int employeeIdNumber = result.getInt("employee_id");
                double hoursWorked = result.getDouble("weekly_hours_worked");
                boolean managerAccess = result.getBoolean("manager_access");
                employeeText.add("employee #" + employeeIdNumber + " worked: " + String.valueOf(hoursWorked)
                        + " hours, manager: " + String.valueOf(managerAccess));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }

    /**
     * Gets specified ingredient's information from database
     * 
     * @author zach
     * @param oldIngredientName ingredient name from button text
     */
    public void getInventoryInfoFromIngredient(String oldIngredientName) {
        try {
            // create a statement object
            Statement stmt = conn.createStatement();

            // create a SQL statement
            String sqlStatement = "SELECT * FROM inventory_items where ingredient = " + "'" + oldIngredientName + "'";

            // send statement to DBMS
            ResultSet result = stmt.executeQuery(sqlStatement);

            if (result.next()) {
                ingredientValue.setText(result.getString("ingredient"));
                amountValue.setText(result.getString("amount"));
                costPerAmountValue.setText(result.getString("cost_per_amount"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }

    /**
     * Gets specified employee's information from database
     * 
     * @author zach
     * @param oldemployeeId employee id from button text
     */
    public void getEmployeeInfoFromId(String oldemployeeId) {
        try {
            // create a statement object
            Statement stmt = conn.createStatement();

            // create a SQL statement
            String sqlStatement = "SELECT * FROM employees where employee_id = " + oldemployeeId;

            // send statement to DBMS
            ResultSet result = stmt.executeQuery(sqlStatement);

            if (result.next()) {
                employeeIdValue.setText(result.getString("employee_id"));
                hoursWorkedValue.setText(result.getString("weekly_hours_worked"));
                managerValue.setText(result.getString("manager_access"));

                if (managerValue.getText() == "t") {
                    managerValue.setText("true");
                } else {
                    managerValue.setText("false");
                }

                passwordValue.setText(result.getString("password"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }

    /**
     * Adds inventory item to database from user input and adds new button to panel
     * 
     * @author zach
     */
    public void addInventory() {
        try {
            // create a statement object
            Statement stmt = conn.createStatement();

            // get max id number for adding new inventory item
            String getMaxItemId = "select * from inventory_items where item_id = (select max(item_id) from inventory_items)";
            ResultSet result = stmt.executeQuery(getMaxItemId);

            if (result.next()) {
                int itemId = result.getInt("item_id") + 1;
                String ingredient = ingredientValue.getText();
                String amount = amountValue.getText();
                String cost = costPerAmountValue.getText();

                String insertStatement = "insert into inventory_items (item_id, ingredient, amount, cost_per_amount) VALUES ("
                        + itemId + ", '" + ingredient + "', " + amount + ", " + cost + ")";

                try {
                    ResultSet insertResult = stmt.executeQuery(insertStatement);
                } catch (Exception e) {
                    System.err.println("Some kind of database or execute query error");
                }
            }

            // Add buttons to the inventory panel
            String newButtonText = ingredientValue.getText() + " amount: " + amountValue.getText();
            JButton button = new JButton(newButtonText);

            button.setMinimumSize(new Dimension(200, 200));
            button.setMaximumSize(new Dimension(500, 200));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("modify item");
                    modifyInventoryWindow(button.getText());
                }
            });

            inventoryButtons.add(button);
            inventoryPanel.add(button);

        } catch (Exception e) {
            System.err.println("Some kind of database or execute query error");
        }
    }

    /**
     * Adds employee to database from user input and adds new button to panel
     * 
     * @author zach
     */
    public void addEmployee() {
        try {
            // create a statement object
            Statement stmt = conn.createStatement();

            // select max id from database
            String getMaxEmployeeId = "select * from employees where employee_id = (select max(employee_id) from employees)";
            ResultSet result = stmt.executeQuery(getMaxEmployeeId);

            if (result.next()) {
                int employeeIdNumber = result.getInt("employee_id") + 1;
                employeeIdValue.setText(String.valueOf(employeeIdNumber));
                String employeeHours = hoursWorkedValue.getText();
                String managerVal = managerValue.getText();
                String passwordVal = passwordValue.getText();

                String insertStatement = "INSERT INTO employees (employee_id, weekly_hours_worked, manager_access, password) VALUES ("
                        +
                        employeeIdNumber + ", " + employeeHours + ", " + managerVal + ", '" + passwordVal + "')";

                try {
                    ResultSet insertResult = stmt.executeQuery(insertStatement);
                } catch (Exception e) {
                    System.err.println("Some kind of database or execute query error");
                }
            }

            // Add buttons to the employee panel
            String newButtonText = "employee #" + employeeIdValue.getText() + " worked: " + hoursWorkedValue.getText()
                    + ", manager access: " + managerValue.getText();
            JButton button = new JButton(newButtonText);

            button.setMinimumSize(new Dimension(200, 200));
            button.setMaximumSize(new Dimension(500, 200));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("modify employee");
                    modifyEmployeeWindow(button.getText());
                }
            });

            employeeButtons.add(button);
            employeeButtonPanel.add(button);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }

    /**
     * Modifies inventory item specified of an already existing item in database
     * 
     * @author zach
     * @param originalIngredientName the ingredient name from button text
     */
    public void modifyInventory(String originalIngredientName) {
        try {
            Statement stmt = conn.createStatement();

            String ingredient = ingredientValue.getText();
            String amount = amountValue.getText();
            String cost = costPerAmountValue.getText();

            String update = "UPDATE inventory_items set ingredient = '" + ingredient + "', amount = " + amount
                    + ", cost_per_amount = " + cost + " where ingredient = '" + originalIngredientName + "'";
            ResultSet result = stmt.executeQuery(update);
        } catch (Exception e) {
            System.err.println("Some kind of database or execute query error");
        }
    }

    /**
     * Modifies employee specified of an already existing item in database
     * 
     * @author zach
     * @param originalEmployeeId the employee id from button text
     */
    public void modifyEmployee(String originalEmployeeId) {
        try {
            Statement stmt = conn.createStatement();

            String id = employeeIdValue.getText();
            String hours = hoursWorkedValue.getText();
            String manager = managerValue.getText();
            String password = passwordValue.getText();

            String update = "UPDATE employees SET employee_id = " + id + ", weekly_hours_worked = " + hours
                    + ", manager_access = " + manager + ", password = '" + password + "' where employee_id = "
                    + originalEmployeeId;
            ResultSet result = stmt.executeQuery(update);
        } catch (Exception e) {
            System.err.println("Some kind of database or execute query error");
        }
    }

    /**
     * Removes inventory item specified of an already existing item in database
     * 
     * @author zach
     * @param originalIngredientName the ingredient name from button text
     */
    public void removeInventory(String originalIngredientName) {
        try {
            Statement stmt = conn.createStatement();
            String remove = "DELETE FROM inventory_items WHERE ingredient='" + originalIngredientName + "'";
            ResultSet result = stmt.executeQuery(remove);
        } catch (Exception e) {
            System.err.println("Some kind of database or execute query error");
        }
    }

    /**
     * Removes employee specified of an already existing item in database
     * 
     * @author zach
     * @param originalEmployeeId the employee id from button text
     */
    public void removeEmployee(String originalEmployeeId) {
        try {
            Statement stmt = conn.createStatement();
            String remove = "DELETE FROM employees WHERE employee_id = " + originalEmployeeId;
            ResultSet result = stmt.executeQuery(remove);
        } catch (Exception e) {
            System.err.println("Some kind of database or execute query error");
        }
    }

    /**
     * Generates and displays the X-Report for today's sales performance.
     *
     * <p>The method retrieves sales data from the database, processes it, and 
     * formats it into a readable report displayed in a dialog.</p>
     *
     * 
     * @author Vikrum, Anuraag
     */
    public void XReport() {
        LocalTime start = LocalTime.of(11, 0); // 11 AM
        LocalTime end = LocalTime.of(23, 0); // 11 PM
        LocalTime currentTime = LocalTime.now();

        // Calculate elapsed hours from 11 AM to current time
        int elapsedHours = currentTime.getHour() - start.getHour() + 1;

        // If the current hour is not completed, exclude it from the report
        if (currentTime.getMinute() != 0)
            --elapsedHours;

        salesPerHour.clear();
        revenuePerHour.clear();

        // Database query to fetch sales and revenue data for the current date (within
        // working hours)
        try {
            Statement stmt = conn.createStatement();
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String sql = "SELECT EXTRACT(HOUR FROM TO_TIMESTAMP(time, 'HH24:MI:SS')) AS sale_hour, " +
                    "COUNT(*) AS sales, SUM(cost + tip) AS total_revenue " +
                    "FROM orders " +
                    "WHERE date = '" + currentDate + "' " +
                    "AND EXTRACT(HOUR FROM TO_TIMESTAMP(time, 'HH24:MI:SS')) BETWEEN 11 AND " + currentTime.getHour()
                    + " " +
                    "GROUP BY sale_hour ORDER BY sale_hour";

            ResultSet result = stmt.executeQuery(sql);

            // Collect sales and revenue data for each hour from 11 AM to current time
            while (result.next()) {
                int hour = result.getInt("sale_hour");
                int sales = result.getInt("sales");
                double totalRevenue = result.getDouble("total_revenue");
                salesPerHour.put(hour, sales);
                revenuePerHour.put(hour, totalRevenue);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error fetching sales data.");
            return;
        }

        // Prepare the report using string concatenation
        StringBuilder report = new StringBuilder("X-Report for Today:\n");
        for (int hour = 11; hour < 11 + elapsedHours; ++hour) {
            int sales = salesPerHour.getOrDefault(hour, 0);
            double revenue = revenuePerHour.getOrDefault(hour, 0.0);
            report.append(String.format("%2d:00 - %2d:00 : %d sales, $%.2f revenue\n", hour, hour + 1, sales, revenue));
        }

        // Display the report in a new window or dialog box
        JDialog xReportDialog = new JDialog((Frame) null, "X-Report", false);
        xReportDialog.setSize(400, 400);
        xReportDialog.setLayout(new BorderLayout());

        if (xReportTextArea == null) {
            xReportTextArea = new JTextArea(report.toString());
            xReportTextArea.setEditable(false);
        } else {
            xReportTextArea.setText(report.toString());
        }

        JButton resetButton = new JButton("Reset X-Report");
        resetButton.addActionListener(e -> resetXReport());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resetButton);

        xReportDialog.add(new JScrollPane(xReportTextArea), BorderLayout.CENTER);
        xReportDialog.add(buttonPanel, BorderLayout.SOUTH);

        xReportDialog.setVisible(true);
    }

     /**
     * Resets the X-Report for today's sales performance.
     * 
     * @author Anuraag
     */
    public void resetXReport() {
        LocalTime start = LocalTime.of(11, 0); // 11 AM
        LocalTime end = LocalTime.of(23, 0); // 11 PM
        LocalTime currentTime = LocalTime.now();
        final int elapsedHours;
        int tempElapsedHours = currentTime.getHour() - start.getHour() + 1;
        if (currentTime.getMinute() != 0)
            --tempElapsedHours;
        elapsedHours = tempElapsedHours;

        SwingUtilities.invokeLater(() -> {
            System.out.println("Resetting X-Report Data...");

            // Clear sales and revenue data
            salesPerHour.clear();
            revenuePerHour.clear();

            // Update report display
            String report = "X-Report for Today: \n";
            for (int hour = 11; hour < 11 + elapsedHours; ++hour) {
                report += String.format("%2d:00 - %2d:00 : %d sales, $%.2f revenue\n", hour, hour + 1, 0, 0.0);
            }

            // Show updated report
            if (xReportTextArea != null) {
                xReportTextArea.setText(report.toString()); // Update text in dialog
            }

            System.out.println("X-Report reset completed.");
        });
    }

    /**
     * Displays the Z-Report for today's end of day sales performance and analytics.
     * 
     * <p>The method retrieves sales data from the database and formats it using 
     * another function and then displays it as a readable report shown in a dialog.</p>
     *
     * @author Anuraag
     */
    public void showZReportDialog() {
        JDialog zReportDialog = new JDialog(managerFrame, "Z-Report", true);
        zReportDialog.setSize(600, 600);
        zReportDialog.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Z-Report", SwingConstants.CENTER);
        searchBar = new JTextArea(2, 10);
        searchBar.setEditable(false);
        searchBar.setText("Z-Report Conducted on " + testDate.toString());

        tableModel = new DefaultTableModel(new String[] { "Total Cost ($)", "Items Sold" }, 0);
        ordersTable = new JTable(tableModel);
        ordersTable.getTableHeader().setVisible(false);
        ordersTable.setDefaultEditor(Object.class, null);

        teaTableModel = new DefaultTableModel(new String[] { "Tea Type", "Quantity Sold" }, 0);
        teaOrdersTable = new JTable(teaTableModel);
        teaOrdersTable.setDefaultEditor(Object.class, null);

        zReportloadDataFromDatabase();
        //scheduleEndOfDayReset();

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        JScrollPane teaScrollPane = new JScrollPane(teaOrdersTable);

        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
        tablesPanel.add(scrollPane);
        tablesPanel.add(new JLabel("Total Amount of Each Tea Type and Flavor Sold", SwingConstants.CENTER));
        tablesPanel.add(teaScrollPane);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> zReportDialog.dispose());

        JButton resetButton = new JButton("Reset Z-Report");
        resetButton.addActionListener(e -> resetTables());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        buttonPanel.add(resetButton);

        zReportDialog.add(titleLabel, BorderLayout.NORTH);
        zReportDialog.add(searchBar, BorderLayout.NORTH);
        zReportDialog.add(tablesPanel, BorderLayout.CENTER);
        zReportDialog.add(buttonPanel, BorderLayout.SOUTH);

        zReportDialog.setVisible(true);
    }

    /**
     * Retrieves sales data from the database, processes it, and 
     * formats it to be ready for the Z-Report for today's end of day sales performance and analytics.
     *
     * <p>The method retrieves sales data from the database, processes it, and 
     * formats and is used by calling this function.</p>
     * 
     * @author Anuraag
     */
    private void zReportloadDataFromDatabase() {
        try {
            Statement stmt = conn.createStatement();
            String dateString = testDate.toString();

            searchBar.setText("Z-Report Conducted on " + dateString);

            String checkDateQuery = "SELECT COUNT(*) AS count FROM orders WHERE date = '" + dateString + "'";
            ResultSet dateCheckResult = stmt.executeQuery(checkDateQuery);
            dateCheckResult.next();
            int recordCount = dateCheckResult.getInt("count");

            // Clear table before inserting new values
            tableModel.setRowCount(0);
            teaTableModel.setRowCount(0);

            if (recordCount == 0) {
                tableModel.addRow(new Object[] { "Total Cost ($)", 0.0 });
                tableModel.addRow(new Object[] { "Total Tip ($)", 0.0 });
                tableModel.addRow(new Object[] { "Items Sold", 0 });
                tableModel.addRow(new Object[] { "Total Addons Used", 0.0 });

                String[] teaTypes = { "Milk Tea", "Ice Blended Tea", "Brewed Tea", "Fruit Tea", "Fresh Milk",
                        "Tea Mojito", "Chocolate", "Vanilla", "Strawberry", "Blueberry", "Banana" };
                for (String tea : teaTypes) {
                    teaTableModel.addRow(new Object[] { tea, 0 });
                }
            }

            else {
                String sqlStatement = "SELECT " +
                        "SUM(cost) AS total_cost, " +
                        "SUM(tip) AS total_tip, " +
                        "SUM(quantity) AS item_sold, " +
                        "SUM(addon_price) AS total_addon_cost, " +
                        "date, " +
                        "SUM(CASE WHEN tea_type = 'Milk Tea' THEN quantity ELSE 0 END) AS Milk_Tea, " +
                        "SUM(CASE WHEN tea_type = 'Ice Blended Tea' THEN quantity ELSE 0 END) AS Ice_Blended_Tea, " +
                        "SUM(CASE WHEN tea_type = 'Brewed Tea' THEN quantity ELSE 0 END) AS Brewed_Tea, " +
                        "SUM(CASE WHEN tea_type = 'Fruit Tea' THEN quantity ELSE 0 END) AS Fruit_Tea, " +
                        "SUM(CASE WHEN tea_type = 'Fresh Milk' THEN quantity ELSE 0 END) AS Fresh_Milk, " +
                        "SUM(CASE WHEN tea_type = 'Tea Mojito' THEN quantity ELSE 0 END) AS Tea_Mojito, " +
                        "SUM(CASE WHEN flavor = 'chocolate' THEN quantity ELSE 0 END) AS chocolate, " +
                        "SUM(CASE WHEN flavor = 'vanilla' THEN quantity ELSE 0 END) AS vanilla, " +
                        "SUM(CASE WHEN flavor = 'strawberry' THEN quantity ELSE 0 END) AS strawberry, " +
                        "SUM(CASE WHEN flavor = 'blueberry' THEN quantity ELSE 0 END) AS blueberry, " +
                        "SUM(CASE WHEN flavor = 'banana' THEN quantity ELSE 0 END) AS banana " +
                        "FROM orders " +
                        "WHERE date = '" + dateString + "'" +
                        "GROUP BY date";
                // send statement to DBMS
                ResultSet result = stmt.executeQuery(sqlStatement);

                while (result.next()) {
                    String date = result.getString("date");
                    // searchBar.append(date);

                    double totalCost = result.getDouble("total_cost");
                    double totalTip = result.getDouble("total_tip");
                    double itemsSold = result.getDouble("item_sold");
                    double totalAddonUsed = result.getDouble("total_addon_cost") / 0.75;

                    double milkTea = result.getDouble("Milk_Tea");
                    double iceBlendedTea = result.getDouble("Ice_Blended_Tea");
                    double brewedTea = result.getDouble("Brewed_Tea");
                    double fruitTea = result.getDouble("Fruit_Tea");
                    double freshMilk = result.getDouble("Fresh_Milk");
                    double teaMojito = result.getDouble("Tea_Mojito");

                    double chocolate = result.getDouble("chocolate");
                    double vanilla = result.getDouble("vanilla");
                    double strawberry = result.getDouble("strawberry");
                    double blueberry = result.getDouble("blueberry");
                    double banana = result.getDouble("banana");

                    // tableModel.addRow(new Object[]{totalCost, itemsSold, date, time, menuItem});
                    // tableModel.addRow(new Object[]{totalCost, itemsSold});
                    tableModel.addRow(new Object[] { "Total Cost ($)", totalCost });
                    tableModel.addRow(new Object[] { "Total Tip ($)", totalTip });
                    tableModel.addRow(new Object[] { "Items Sold", itemsSold });
                    tableModel.addRow(new Object[] { "Total Addons Used", totalAddonUsed });

                    teaTableModel.addRow(new Object[] { "Milk Tea", milkTea });
                    teaTableModel.addRow(new Object[] { "Ice Blended Tea", iceBlendedTea });
                    teaTableModel.addRow(new Object[] { "Brewed Tea", brewedTea });
                    teaTableModel.addRow(new Object[] { "Fruit Tea", fruitTea });
                    teaTableModel.addRow(new Object[] { "Fresh Milk", freshMilk });
                    teaTableModel.addRow(new Object[] { "Tea Mojito", teaMojito });

                    teaTableModel.addRow(new Object[] { "Chocolate", chocolate });
                    teaTableModel.addRow(new Object[] { "Vanilla", vanilla });
                    teaTableModel.addRow(new Object[] { "Strawberry", strawberry });
                    teaTableModel.addRow(new Object[] { "Blueberry", blueberry });
                    teaTableModel.addRow(new Object[] { "Banana", banana });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
    }

    /**
     * Resets the Z-Report for today's sales performance.
     * 
     * @author Anuraag
     */
    private void resetTables() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Running Reset...");

            // Clear all table rows
            tableModel.setRowCount(0);
            teaTableModel.setRowCount(0);

            // Reset all values to 0
            tableModel.addRow(new Object[] { "Total Cost ($)", 0.0 });
            tableModel.addRow(new Object[] { "Total Tip ($)", 0.0 });
            tableModel.addRow(new Object[] { "Items Sold", 0 });
            tableModel.addRow(new Object[] { "Total Addons Used", 0.0 });

            String[] teaTypes = { "Milk Tea", "Ice Blended Tea", "Brewed Tea", "Fruit Tea", "Fresh Milk", "Tea Mojito",
                    "Chocolate", "Vanilla", "Strawberry", "Blueberry", "Banana" };
            for (String tea : teaTypes) {
                teaTableModel.addRow(new Object[] { tea, 0 });
            }

            // Manually set the new date to 2024-05-29
            testDate = testDate.plusDays(1);
            // searchBar.setText("Z-Report Conducted on ");

            // searchBar.setText("Z-Report Conducted on 2024-05-29");

            System.out.println("Reset Complete! Date updated to 2024-05-29.");

            zReportloadDataFromDatabase();
        });
    }

    /**
     * Function that runs the resetTables() and resetXReport() function calls
     * at the end of every working day in order to reset the X-Report and Z-Report.
     * 
     * @author Anuraag
     */
    public void scheduleEndOfDayReset() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resetTime = now.toLocalDate().atTime(22, 22); // test time today
        // LocalDateTime resetTime = now.toLocalDate().atTime(23, 0); //real value for
        // final submission

        if (now.isAfter(resetTime)) {
            resetTime = resetTime.plusDays(1);
        }

        // Store the final reset time to be used inside the lambda
        final LocalDateTime finalResetTime = resetTime;
        long delay = ChronoUnit.MILLIS.between(now, resetTime);

        scheduler.scheduleAtFixedRate(() -> {
            resetTables();
            System.out.println("Next reset scheduled for: " + finalResetTime.plusDays(1));

            resetXReport();
        }, delay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }
}
