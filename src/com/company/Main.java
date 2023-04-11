package com.company;


import javax.swing.*;

class Main {

    public static void main(String[] args) {
        // try {
        //     UIManager.setLookAndFeel("com.sun.java.swing.plaf.mac.MacLookAndFeel");
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        SwingUtilities.invokeLater(() -> new DrCorrGUI().createAndShowGUI());
    }
}
