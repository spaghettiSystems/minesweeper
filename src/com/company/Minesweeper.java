package com.company;


import com.bulenkov.darcula.DarculaLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class Minesweeper {
    static JFrame frame;
    GameLogic game;
    Timer clock;
    int seconds;
    int minutes;
    int hours;
    boolean literalBombs;
    private JTable mineSweeperTable;
    private JPanel mainPanel;
    private JButton resetButton;
    private JScrollPane tableScrollPane;
    private JLabel flagCountLabel;
    private JLabel timeCounterLabel;
    private JTabbedPane tabbedPane1;
    private JTextField rowsTextField;
    private JTextField columnsTextField;
    private JTextField bombsTextField;
    private JPanel welcomePanel;
    private JButton startButton;
    private JButton loadButton;
    private JButton optionsButton;
    private JButton exitButton;
    private JPanel cardPanel;
    private JButton chooseFileButton1;
    private JTextField saveFileTextField;
    private JButton saveButton;
    private JButton chooseFileButton;
    private JButton openButton;
    private JTextField openFileTextField;
    private JCheckBox bombValueLiteral;

    public Minesweeper() {

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int rows = Integer.parseInt(rowsTextField.getText());
                int columns = Integer.parseInt(columnsTextField.getText());
                int bombrate = Integer.parseInt(bombsTextField.getText());
                game = new GameLogic(rows, columns, bombrate, literalBombs);
                resetButton.setIcon(new ImageIcon(getClass().getResource("/com/company/icon_64_playing.png")));
                MinesweeperTableModel model = new MinesweeperTableModel();
                mineSweeperTable.setModel(model);

                flagCountLabel.setText(game.flagged + "");

                setProperTableAttributes();

                resetClock(0, 0, 0);

                game.getRealBoard().displayBoard();

            }
        });
        mineSweeperTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                setSelectionToCurrentMouse(e);
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1:
                        if (game.clickCell(mineSweeperTable.getSelectedRow(), mineSweeperTable.getSelectedColumn())) {
                            userLost();
                        }
                        mineSweeperTable.repaint();
                        break;
                    case MouseEvent.BUTTON3:
                        if (game.flagCell(mineSweeperTable.getSelectedRow(), mineSweeperTable.getSelectedColumn())) {
                            userWon();
                        }
                        flagCountLabel.setText(game.flagged + "");
                        mineSweeperTable.repaint();
                        break;
                    default:
                        System.out.println("wth");
                        return;
                }
            }
        });
        tabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (tabbedPane1.getSelectedIndex() == 0) {
                    try {
                        int rows = Integer.parseInt(rowsTextField.getText());
                        int columns = Integer.parseInt(columnsTextField.getText());
                        int bombs = Integer.parseInt(bombsTextField.getText());
                        if (
                                rows > 45 || rows < 15 ||
                                        columns > 45 || columns < 15 ||
                                        ((bombs > 50 || bombs < 5) && !literalBombs) ||
                                        ((bombs > rows * columns / 2 || bombs < 1) && literalBombs)
                        ) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        tabbedPane1.setSelectedIndex(1);
                        JOptionPane.showMessageDialog(mainPanel, "The minimum number of rows/columns is 15, and the maximum is 45.\n " +
                                "You may only enter positive integers.\n" +
                                "Bombs must be bigger than 0 and smaller than 50 in probability mode \n" +
                                "In literal mode they must be bigger than 1 and smaller than half the total number of cells", "The numbers you entered are invalid!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CardLayout layout = (CardLayout) cardPanel.getLayout();
                layout.show(cardPanel, "Card2");
                tabbedPane1.setSelectedIndex(0);
                resetButton.doClick();
            }
        });
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CardLayout layout = (CardLayout) cardPanel.getLayout();
                layout.show(cardPanel, "Card2");
                tabbedPane1.setSelectedIndex(2);
            }
        });
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CardLayout layout = (CardLayout) cardPanel.getLayout();
                layout.show(cardPanel, "Card2");
                tabbedPane1.setSelectedIndex(1);
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                exit(0);
            }
        });
        chooseFileButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileDialog = new JFileChooser();
                int fileValid = fileDialog.showSaveDialog(mainPanel);
                if (fileValid == JFileChooser.APPROVE_OPTION) {
                    File file = fileDialog.getSelectedFile();
                    saveFileTextField.setText(file.getAbsolutePath());
                }
            }
        });
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileDialog = new JFileChooser();
                int fileValid = fileDialog.showOpenDialog(mainPanel);
                if (fileValid == JFileChooser.APPROVE_OPTION) {
                    File file = fileDialog.getSelectedFile();
                    openFileTextField.setText(file.getAbsolutePath());
                }
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    game.saveBoard(saveFileTextField.getText(), timeCounterLabel.getText());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(mainPanel, "Invalid file", "Nice try bro", JOptionPane.ERROR_MESSAGE);
                } catch (NullPointerException e) {
                    JOptionPane.showMessageDialog(mainPanel, "How can you save a game you didn't start?", "Nice try bro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                GameLogic temp = new GameLogic();
                String time;
                try {
                    time = temp.openBoard(openFileTextField.getText());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(mainPanel, "Invalid file", "Nice try bro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                game = temp;
                rowsTextField.setText(game.getRealBoard().width + "");
                columnsTextField.setText(game.getRealBoard().height + "");
                bombsTextField.setText(game.getRealBoard().bombRate + "");
                resetButton.setIcon(new ImageIcon(getClass().getResource("/com/company/icon_64_playing.png")));
                MinesweeperTableModel model = new MinesweeperTableModel();
                mineSweeperTable.setModel(model);

                flagCountLabel.setText(game.flagged + "");

                setProperTableAttributes();

                String[] times = time.split(":");
                resetClock(Integer.parseInt(times[2]), Integer.parseInt(times[1]), Integer.parseInt(times[0]));

                game.getRealBoard().displayBoard();

            }
        });

        bombValueLiteral.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                literalBombs = bombValueLiteral.isSelected();
                if (literalBombs) {
                    bombValueLiteral.setText("\"Bombs\" is a literal");
                } else {
                    bombValueLiteral.setText("\"Bombs\" is a probability");
                }
            }
        });
        mineSweeperTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setSelectionToCurrentMouse(e);
            }
        });
    }

    private void setSelectionToCurrentMouse(MouseEvent e) {
        int rowAtMouse = mineSweeperTable.rowAtPoint(e.getPoint());
        int colAtMouse = mineSweeperTable.columnAtPoint(e.getPoint());
        if (rowAtMouse < 0 || colAtMouse < 0) {
            return;
        }
        mineSweeperTable.changeSelection(rowAtMouse, colAtMouse, false, false);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new DarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }


        frame = new JFrame("main");
        frame.setContentPane(new Minesweeper().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);

    }

    private void setProperTableAttributes() {
        DefaultTableCellRenderer tableHeaderRenderer = new DefaultTableCellRenderer();
        tableHeaderRenderer.setPreferredSize(new Dimension(0, 0));
        mineSweeperTable.getTableHeader().setDefaultRenderer(tableHeaderRenderer);

        //DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        //centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        mineSweeperTable.setCellSelectionEnabled(true);


        for (int i = 0; i < game.getRealBoard().height; i++) {
            //mineSweeperTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            mineSweeperTable.getColumnModel().getColumn(i).setPreferredWidth(16);
        }
        mineSweeperTable.setRowHeight(16);

        tableScrollPane.setPreferredSize(new Dimension(game.getRealBoard().height * 16, game.getRealBoard().width * 16 + 6));
        frame.pack();

    }

    private void userLost() {
        ImageIcon lossIcon = new ImageIcon(getClass().getResource("/com/company/icon_64_lost.gif"));
        resetButton.setIcon(lossIcon);
        JOptionPane.showMessageDialog(mainPanel, "You lost!", "Sad!", JOptionPane.INFORMATION_MESSAGE, lossIcon);
        resetButton.doClick();
    }

    private void userWon() {
        ImageIcon winIcon = new ImageIcon(getClass().getResource("/com/company/icon_64_win.png"));
        resetButton.setIcon(winIcon);
        JOptionPane.showMessageDialog(mainPanel, "You won!", "Congratulations!", JOptionPane.INFORMATION_MESSAGE, winIcon);
        resetButton.doClick();
    }

    private void resetClock(int sec, int min, int hr) {
        seconds = sec;
        hours = hr;
        minutes = min;

        if (clock != null) {
            clock.stop();
        }

        clock = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                seconds++;
                if (seconds > 59) {
                    minutes++;
                    seconds = 0;
                }
                if (minutes > 59) {
                    hours++;
                    minutes = 0;
                }

                timeCounterLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        });
        clock.setInitialDelay(0);
        clock.start();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout(0, 0));
        mainPanel.add(cardPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        welcomePanel = new JPanel();
        welcomePanel.setLayout(new GridLayoutManager(6, 1, new Insets(10, 10, 10, 10), -1, -1));
        welcomePanel.setMinimumSize(new Dimension(-1, -1));
        cardPanel.add(welcomePanel, "Card1");
        final JLabel label1 = new JLabel();
        label1.setIcon(new ImageIcon(getClass().getResource("/com/company/welcomelogo.png")));
        label1.setText("");
        welcomePanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(240, -1), 0, false));
        final Spacer spacer1 = new Spacer();
        welcomePanel.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        startButton = new JButton();
        startButton.setText("Start");
        welcomePanel.add(startButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        loadButton = new JButton();
        loadButton.setText("Load");
        welcomePanel.add(loadButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        optionsButton = new JButton();
        optionsButton.setText("Options");
        welcomePanel.add(optionsButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        exitButton = new JButton();
        exitButton.setText("Exit");
        welcomePanel.add(exitButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setTabLayoutPolicy(1);
        cardPanel.add(tabbedPane1, "Card2");
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 3, new Insets(10, 10, 10, 10), -1, -1));
        panel1.setMinimumSize(new Dimension(-1, -1));
        tabbedPane1.addTab("Minesweeper", panel1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, -1), null, null, 0, false));
        flagCountLabel = new JLabel();
        flagCountLabel.setHorizontalAlignment(0);
        flagCountLabel.setHorizontalTextPosition(0);
        flagCountLabel.setText("Flags");
        panel2.add(flagCountLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, -1), null, null, 0, false));
        timeCounterLabel = new JLabel();
        timeCounterLabel.setHorizontalAlignment(0);
        timeCounterLabel.setHorizontalTextPosition(0);
        timeCounterLabel.setText("Timer");
        panel3.add(timeCounterLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resetButton = new JButton();
        resetButton.setIcon(new ImageIcon(getClass().getResource("/com/company/icon_64_playing.png")));
        resetButton.setText("");
        resetButton.setToolTipText("Reset game");
        panel1.add(resetButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tableScrollPane = new JScrollPane();
        panel1.add(tableScrollPane, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null));
        mineSweeperTable = new JTable();
        mineSweeperTable.setAutoCreateColumnsFromModel(true);
        mineSweeperTable.setDragEnabled(false);
        mineSweeperTable.setIntercellSpacing(new Dimension(0, 0));
        mineSweeperTable.setPreferredScrollableViewportSize(new Dimension(-1, -1));
        mineSweeperTable.setRowSelectionAllowed(false);
        mineSweeperTable.setShowHorizontalLines(true);
        tableScrollPane.setViewportView(mineSweeperTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(5, 2, new Insets(10, 10, 10, 10), -1, -1));
        panel4.setMinimumSize(new Dimension(-1, -1));
        tabbedPane1.addTab("Options", panel4);
        rowsTextField = new JTextField();
        rowsTextField.setText("15");
        panel4.add(rowsTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        columnsTextField = new JTextField();
        columnsTextField.setText("15");
        panel4.add(columnsTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Columns");
        panel4.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Rows");
        panel4.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bombsTextField = new JTextField();
        bombsTextField.setText("20");
        panel4.add(bombsTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Bombs");
        panel4.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        bombValueLiteral = new JCheckBox();
        bombValueLiteral.setText("\"Bombs\" is a probability");
        panel4.add(bombValueLiteral, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setMinimumSize(new Dimension(-1, -1));
        tabbedPane1.addTab("Load/Save", panel5);
        chooseFileButton1 = new JButton();
        chooseFileButton1.setIcon(new ImageIcon(getClass().getResource("/com/company/file.png")));
        chooseFileButton1.setText("");
        panel5.add(chooseFileButton1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveFileTextField = new JTextField();
        saveFileTextField.setText("");
        panel5.add(saveFileTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel5.add(saveButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openButton = new JButton();
        openButton.setText("Open");
        panel5.add(openButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openFileTextField = new JTextField();
        panel5.add(openFileTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        chooseFileButton = new JButton();
        chooseFileButton.setIcon(new ImageIcon(getClass().getResource("/com/company/file.png")));
        chooseFileButton.setText("");
        panel5.add(chooseFileButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel5.add(spacer3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private class MinesweeperTableModel extends AbstractTableModel {
        public int getRowCount() {
            return game.getRealBoard().width;
        }

        public int getColumnCount() {
            return game.getRealBoard().height;
        }

        public Object getValueAt(int row, int col) {
            char toBeDisplayed = game.getFakeBoard()[row][col];
            if ((toBeDisplayed >= '1' && toBeDisplayed <= '9') || toBeDisplayed == '!' || toBeDisplayed == ' ' || toBeDisplayed == '#') {
                return new ImageIcon(Minesweeper.class.getResource("icon_16_" + toBeDisplayed + ".png"));
            }
            if (toBeDisplayed == '*') {
                return new ImageIcon(Minesweeper.class.getResource("icon_16_bomb.png"));
            }
            return game.getFakeBoard()[row][col];
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public Class getColumnClass(int c) {
            return ImageIcon.class;
            //return getValueAt(0, c).getClass();
        }
    }
}
