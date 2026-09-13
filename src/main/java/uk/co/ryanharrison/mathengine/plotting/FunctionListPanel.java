package uk.co.ryanharrison.mathengine.plotting;

import uk.co.ryanharrison.mathengine.core.Function;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Side panel for managing multiple plotted functions.
 * <p>
 * Provides a list view of all functions with controls to:
 * </p>
 * <ul>
 *     <li>Toggle visibility (show/hide individual functions)</li>
 *     <li>Change function colors</li>
 *     <li>Edit function equations</li>
 *     <li>Remove functions</li>
 * </ul>
 */
public final class FunctionListPanel extends JPanel {
    private final DefaultListModel<FunctionListItem> listModel = new DefaultListModel<>();
    private final JList<FunctionListItem> functionList = new JList<>(listModel);
    private final List<FunctionListListener> listeners = new ArrayList<>();

    private static final Color[] DEFAULT_COLORS = {
            new Color(0, 102, 204),      // Blue
            new Color(204, 0, 0),        // Red
            new Color(0, 153, 0),        // Green
            new Color(204, 102, 0),      // Orange
            new Color(153, 0, 153),      // Purple
            new Color(0, 153, 153),      // Teal
            new Color(153, 102, 51),     // Brown
            new Color(204, 0, 102)       // Magenta
    };

    private int colorIndex = 0;

    /**
     * Creates a new function list panel.
     */
    public FunctionListPanel() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Functions"));
        setPreferredSize(new Dimension(250, 0));

        initializeComponents();
    }

    private void initializeComponents() {
        // Configure list
        functionList.setCellRenderer(new FunctionListCellRenderer());
        functionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(functionList);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton removeButton = new JButton("Remove");

        addButton.addActionListener(e -> showAddFunctionDialog());
        editButton.addActionListener(e -> editSelectedFunction());
        removeButton.addActionListener(e -> removeSelectedFunction());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add click listener for checkbox toggle and double-click to edit
        functionList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = functionList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Rectangle bounds = functionList.getCellBounds(index, index);
                    if (bounds != null) {
                        if (e.getClickCount() == 2) {
                            // Double-click to edit
                            functionList.setSelectedIndex(index);
                            editSelectedFunction();
                        } else if (e.getX() < 25) {
                            // Single click on checkbox area
                            toggleFunctionVisibility(index);
                        }
                    }
                }
            }
        });
    }

    /**
     * Adds a function to the list.
     *
     * @param function the function to add
     */
    public void addFunction(PlottedFunction function) {
        listModel.addElement(new FunctionListItem(function, listModel.size()));
    }

    /**
     * Updates a function in the list.
     *
     * @param index    the index of the function to update
     * @param function the new function
     */
    public void updateFunction(int index, PlottedFunction function) {
        if (index >= 0 && index < listModel.size()) {
            FunctionListItem item = listModel.get(index);
            listModel.set(index, new FunctionListItem(function, item.index));
            fireFunctionUpdated(index, function);
        }
    }

    /**
     * Removes a function from the list.
     *
     * @param index the index of the function to remove
     */
    public void removeFunction(int index) {
        if (index >= 0 && index < listModel.size()) {
            listModel.remove(index);
            // Update indices
            for (int i = 0; i < listModel.size(); i++) {
                FunctionListItem item = listModel.get(i);
                listModel.set(i, new FunctionListItem(item.function, i));
            }
        }
    }

    /**
     * Clears all functions from the list.
     */
    public void clearFunctions() {
        listModel.clear();
    }

    /**
     * Adds a listener for function list events.
     *
     * @param listener the listener to add
     */
    public void addFunctionListListener(FunctionListListener listener) {
        listeners.add(listener);
    }

    private void showAddFunctionDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        JTextField equationField = new JTextField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Equation:"), gbc);
        gbc.gridx = 1;
        panel.add(equationField, gbc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Function",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String equation = equationField.getText().trim();

            if (!equation.isEmpty()) {
                if (name.isEmpty()) {
                    name = "f" + (listModel.size() + 1) + "(x)";
                }

                try {
                    Function func = new Function(equation);
                    PlottedFunction plottedFunc = PlottedFunction.builder()
                            .function(func)
                            .name(name)
                            .color(getNextColor())
                            .strokeWidth(2.0f)
                            .visible(true)
                            .build();

                    addFunction(plottedFunc);
                    fireFunctionAdded(plottedFunc);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid equation: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    private void editSelectedFunction() {
        int index = functionList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Please select a function to edit.");
            return;
        }

        FunctionListItem item = listModel.get(index);
        PlottedFunction currentFunc = item.function;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(currentFunc.name(), 15);
        JTextField equationField = new JTextField(currentFunc.getEquation(), 15);
        JButton colorButton = new JButton("Change Color");
        final Color[] selectedColor = {currentFunc.color()};

        colorButton.setBackground(selectedColor[0]);
        colorButton.setOpaque(true);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Color", selectedColor[0]);
            if (newColor != null) {
                selectedColor[0] = newColor;
                colorButton.setBackground(newColor);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Equation:"), gbc);
        gbc.gridx = 1;
        panel.add(equationField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 1;
        panel.add(colorButton, gbc);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Function",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String equation = equationField.getText().trim();

            if (!equation.isEmpty()) {
                try {
                    Function func = new Function(equation);
                    PlottedFunction newFunc = PlottedFunction.builder()
                            .function(func)
                            .name(name.isEmpty() ? currentFunc.name() : name)
                            .color(selectedColor[0])
                            .strokeWidth(currentFunc.strokeWidth())
                            .visible(currentFunc.visible())
                            .build();

                    updateFunction(index, newFunc);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid equation: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    private void removeSelectedFunction() {
        int index = functionList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Please select a function to remove.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove function: " + listModel.get(index).function.name() + "?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            removeFunction(index);
            fireFunctionRemoved(index);
        }
    }

    private void toggleFunctionVisibility(int index) {
        if (index >= 0 && index < listModel.size()) {
            FunctionListItem item = listModel.get(index);
            PlottedFunction newFunc = item.function.withVisible(!item.function.visible());
            listModel.set(index, new FunctionListItem(newFunc, item.index));
            functionList.repaint();
            fireFunctionUpdated(index, newFunc);
        }
    }

    private Color getNextColor() {
        Color color = DEFAULT_COLORS[colorIndex % DEFAULT_COLORS.length];
        colorIndex++;
        return color;
    }

    private void fireFunctionAdded(PlottedFunction function) {
        for (FunctionListListener listener : listeners) {
            listener.onFunctionAdded(function);
        }
    }

    private void fireFunctionUpdated(int index, PlottedFunction function) {
        for (FunctionListListener listener : listeners) {
            listener.onFunctionUpdated(index, function);
        }
    }

    private void fireFunctionRemoved(int index) {
        for (FunctionListListener listener : listeners) {
            listener.onFunctionRemoved(index);
        }
    }

    /**
     * Interface for listening to function list events.
     */
    public interface FunctionListListener {
        default void onFunctionAdded(PlottedFunction function) {
        }

        default void onFunctionUpdated(int index, PlottedFunction function) {
        }

        default void onFunctionRemoved(int index) {
        }
    }

    /**
     * Internal wrapper for list items.
     */
    private record FunctionListItem(PlottedFunction function, int index) {
    }

    /**
     * Custom cell renderer for function list.
     */
    private static class FunctionListCellRenderer extends JPanel implements ListCellRenderer<FunctionListItem> {
        private final JCheckBox visibilityCheckbox = new JCheckBox();
        private final JLabel colorLabel = new JLabel();
        private final JLabel nameLabel = new JLabel();
        private final JLabel equationLabel = new JLabel();

        FunctionListCellRenderer() {
            setLayout(new BorderLayout(5, 2));
            setBorder(new EmptyBorder(5, 5, 5, 5));

            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            leftPanel.setOpaque(false);
            leftPanel.add(visibilityCheckbox);

            colorLabel.setOpaque(true);
            colorLabel.setPreferredSize(new Dimension(16, 16));
            colorLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            leftPanel.add(colorLabel);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            textPanel.setOpaque(false);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
            equationLabel.setFont(equationLabel.getFont().deriveFont(Font.PLAIN, 10f));
            equationLabel.setForeground(Color.GRAY);
            textPanel.add(nameLabel);
            textPanel.add(equationLabel);

            add(leftPanel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends FunctionListItem> list,
                FunctionListItem item,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            PlottedFunction func = item.function;

            visibilityCheckbox.setSelected(func.visible());
            colorLabel.setBackground(func.color());
            nameLabel.setText(func.name());
            equationLabel.setText(func.getEquation());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                nameLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                nameLabel.setForeground(list.getForeground());
            }

            return this;
        }
    }
}
