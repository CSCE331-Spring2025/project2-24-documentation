/**
* This file is used to generate the graphical product usage report on a bar graph. This report
* describes the usage of various ingredients from the inventory between two specified points
* in time. The graph in question is a bar graph which has a label of all ingredients used.
* 
* @author zach
*/

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;


/**
* The BarGraph class extends JPanel and contains all methods needed for graphing.
* 
* @author zach
*/
class BarGraph extends JPanel {
    private ArrayList<Integer> data;
    private ArrayList<String> labels;
    private Connection conn = null;

    // need linkedhashmap so that the order of inventory items is always consistent on the graph
    LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<>(15);

    /**
     * Constructor used to create BarGraph object
     * 
     * @author zach
     */
    public BarGraph() {
        this.data = new ArrayList<Integer>(15);
        this.labels = new ArrayList<String>(15);
    }

    @Override
    /**
     * Graphs bar graph using data that was collected from database
     * 
     * @author zach
     * @param g Graphics object used to display graph data
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.isEmpty()) {
            return;
        }

        int barWidth = getWidth() / data.size() - 12;
        int x = 10;
        int xMinData = 10;
        int xMaxData = 10;
        int minDataValue = Collections.min(data);
        int maxDataValue = Collections.max(data);
        int maxHeight = getHeight() - 50;
        ArrayList<Color> colors = new ArrayList<Color>(3);

        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        colors.add(Color.PINK);

        for (int i = 0; i < data.size(); i++) {
            int barHeight = (int) ((double) data.get(i) / maxDataValue * maxHeight);
            
            // change bar color
            g.setColor(colors.get(i % 4));

            // draw bar graph bar
            g.fillRect(x, getHeight() - barHeight - 20, barWidth, barHeight);

            // change label color
            g.setColor(Color.BLACK);

            // draw label text below bars
            g.drawString(labels.get(i), x, getHeight() - 5);
            
            if (data.get(i) == minDataValue) {
                xMinData = x;
            }
            else if (data.get(i) == maxDataValue)
            {
                xMaxData = x;
            }

            x += barWidth + 10;
        }

        // calculate location of max and min datavalues in window
        int yMaxValue = (int) ((double) Collections.max(data) / maxDataValue * maxHeight);
        int yMinValue = (int) ((double) minDataValue / maxDataValue * maxHeight);
        
        g.drawString("max amount: " + maxDataValue, xMaxData, getHeight() - yMaxValue - 20);
        g.drawString("min amount: " + minDataValue, xMinData, getHeight() - yMinValue - 20);
    }

    /**
     * Connects to database when called
     * 
     * @author zach
     */
    public void connectToDatabase() {
        // Building the connection
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
    }

    /**
     * Closes connection to database when called
     * 
     * @author zach
     */
    public void closeDataBase() {

        // closing the connection
        try {
            conn.close();

            // JOptionPane.showMessageDialog(null, "Connection Closed.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection NOT Closed.");
        }
    }

    /**
     * Gets the names of all ingredients in inventory
     * 
     * @author zach
     */
    public void getInventoryNames() {
        connectToDatabase();
        try {
            // create a statement object
            Statement stmt = conn.createStatement();
            
            // create a SQL statement
            String sqlStatement = "select ingredient from inventory_items order by item_id";
            
            // send statement to DBMS
            ResultSet result = stmt.executeQuery(sqlStatement);
            
            while (result.next()) {
                this.labels.add(result.getString("ingredient"));
                linkedHashMap.put(result.getString("ingredient"), 0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }
        closeDataBase();
    }

    /**
     * Gets all sums of use of inventory items between two dates from db using queries
     * 
     * @author zach
     * @param firstDate the starting date for selecting data
     * @param secondDate the ending date for selecting data
     */
    public void getInventoryUsedBetweenDates(String firstDate, String secondDate) {
        connectToDatabase();
        try {
           Statement stmt = conn.createStatement();

           // create SQL statements
           String flavorSum = "select distinct orders.flavor, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' group by orders.flavor";
           String teaTypeSum = "select distinct orders.tea_type, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' group by orders.tea_type";
           String sugarSum = "select distinct menu_item.sugar_level, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' group by menu_item.sugar_level";
           String iceSum = "select distinct menu_item.ice_level, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' group by menu_item.ice_level";
           String pearlsSum = "select distinct addon.pearls, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' and addon.pearls = TRUE group by addon.pearls";
           String puddingSum = "select distinct addon.pudding, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' and addon.pudding = TRUE group by addon.pudding";
           String jellySum = "select distinct addon.jelly, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' and addon.jelly = TRUE group by addon.jelly";
           String iceCreamSum = "select distinct addon.ice_cream, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' and addon.ice_cream = TRUE group by addon.ice_cream";
           String creamaSum = "select distinct addon.creama, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' and addon.creama = TRUE group by addon.creama";
           String bobaSum = "select distinct addon.boba, SUM(orders.quantity) from orders inner join addon on orders.order_number = addon.item_id inner join menu_item on orders.order_number = menu_item.item_id where orders.date between '" + firstDate + "' and '" + secondDate + "' and addon.boba = TRUE group by addon.boba";
           
            // send statement to DBMS
            try {
                ResultSet flavorResult = stmt.executeQuery(flavorSum);

                while (flavorResult.next()) {
                    String inventoryFlavor = flavorResult.getString("flavor");

                    if (inventoryFlavor.equals("strawberry"))
                    {
                        inventoryFlavor = "strawberries";
                    }
                    else if (inventoryFlavor.equals("blueberry"))
                    {
                        inventoryFlavor = "blueberries";
                    }
                    else if (inventoryFlavor.equals("chocolate"))
                    {
                        inventoryFlavor = "cocoa powder";
                    }
                    else if (inventoryFlavor.equals("vanilla"))
                    {
                        inventoryFlavor = "vanilla extract";
                    }

                    linkedHashMap.put(inventoryFlavor, flavorResult.getInt("sum"));
                }
            }
            catch (Exception e) {

            }
            
            try {
                ResultSet teaTypeResult = stmt.executeQuery(teaTypeSum);

                while (teaTypeResult.next()) {
                    String inventoryTeaType = teaTypeResult.getString("tea_type");

                    if (inventoryTeaType.equals("Milk Tea")) {
                        
                        //since this drink uses both milk and tea, we need to add to hashmap milk and tea values
                        linkedHashMap.put("milk", linkedHashMap.get("milk") + teaTypeResult.getInt("sum"));
                        linkedHashMap.put("tea", linkedHashMap.get("tea") + teaTypeResult.getInt("sum"));
                        continue;
                    }
                    else if (inventoryTeaType.contains("Tea")) {
                        inventoryTeaType = "tea";
                    }
                    else if (inventoryTeaType.contains("Milk")) {
                        inventoryTeaType = "milk";
                    }

                    linkedHashMap.put(inventoryTeaType, linkedHashMap.get(inventoryTeaType) + teaTypeResult.getInt("sum"));
                }
            }
            catch (Exception e) {

            }

            try {
                ResultSet sugarResult = stmt.executeQuery(sugarSum);

                while (sugarResult.next()) {
                    String sugarType = sugarResult.getString("sugar_level");
                    int sugarLevelMultiplier = 0;

                    if (sugarType.equals("30% sugar")) {
                        sugarLevelMultiplier = 1;
                    }
                    else if (sugarType.equals("50% sugar")) {
                        sugarLevelMultiplier = 2;
                    }
                    else if (sugarType.equals("80% sugar")) {
                        sugarLevelMultiplier = 3;
                    }
                    else if (sugarType.equals("100% sugar")) {
                        sugarLevelMultiplier = 4;
                    }
                    else {
                        continue;
                    }

                    sugarType = "sugar";
                    linkedHashMap.put(sugarType, linkedHashMap.get(sugarType) + (sugarResult.getInt("sum") * sugarLevelMultiplier));
                }
            }
            catch (Exception e) {

            }

            try {
                ResultSet iceResult = stmt.executeQuery(iceSum);

                while (iceResult.next()) {
                    String iceType = iceResult.getString("ice_level");
                    int iceMultiplier = 0;

                    if (iceType.equals("light")) {
                        iceMultiplier = 1;
                    }
                    else if (iceType.equals("regular")) {
                        iceMultiplier = 2;
                    }
                    else if (iceType.equals("extra")) {
                        iceMultiplier = 3;
                    }
                    else {
                        continue;
                    }
                    
                    linkedHashMap.put("ice", linkedHashMap.get("ice") + (iceResult.getInt("sum") * iceMultiplier));
                }
            }
            catch (Exception e) {

            }

            try {
                ResultSet pearlsResult = stmt.executeQuery(pearlsSum);
                
                while (pearlsResult.next()) {
                    linkedHashMap.put("pearls", pearlsResult.getInt("sum"));
                }
            }
            catch (Exception e) {

            }

            try {
                ResultSet puddingResult = stmt.executeQuery(puddingSum);
                
                while (puddingResult.next()) {
                    linkedHashMap.put("pudding", puddingResult.getInt("sum"));
                }
            }
            catch (Exception e) {

            }

            try {
                ResultSet jellyResult = stmt.executeQuery(jellySum);
                
                while (jellyResult.next()) {
                    linkedHashMap.put("jelly", jellyResult.getInt("sum"));
                }
            }
            catch (Exception e) {

            }

            try {
                ResultSet iceCreamResult = stmt.executeQuery(iceCreamSum);
                
                while (iceCreamResult.next()) {
                    linkedHashMap.put("ice cream", iceCreamResult.getInt("sum"));
                }
            }
            catch (Exception e) {

            }

            try {
                ResultSet creamaResult = stmt.executeQuery(creamaSum);
                
                while (creamaResult.next()) {
                    linkedHashMap.put("creama", creamaResult.getInt("sum"));
                }
            }
            catch (Exception e) {

            }

            try {
                ResultSet bobaResult = stmt.executeQuery(bobaSum);
                
                while (bobaResult.next()) {
                    linkedHashMap.put("boba", bobaResult.getInt("sum"));
                }
            }
            catch (Exception e) {

            }
        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database.");
        }

        for (Map.Entry<String, Integer> entry : linkedHashMap.entrySet()) {
            this.data.add(entry.getValue());
        }

        System.out.println("size::: " + data.size());

        closeDataBase();
    }

    /*
     * Runs the graphing functions and displays it
     * 
     * @author zach
     * @param args used to transfer dates from manager.java to this file for queries
     */
    public static void main(String[] args) {
        BarGraph graph = new BarGraph();

        graph.getInventoryNames();
        graph.getInventoryUsedBetweenDates(args[0], args[1]);

        for (Map.Entry<String, Integer> entry : graph.linkedHashMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key + ": " + value);
        }

        System.out.println(graph.data.size());
        System.out.println(graph.labels.size());
        System.out.println(args[0]);
        System.out.println(args[1]);

        JFrame frame = new JFrame("Bar Graph Example");
        frame.add(graph);
        frame.pack();
        frame.setSize(1270, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
}