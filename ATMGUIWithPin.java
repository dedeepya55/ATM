import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ATMGUIWithPin extends JFrame implements ActionListener {
    private JTextField pinField;
    private JButton[] numButtons = new JButton[10];
    private String enteredPIN;
    private final String DB_URL = "jdbc:mysql://localhost:3306/newdatabases";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "dedeepya";
    
  


    
    public ATMGUIWithPin() {

        setTitle("ATM Machine");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize fields
        pinField = new JTextField(4);

        // Set up panels and buttons
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Enter PIN:"));
        topPanel.add(pinField);
        add(topPanel, BorderLayout.NORTH);

        // Numerical button panel
        JPanel numPanel = new JPanel(new GridLayout(4, 3));
        for (int i = 0; i < 9; i++) {
            numButtons[i] = new JButton(String.valueOf(i + 1));
            numButtons[i].addActionListener(this);
            numPanel.add(numButtons[i]);
        }

        // Adding zero button
        numButtons[9] = new JButton("0");
        numButtons[9].addActionListener(this);
        numPanel.add(new JButton(" ")); // Empty button
        numPanel.add(numButtons[9]);
        numPanel.add(new JButton(" ")); // Empty button

        add(numPanel, BorderLayout.CENTER);

        // Bottom panel for action buttons
        JPanel bottomPanel = new JPanel();
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(this);
        bottomPanel.add(continueButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.matches("\\d")) {
            pinField.setText(pinField.getText() + command);
        } else if (e.getActionCommand().equals("Continue")) {
            enteredPIN = pinField.getText(); // Store entered PIN
            if (validatePIN()) {
                openMainOperationsFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect PIN!");
            }
        }
    }

    private boolean validatePIN() {
        
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM atm WHERE pin = ?");
            statement.setString(1, enteredPIN);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // If resultSet has at least one row, PIN is valid
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error occurred: " + e.getMessage());
            return false;
        }
    }

    private void openMainOperationsFrame() {
        MainOperationsFrame mainOperationsFrame = new MainOperationsFrame(enteredPIN); // Pass entered PIN to MainOperationsFrame
        mainOperationsFrame.setSize(getSize());
        mainOperationsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainOperationsFrame.setVisible(true);
        dispose(); // Close the PIN entry frame
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ATMGUIWithPin().setVisible(true);
        });
    }
}

class MainOperationsFrame extends JFrame implements ActionListener {
    private String pin; // Store PIN passed from ATMGUIWithPin
    private final String DB_URL = "jdbc:mysql://localhost:3306/newdatabases";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "dedeepya";

    public MainOperationsFrame(String pin) {
        this.pin = pin; // Initialize PIN
        setTitle("ATM Operations");
        setSize(300, 200);
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton withdrawButton = new JButton("Withdraw");
        JButton depositButton = new JButton("Deposit");
        JButton balanceButton = new JButton("Check Balance");

        withdrawButton.addActionListener(this);
        depositButton.addActionListener(this);
        balanceButton.addActionListener(this);

        buttonPanel.add(withdrawButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(balanceButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Withdraw":
                new WithdrawFrame(pin);
                break;
            case "Deposit":
                new DepositFrame(pin);
                break;
            case "Check Balance":
                showBalanceAndPIN();
                break;
        }
    }

    private void showBalanceAndPIN() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM atm WHERE pin = ?");
                    statement.setString(1, pin); // Use stored PIN
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        String pin = resultSet.getString("pin");
                        double balance = resultSet.getDouble("amount");
                        JOptionPane.showMessageDialog(MainOperationsFrame.this, "Your PIN is: " + pin + "\nYour amount is: $" + balance);
                    } else {
                        JOptionPane.showMessageDialog(MainOperationsFrame.this, "Account not found.");
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(MainOperationsFrame.this, "Database error occurred: " + e.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }
}

class WithdrawFrame extends JFrame implements ActionListener {
    private final String DB_URL = "jdbc:mysql://localhost:3306/newdatabases";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "dedeepya";
    private JTextField amountField;
    private String pin; // Store PIN passed from MainOperationsFrame

    public WithdrawFrame(String pin) {
        this.pin = pin; // Initialize PIN
        setTitle("Withdraw");
        setSize(500, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        amountField = new JTextField(4);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Enter amount:"));
        topPanel.add(amountField);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel numPanel = new JPanel(new GridLayout(4, 3));
        for (int i = 0; i < 9; i++) {
            JButton button = new JButton(String.valueOf(i + 1));
            button.addActionListener(this);
            numPanel.add(button);
        }
        JButton zeroButton = new JButton("0");
        zeroButton.addActionListener(this);
        numPanel.add(new JButton(" "));
        numPanel.add(zeroButton);
        numPanel.add(new JButton(" "));
        panel.add(numPanel, BorderLayout.CENTER);

        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(this);
        panel.add(withdrawButton, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.matches("\\d")) {
            amountField.setText(amountField.getText() + command);
        } else if (command.equals("Withdraw")) {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
            } else {
                // Perform withdrawal operation
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return validateWithdrawal(amount);
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(WithdrawFrame.this, "Withdrawal successful! Amount: $" + amount);
                                dispose();
                            } else {
                                JOptionPane.showMessageDialog(WithdrawFrame.this, "Insufficient balance.");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(WithdrawFrame.this, "Error occurred: " + ex.getMessage());
                        }
                    }
                };
                worker.execute();
            }
        }
    }

    private boolean validateWithdrawal(double amount) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement statement = connection.prepareStatement("SELECT amount FROM atm WHERE pin = ?");
            statement.setString(1, pin);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double balance = resultSet.getDouble("amount");
                if (balance >= amount) {
                    // Update balance in database
                    PreparedStatement updateStatement = connection.prepareStatement("UPDATE atm SET amount = amount - ? WHERE pin = ?");
                    updateStatement.setDouble(1, amount);
                    updateStatement.setString(2, pin);
                    updateStatement.executeUpdate();
                    return true;
                } else {
                    return false; // Insufficient balance
                }
            } else {
                throw new SQLException("Account not found.");
            }
        }
    }
}

class DepositFrame extends JFrame implements ActionListener {
    private final String DB_URL = "jdbc:mysql://localhost:3306/newdatabases";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "dedeepya";
    private JTextField amountField;
    private String pin; // Store PIN passed from MainOperationsFrame

    public DepositFrame(String pin) {
        this.pin = pin; // Initialize PIN
        setTitle("Deposit");
        setSize(500, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        amountField = new JTextField(4);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Enter amount:"));
        topPanel.add(amountField);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel numPanel = new JPanel(new GridLayout(4, 3));
        for (int i = 0; i < 9; i++) {
            JButton button = new JButton(String.valueOf(i + 1));
            button.addActionListener(this);
            numPanel.add(button);
        }
        JButton zeroButton = new JButton("0");
        zeroButton.addActionListener(this);
        numPanel.add(new JButton(" "));
        numPanel.add(zeroButton);
        numPanel.add(new JButton(" "));
        panel.add(numPanel, BorderLayout.CENTER);

        JButton depositButton = new JButton("Deposit");
        depositButton.addActionListener(this);
        panel.add(depositButton, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.matches("\\d")) {
            amountField.setText(amountField.getText() + command);
        } else if (command.equals("Deposit")) {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
            } else {
                // Perform deposit operation
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        if (validateDeposit(amount)) {
                            JOptionPane.showMessageDialog(DepositFrame.this, "Deposit successful! Amount: $" + amount);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(DepositFrame.this, "Deposit failed.");
                        }
                        return null;
                    }
                };
                worker.execute();
            }
        }
    }

    private boolean validateDeposit(double amount) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement statement = connection.prepareStatement("UPDATE atm SET amount = amount + ? WHERE pin = ?");
            statement.setDouble(1, amount);
            statement.setString(2, pin);
            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error occurred: " + ex.getMessage());
            return false;
        }
    }
}
