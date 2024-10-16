package main.java;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.InetAddress;


public class WeighingSystemControl extends JFrame {

    private TCPMasterConnection connection = null;
    private ModbusTCPTransaction transaction = null;
    private InetAddress address = null;
    private int port = Modbus.DEFAULT_PORT;

    private JLabel connectionStatusLabel;
    private JButton connectButton;
    private JTextField carNumberField = new JTextField(8);
    private JTextField coalWeighField = new JTextField(8);

    private boolean isConnected = false;

    public WeighingSystemControl() {
        setTitle("Управление системой автовесов");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создание элементов интерфейса
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(6, 1));

        // Верхняя панель для подключения
        JPanel connectionPanel = new JPanel();
        connectButton = new JButton("Подключиться");
        connectionStatusLabel = new JLabel("Статус: Отключено");

        connectionPanel.add(connectButton);
        connectionPanel.add(connectionStatusLabel);

        // Создание кнопок для управления
        JButton btnOpenGate1 = new JButton("Открыть Шлагбаум 1");
        JButton btnCloseGate1 = new JButton("Закрыть Шлагбаум 1");
        JButton btnOpenGate2 = new JButton("Открыть Шлагбаум 2");
        JButton btnCloseGate2 = new JButton("Закрыть Шлагбаум 2");

        JLabel carNumberLabel = new JLabel("Car number: ");

        carNumberField.setEditable(false);
        carNumberField.setFocusable(false);

        JLabel coalWeighLabel = new JLabel("Coal Weigh: ");
        coalWeighField.setEditable(false);
        coalWeighField.setFocusable(false);


        btnOpenGate1.setEnabled(false);
        btnCloseGate1.setEnabled(false);
        btnOpenGate2.setEnabled(false);
        btnCloseGate2.setEnabled(false);

        // Панель с кнопками управления
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(btnOpenGate1);
        controlPanel.add(btnCloseGate1);
        controlPanel.add(btnOpenGate2);
        controlPanel.add(btnCloseGate2);

        JPanel panelCarNumber = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCarNumber.add(carNumberLabel);
        panelCarNumber.add(carNumberField);

        JPanel coalWeighPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        coalWeighPanel.add(coalWeighLabel);
        coalWeighPanel.add(coalWeighField);


        mainPanel.add(connectionPanel, BorderLayout.NORTH);
        mainPanel.add(controlPanel, BorderLayout.CENTER);
        mainPanel.add(panelCarNumber, BorderLayout.LINE_END);
        mainPanel.add(coalWeighPanel, BorderLayout.LINE_END);

        add(mainPanel);

        // Обработчик для кнопки подключения
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    try {
                        address = InetAddress.getByName("192.168.1.5"); // Замените на IP-адрес вашего контроллера
                        connection = new TCPMasterConnection(address);
                        connection.setPort(port);
                        connection.connect();

                        isConnected = true;
                        connectionStatusLabel.setText("Статус: Подключено");
                        connectButton.setText("Отключиться");

                        btnOpenGate1.setEnabled(true);
                        btnCloseGate1.setEnabled(true);
                        btnOpenGate2.setEnabled(true);
                        btnCloseGate2.setEnabled(true);


                        carNumberField.setEditable(true);
                        carNumberField.setFocusable(true);

//
//                        coalWeighField.setEditable(true);
//                        coalWeighField.setFocusable(true);
                        startSensorMonitoring();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(WeighingSystemControl.this, "Ошибка подключения к контроллеру", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    try {
                        if (connection != null && connection.isConnected()) {
                            connection.close();
                            isConnected = false;
                            connectionStatusLabel.setText("Статус: Отключено");
                            connectButton.setText("Подключиться");

                            btnOpenGate1.setEnabled(false);
                            btnCloseGate1.setEnabled(false);
                            btnOpenGate2.setEnabled(false);
                            btnCloseGate2.setEnabled(false);

                            carNumberField.setEditable(false);
                            carNumberField.setFocusable(false);
//
//                            coalWeighField.setEditable(false);
//                            coalWeighField.setFocusable(false);

                            System.out.println("Соединение с контроллером закрыто");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        btnOpenGate1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openGate1();
            }
        });

        btnCloseGate1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeGate1();
            }
        });

        btnOpenGate2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openGate2();
            }
        });

        btnCloseGate2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeGate2();
            }
        });

        carNumberField.addActionListener(e -> {
            String carNumber = carNumberField.getText();
            if (carNumber.replace(" ", "").length() == 8) {
                openGate1();
                carNumberField.setFocusable(false);
                carNumberField.setEditable(false);

                try {
                    // TODO Bu vaqtinchalik
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                closeGate1();
            }
        });

        coalWeighField.addActionListener(e -> {
            String coalWeigh = coalWeighField.getText();
            System.out.println(coalWeigh);
            openGate2();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            closeGate2();

        });

        coalWeighField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume(); // Ignore non-digit characters
                }
            }
        });

    }

    private void openGate1() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "Нет подключения к контроллеру", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {


//            for (int i = 0; i <= 15; i++) {
//                writeCoil(i, true);
//                Thread.sleep(2000);
//                writeCoil(i, false);
//                System.out.println("i = " + i);
//            }

            writeCoil(0, true);// Включить зеленый свет для шлагбаума 1 (катушка 0)
            writeCoil(2, true); // Катушка 1 управляет открытием шлагбаума 1
            writeCoil(3, false);
            writeCoil(16, true);



//            writeCoil(8, true); // Катушка 1 управляет открытием шлагбаума 1
            // Управление светофорами для шлагбаума 1
//            writeCoil(3, true); // Выключить красный свет для шлагбаума 1 (катушка 2)
            System.out.println("Шлагбаум 1 открыт, зеленый свет включен, красный свет выключен");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeGate1() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "Нет подключения к контроллеру", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            coalWeighField.setFocusable(true);
            coalWeighField.setEditable(true);
            // Закрытие шлагбаума 1
            writeCoil(0, false); // Выключить зеленый свет для шлагбаума 1 (катушка 0)
            writeCoil(2, false); // Выключить зеленый свет для шлагбаума 1 (катушка 0)
            writeCoil(3, true); // Катушка 2 управляет закрытием шлагбаума 1
            // Управление светофорами для шлагбаума 1
//            writeCoil(1, false);  // Включить красный свет для шлагбаума 1 (катушка 2)
            System.out.println("Шлагбаум 1 закрыт, зеленый свет выключен, красный свет включен");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGate2() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "Нет подключения к контроллеру", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            coalWeighField.setEditable(false);
            coalWeighField.setFocusable(false);
            // Открытие шлагбаума 2
            writeCoil(4, true); // Катушка 4 управляет открытием шлагбаума 2
            // Управление светофорами для шлагбаума 2
            writeCoil(1, true);  // Включить зеленый свет для шлагбаума 2 (катушка 1)
            writeCoil(3, false); // Выключить красный свет для шлагбаума 2 (катушка 3)
            System.out.println("Шлагбаум 2 открыт, зеленый свет включен, красный свет выключен");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeGate2() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "Нет подключения к контроллеру", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            carNumberField.setText("");
            carNumberField.setFocusable(true);
            carNumberField.setEditable(true);

            coalWeighField.setText("");
            coalWeighField.setFocusable(false);
            coalWeighField.setEditable(false);
            // Закрытие шлагбаума 2
            writeCoil(3, true); // Катушка 3 управляет закрытием шлагбаума 2
            writeCoil(2, true); // Катушка 3 управляет закрытием шлагбаума 2
            // Управление светофорами для шлагбаума 2
            writeCoil(1, false); // Выключить зеленый свет для шлагбаума 2 (катушка 1)
            writeCoil(3, true);  // Включить красный свет для шлагбаума 2 (катушка 3)
            System.out.println("Шлагбаум 2 закрыт, зеленый свет выключен, красный свет включен");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeCoil(int coilAddress, boolean state) throws Exception {
        // Создание запроса на запись в катушку
        WriteCoilRequest request = new WriteCoilRequest(coilAddress, state);



        transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();
        ModbusResponse response = transaction.getResponse();
        if (response instanceof ExceptionResponse) {
            ExceptionResponse exceptionResponse = (ExceptionResponse) response;
            System.out.println("Ошибка при записи в катушку " + coilAddress + ": " + exceptionResponse.getMessage());
        } else {
            System.out.println("Катушка " + coilAddress + " установлена в состояние " + state);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        // Закрытие Modbus-соединения при выходе
        try {
            if (connection != null && connection.isConnected()) {
                connection.close();
                System.out.println("Соединение с контроллером закрыто");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int readSensor(int coilAddress) throws Exception {
        // Read the coil status (0 or 1)
        ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1);
        transaction = new ModbusTCPTransaction(connection);
        transaction.setRequest(request);
        transaction.execute();
        ModbusResponse response = transaction.getResponse();

        if (response instanceof ExceptionResponse) {
            ExceptionResponse exceptionResponse = (ExceptionResponse) response;
            System.out.println("Ошибка при чтении катушки " + coilAddress + ": " + exceptionResponse.getMessage());
            return -1; // Error value
        } else {
            ReadCoilsResponse readResponse = (ReadCoilsResponse) response;
            return readResponse.getCoils().getBit(0) ? 1 : 0; // Return 1 or 0
        }
    }

    private void startSensorMonitoring() {
        Timer timer = new Timer(2000, new ActionListener() { // Check every 2 seconds
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isConnected) {
                    try {
                        int sensor1Status = readSensor(15); // Assume sensor 1 is at coil 5

                        System.out.println("Sensor 1 status: " + sensor1Status);

                        // Add actions depending on sensor states
                        if (sensor1Status == 0 ) {
                            System.out.println("Person detected ");
                            openGate1();

                        } else if (sensor1Status == 1 ) {
                            System.out.println("No person detected");
                            closeGate1();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        timer.start();
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeighingSystemControl app = new WeighingSystemControl();
            app.setVisible(true);
        });
    }
}
