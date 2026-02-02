package uk.co.ryanharrison.mathengine.gui;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.CompiledExpression;
import uk.co.ryanharrison.mathengine.parser.MathEngine;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;
import uk.co.ryanharrison.mathengine.parser.format.NodeFormatter;
import uk.co.ryanharrison.mathengine.parser.format.StringNodeFormatter;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.registry.ConstantDefinition;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;
import uk.co.ryanharrison.mathengine.parser.util.AstTreeBuilder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * Professional GUI REPL for the Math Engine with comprehensive features:
 * - Interactive expression evaluation with history
 * - Configurable engine settings (angle unit, precision, feature toggles)
 * - Debug views (tokens, AST, evaluation steps)
 * - Session information (variables, functions, constants, operators)
 * - Syntax highlighting and formatted output
 *
 * @author Ryan Harrison
 */
public final class MainFrame extends JFrame {

    private static final String TITLE = "Math Engine - Expression Evaluator";
    private static final int DEFAULT_WIDTH = 1400;
    private static final int DEFAULT_HEIGHT = 900;
    private static final int MIN_WIDTH = 1200;
    private static final int MIN_HEIGHT = 700;

    // Modern color scheme
    private static final Color BG_PRIMARY = new Color(250, 250, 252);
    private static final Color BG_SECONDARY = new Color(240, 242, 245);
    private static final Color BG_PANEL = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(0, 120, 215);
    private static final Color SUCCESS_COLOR = new Color(16, 124, 16);
    private static final Color ERROR_COLOR = new Color(200, 50, 50);
    private static final Color BORDER_COLOR = new Color(220, 220, 225);
    private static final Color TEXT_PRIMARY = new Color(30, 30, 30);
    private static final Color TEXT_SECONDARY = new Color(100, 100, 100);

    // Fonts
    private static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 14);
    private static final Font MONO_LARGE = new Font("Consolas", Font.PLAIN, 16);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font UI_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    public static void main() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Set modern font rendering
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            // Use default
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Components
    private MathEngine mathEngine;
    private MathEngineConfig currentConfig;
    private NodeFormatter formatter;

    private final JTextPane outputPane;
    private final JTextArea inputArea;
    private final JTextPane tokensPane;
    private final JTextPane astPane;
    private final JTextPane variablesPane;
    private final JTextPane functionsPane;
    private final JTextPane constantsPane;
    private final JTextPane unitsPane;
    private final JTextPane referencePane;
    private final JPanel configPane;
    private final JLabel statusLabel;
    private final JSplitPane mainSplit;
    private final JLabel angleUnitLabel;
    private final JLabel precisionLabel;

    // Styles
    private final SimpleAttributeSet promptStyle;
    private final SimpleAttributeSet resultStyle;
    private final SimpleAttributeSet errorStyle;
    private final SimpleAttributeSet normalStyle;
    private final SimpleAttributeSet commentStyle;
    private final SimpleAttributeSet keywordStyle;
    private final SimpleAttributeSet numberStyle;

    public MainFrame() {
        super(TITLE);

        // Initialize styles
        promptStyle = createStyle(ACCENT_COLOR, true);
        resultStyle = createStyle(SUCCESS_COLOR, true);
        errorStyle = createStyle(ERROR_COLOR, false);
        normalStyle = createStyle(TEXT_PRIMARY, false);
        commentStyle = createStyle(TEXT_SECONDARY, false);
        keywordStyle = createStyle(new Color(0, 0, 255), true);
        numberStyle = createStyle(new Color(9, 134, 88), false);

        // Initialize engine with default config
        currentConfig = MathEngineConfig.builder()
                .decimalPlaces(6)
                .build();
        mathEngine = MathEngine.create(currentConfig);
        formatter = StringNodeFormatter.withDecimalPlaces(currentConfig.decimalPlaces());

        // Create components
        outputPane = createStyledTextPane();
        inputArea = createInputArea();
        tokensPane = createStyledTextPane();
        astPane = createStyledTextPane();
        variablesPane = createStyledTextPane();
        functionsPane = createStyledTextPane();
        constantsPane = createStyledTextPane();
        unitsPane = createStyledTextPane();
        referencePane = createStyledTextPane();
        configPane = createConfigPane();

        // Status bar components
        statusLabel = new JLabel("Ready");
        angleUnitLabel = new JLabel();
        precisionLabel = new JLabel();

        // Build UI first so mainSplit is initialized
        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        setupMenuBar();
        setupMainLayout();
        updateStatusBar();

        // Window settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

        // Display welcome message
        displayWelcomeMessage();
        updateSessionInfo();

        SwingUtilities.invokeLater(inputArea::grabFocus);
    }

    private SimpleAttributeSet createStyle(Color color, boolean bold) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        return style;
    }

    private JTextPane createStyledTextPane() {
        JTextPane pane = new JTextPane();
        pane.setFont(MONO_FONT);
        pane.setEditable(false);
        pane.setBackground(BG_PANEL);
        pane.setMargin(new Insets(10, 10, 10, 10));
        return pane;
    }

    private JTextArea createInputArea() {
        JTextArea area = new HistoricalTextField(3, 40);
        area.setFont(MONO_LARGE);
        area.setMargin(new Insets(12, 12, 12, 12));
        area.setBackground(BG_PANEL);
        area.setForeground(TEXT_PRIMARY);
        area.setCaretColor(ACCENT_COLOR);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        area.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isControlDown() || e.isShiftDown()) {
                        // Allow multi-line input with Ctrl+Enter or Shift+Enter
                        return;
                    }

                    e.consume();
                    String expression = area.getText().trim();
                    if (!expression.isEmpty()) {
                        if (expression.equalsIgnoreCase("clear")) {
                            outputPane.setText("");
                            statusLabel.setText("Output cleared");
                        } else if (expression.equalsIgnoreCase("reset")) {
                            resetEngine();
                        } else {
                            evaluateExpression(expression);
                        }
                    }
                    area.setText("");
                }
            }
        });

        return area;
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BG_SECONDARY);

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(UI_FONT);

        JMenuItem clearOutput = new JMenuItem("Clear Output");
        clearOutput.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        clearOutput.addActionListener(e -> {
            outputPane.setText("");
            statusLabel.setText("Output cleared");
        });

        JMenuItem resetEngine = new JMenuItem("Reset Engine");
        resetEngine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
        resetEngine.addActionListener(e -> resetEngine());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        fileMenu.add(clearOutput);
        fileMenu.add(resetEngine);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(UI_FONT);

        JMenuItem toggleSidebar = new JMenuItem("Toggle Sidebar");
        toggleSidebar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));
        toggleSidebar.addActionListener(e -> toggleSidebar());

        JMenuItem refreshSession = new JMenuItem("Refresh Info");
        refreshSession.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshSession.addActionListener(e -> updateSessionInfo());

        viewMenu.add(toggleSidebar);
        viewMenu.add(refreshSession);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private void toggleSidebar() {
        int location = mainSplit.getDividerLocation();
        if (location < mainSplit.getWidth() - 50) {
            // Sidebar is visible, hide it
            mainSplit.setDividerLocation(1.0);
            statusLabel.setText("Sidebar hidden (Ctrl+B to show)");
        } else {
            // Sidebar is hidden, show it
            mainSplit.setDividerLocation(800);
            statusLabel.setText("Sidebar shown");
        }
    }

    private void setupMainLayout() {
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_PRIMARY);

        // Configure the split pane
        mainSplit.setDividerLocation(800);
        mainSplit.setResizeWeight(0.6);
        mainSplit.setBorder(null);

        // Left side: REPL (output + input)
        JPanel replPanel = createReplPanel();

        // Right side: Tabbed pane with debug and session info
        JTabbedPane rightTabs = new JTabbedPane();
        rightTabs.setFont(UI_FONT);
        rightTabs.setBackground(BG_SECONDARY);

        rightTabs.addTab("Debug", createDebugPanel());
        rightTabs.addTab("Variables", createScrollPane(variablesPane, "Session Variables"));
        rightTabs.addTab("Functions", createScrollPane(functionsPane, "Available Functions"));
        rightTabs.addTab("Constants", createScrollPane(constantsPane, "Predefined Constants"));
        rightTabs.addTab("Units", createScrollPane(unitsPane, "Unit Conversions"));
        rightTabs.addTab("Config", configPane);
        rightTabs.addTab("Reference", createScrollPane(referencePane, "Grammar Reference"));

        mainSplit.setLeftComponent(replPanel);
        mainSplit.setRightComponent(rightTabs);

        mainPanel.add(mainSplit, BorderLayout.CENTER);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createReplPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_PRIMARY);

        // Output area
        JScrollPane outputScroll = new JScrollPane(outputPane);
        outputScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 4, 4),
                BorderFactory.createLineBorder(BORDER_COLOR, 1)
        ));
        outputScroll.setBackground(BG_PANEL);

        // Input area
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 8, 8, 4),
                BorderFactory.createLineBorder(ACCENT_COLOR, 2)
        ));
        inputScroll.setPreferredSize(new Dimension(0, 120));

        // Input hint label
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(BG_PRIMARY);
        JLabel hintLabel = new JLabel("Enter expression (Enter to evaluate, 'clear' to clear output, 'reset' to reset engine)");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(TEXT_SECONDARY);
        hintLabel.setBorder(new EmptyBorder(4, 12, 4, 12));
        inputPanel.add(hintLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        panel.add(outputScroll, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDebugPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);

        JTabbedPane debugTabs = new JTabbedPane();
        debugTabs.setFont(UI_FONT);

        debugTabs.addTab("Tokens", createScrollPane(tokensPane, "Lexer Tokens"));
        debugTabs.addTab("AST", createScrollPane(astPane, "Abstract Syntax Tree"));

        panel.add(debugTabs, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createScrollPane(JTextPane pane, String title) {
        JScrollPane scroll = new JScrollPane(pane);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        title,
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        UI_BOLD,
                        TEXT_SECONDARY
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return scroll;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(BG_SECONDARY);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        statusLabel.setFont(UI_FONT);
        statusLabel.setForeground(TEXT_PRIMARY);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(BG_SECONDARY);

        angleUnitLabel.setFont(UI_FONT);
        angleUnitLabel.setForeground(TEXT_SECONDARY);

        precisionLabel.setFont(UI_FONT);
        precisionLabel.setForeground(TEXT_SECONDARY);

        rightPanel.add(angleUnitLabel);
        rightPanel.add(new JSeparator(SwingConstants.VERTICAL));
        rightPanel.add(precisionLabel);

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(rightPanel, BorderLayout.EAST);

        return statusBar;
    }

    private void updateStatusBar() {
        AngleUnit unit = currentConfig.angleUnit();
        angleUnitLabel.setText("Angle Unit: " + (unit == AngleUnit.RADIANS ? "Radians" : "Degrees"));

        String precision = currentConfig.forceDoubleArithmetic() ? "Double (Fast)" : "Rational (Exact)";
        precisionLabel.setText("Arithmetic: " + precision);
    }

    private void displayWelcomeMessage() {
        try {
            StyledDocument doc = outputPane.getStyledDocument();
            doc.insertString(doc.getLength(), "Math Engine - Interactive Expression Evaluator\n\n", resultStyle);

            doc.insertString(doc.getLength(), "Examples:\n", keywordStyle);
            doc.insertString(doc.getLength(), "  2 + 3 * 4                    →  14\n", normalStyle);
            doc.insertString(doc.getLength(), "  {1, 2, 3} + {4, 5, 6}        →  {5, 7, 9}\n", normalStyle);
            doc.insertString(doc.getLength(), "  100 meters in feet           →  328.084 feet\n", normalStyle);
            doc.insertString(doc.getLength(), "  f(x) := x^2; f(5)            →  25\n\n", normalStyle);

            doc.insertString(doc.getLength(), "Commands: 'clear' (clear output), 'reset' (reset engine)\n", commentStyle);
            doc.insertString(doc.getLength(), "Shortcuts: Ctrl+L (clear), Ctrl+R (reset), Ctrl+B (toggle sidebar)\n\n", commentStyle);

        } catch (BadLocationException e) {
            // Ignore
        }
    }

    private void evaluateExpression(String expression) {
        try {
            // Compile and evaluate
            CompiledExpression compiled = mathEngine.compile(expression);
            Node ast = compiled.getAst();

            // Display tokens and AST
            displayTokens(expression);
            displayAst(ast);

            NodeConstant result = compiled.evaluate();

            // Display in output
            StyledDocument doc = outputPane.getStyledDocument();

            // Show input (formatted AST)
            String formattedInput = formatNode(ast);
            doc.insertString(doc.getLength(), "❯ ", promptStyle);
            doc.insertString(doc.getLength(), formattedInput + "\n", normalStyle);

            // Show result
            String resultStr = formatNode(result);
            doc.insertString(doc.getLength(), "  ", normalStyle);
            doc.insertString(doc.getLength(), resultStr, resultStyle);
            doc.insertString(doc.getLength(), "\n\n", normalStyle);

            // Scroll to bottom
            outputPane.setCaretPosition(doc.getLength());

            // Update session info
            updateSessionInfo();
            statusLabel.setText("Expression evaluated successfully");

        } catch (Exception e) {
            displayError(expression, e);
            statusLabel.setText("Error: " + e.getClass().getSimpleName());
        }
    }

    private String formatNode(Node node) {
        return formatter.format(node);
    }

    private void displayTokens(String expression) {
        StyledDocument doc = tokensPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            List<Token> tokens = mathEngine.tokenize(expression);

            doc.insertString(doc.getLength(), "Token Stream (" + tokens.size() + " tokens):\n\n", keywordStyle);

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);

                // Token number
                doc.insertString(doc.getLength(), String.format("[%2d] ", i), commentStyle);

                // Token type
                String typeStr = String.format("%-20s", token.type());
                doc.insertString(doc.getLength(), typeStr, normalStyle);

                // Token lexeme
                doc.insertString(doc.getLength(), " \"" + token.lexeme() + "\"", promptStyle);

                // Position
                doc.insertString(doc.getLength(), String.format("  @%d:%d", token.line(), token.column()), commentStyle);

                doc.insertString(doc.getLength(), "\n", normalStyle);
            }

        } catch (Exception e) {
            try {
                doc.insertString(doc.getLength(), "Error tokenizing: " + e.getMessage(), errorStyle);
            } catch (BadLocationException ex) {
                // Ignore
            }
        }
    }

    private void displayAst(Node ast) {
        StyledDocument doc = astPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            doc.insertString(doc.getLength(), "Abstract Syntax Tree:\n\n", keywordStyle);

            String astStr = formatAstTree(ast, "", true);
            doc.insertString(doc.getLength(), astStr, normalStyle);

            doc.insertString(doc.getLength(), "\n\nNode Type: ", commentStyle);
            doc.insertString(doc.getLength(), ast.getClass().getSimpleName(), keywordStyle);

            doc.insertString(doc.getLength(), "\nString Representation: ", commentStyle);
            doc.insertString(doc.getLength(), ast.toString(), normalStyle);

        } catch (BadLocationException e) {
            // Ignore
        }
    }

    private String formatAstTree(Node node, String prefix, boolean isLast) {
        StringBuilder sb = new StringBuilder();

        // Current node
        sb.append(prefix);
        sb.append(isLast ? "└── " : "├── ");
        sb.append(node.getClass().getSimpleName());

        // Add node details
        String details = getNodeDetails(node);
        if (!details.isEmpty()) {
            sb.append(": ").append(details);
        }
        sb.append("\n");

        // Children (if any)
        List<Node> children = getNodeChildren(node);
        for (int i = 0; i < children.size(); i++) {
            boolean childIsLast = (i == children.size() - 1);
            String childPrefix = prefix + (isLast ? "    " : "│   ");
            sb.append(formatAstTree(children.get(i), childPrefix, childIsLast));
        }

        return sb.toString();
    }

    private String getNodeDetails(Node node) {
        // Return brief details about the node
        String str = node.toString();
        if (str.length() > 30) {
            str = str.substring(0, 27) + "...";
        }
        return str;
    }

    private List<Node> getNodeChildren(Node node) {
        AstTreeBuilder builder = new AstTreeBuilder();
        return node.accept(builder);
    }

    private void displayError(String expression, Exception e) {
        try {
            StyledDocument doc = outputPane.getStyledDocument();

            // Show the raw input that caused the error
            doc.insertString(doc.getLength(), "❯ ", promptStyle);
            doc.insertString(doc.getLength(), expression + "\n", normalStyle);

            // Show error details with enhanced formatting
            doc.insertString(doc.getLength(), "❌ ", errorStyle);

            // Use formatMessage() for MathEngineException to get enhanced error details
            if (e instanceof uk.co.ryanharrison.mathengine.parser.MathEngineException mathEx) {
                String formattedMessage = mathEx.formatMessage();
                doc.insertString(doc.getLength(), formattedMessage + "\n\n", errorStyle);
            } else {
                // Fallback for other exceptions
                doc.insertString(doc.getLength(), e.getClass().getSimpleName() + ": ", errorStyle);
                String message = e.getMessage() != null ? e.getMessage() : "No message available";
                doc.insertString(doc.getLength(), message + "\n\n", errorStyle);
            }

            outputPane.setCaretPosition(doc.getLength());

        } catch (BadLocationException ex) {
            // Ignore
        }
    }

    private void updateSessionInfo() {
        updateVariablesPane();
        updateFunctionsPane();
        updateConstantsPane();
        updateUnitsPane();
        updateReferencePane();
    }

    private void updateReferencePane() {
        StyledDocument doc = referencePane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            doc.insertString(doc.getLength(), "MATH ENGINE GRAMMAR REFERENCE\n\n", resultStyle);

            doc.insertString(doc.getLength(), "LITERALS\n", keywordStyle);
            doc.insertString(doc.getLength(), "  Numbers:  42, 3.14, 1e-3, 1/3\n", normalStyle);
            doc.insertString(doc.getLength(), "  Strings:  \"hello\", 'world'\n", normalStyle);
            doc.insertString(doc.getLength(), "  Vectors:  {1, 2, 3}\n", normalStyle);
            doc.insertString(doc.getLength(), "  Matrices: [[1, 2], [3, 4]] or [1, 2; 3, 4]\n", normalStyle);
            doc.insertString(doc.getLength(), "  Ranges:   1..10, 0..1 step 0.1\n\n", normalStyle);

            doc.insertString(doc.getLength(), "OPERATORS\n", keywordStyle);
            doc.insertString(doc.getLength(), "  Arithmetic:  +, -, *, /, ^, mod\n", normalStyle);
            doc.insertString(doc.getLength(), "  Comparison:  <, >, <=, >=, ==, !=\n", normalStyle);
            doc.insertString(doc.getLength(), "  Logical:     &&, ||, xor, not\n", normalStyle);
            doc.insertString(doc.getLength(), "  Special:     @ (map/matrix multiply), of (percent), ! (factorial)\n", normalStyle);
            doc.insertString(doc.getLength(), "  Assignment:  :=\n", normalStyle);
            doc.insertString(doc.getLength(), "  Lambda:      ->\n\n", normalStyle);

            doc.insertString(doc.getLength(), "REFERENCE SYMBOLS\n", keywordStyle);
            doc.insertString(doc.getLength(), "  @unit   Force unit resolution (e.g., @m for meters)\n", normalStyle);
            doc.insertString(doc.getLength(), "  $var    Force variable resolution (e.g., $m1)\n", normalStyle);
            doc.insertString(doc.getLength(), "  #const  Force constant resolution (e.g., #pi, #e)\n\n", normalStyle);

            doc.insertString(doc.getLength(), "BASIC ARITHMETIC\n", keywordStyle);
            doc.insertString(doc.getLength(), "  2 + 3 * 4        → 14\n", commentStyle);
            doc.insertString(doc.getLength(), "  2^10             → 1024\n", commentStyle);
            doc.insertString(doc.getLength(), "  5! / 3!          → 20\n", commentStyle);
            doc.insertString(doc.getLength(), "  50% of 200       → 100\n\n", commentStyle);

            doc.insertString(doc.getLength(), "VARIABLES & FUNCTIONS\n", keywordStyle);
            doc.insertString(doc.getLength(), "  x := 5\n", commentStyle);
            doc.insertString(doc.getLength(), "  f(x) := x^2 + 2*x\n", commentStyle);
            doc.insertString(doc.getLength(), "  f(3)             → 15\n\n", commentStyle);

            doc.insertString(doc.getLength(), "VECTORS & MATRICES\n", keywordStyle);
            doc.insertString(doc.getLength(), "  v := {1, 2, 3}\n", commentStyle);
            doc.insertString(doc.getLength(), "  m := [[1, 2], [3, 4]]\n", commentStyle);
            doc.insertString(doc.getLength(), "  v[0]             → 1 (subscript)\n", commentStyle);
            doc.insertString(doc.getLength(), "  m[0,1]           → 2\n", commentStyle);
            doc.insertString(doc.getLength(), "  m @ m            → [[7, 10], [15, 22]] (matrix mult)\n\n", commentStyle);

            doc.insertString(doc.getLength(), "BUILT-IN FUNCTIONS\n", keywordStyle);
            doc.insertString(doc.getLength(), "  sin(pi/2)        → 1\n", commentStyle);
            doc.insertString(doc.getLength(), "  ln(e)            → 1\n", commentStyle);
            doc.insertString(doc.getLength(), "  sqrt(16)         → 4\n", commentStyle);
            doc.insertString(doc.getLength(), "  abs(-5)          → 5\n\n", commentStyle);

            doc.insertString(doc.getLength(), "RANGES & COMPREHENSIONS\n", keywordStyle);
            doc.insertString(doc.getLength(), "  1..5             → {1, 2, 3, 4, 5}\n", commentStyle);
            doc.insertString(doc.getLength(), "  {x^2 for x in 1..5} → {1, 4, 9, 16, 25}\n", commentStyle);
            doc.insertString(doc.getLength(), "  sum({x for x in 1..100 if x mod 2 == 0})\n\n", commentStyle);

            doc.insertString(doc.getLength(), "LAMBDAS & HIGHER-ORDER\n", keywordStyle);
            doc.insertString(doc.getLength(), "  map(x -> x*2, {1,2,3})  → {2, 4, 6}\n", commentStyle);
            doc.insertString(doc.getLength(), "  filter(x -> x > 5, 1..10)\n\n", commentStyle);

            doc.insertString(doc.getLength(), "UNIT CONVERSIONS\n", keywordStyle);
            doc.insertString(doc.getLength(), "  100 meters in feet     → 328.084 feet\n", commentStyle);
            doc.insertString(doc.getLength(), "  32 fahrenheit to celsius → 0 celsius\n\n", commentStyle);

        } catch (BadLocationException e) {
            // Ignore
        }
    }

    private JPanel createConfigPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Angle Unit - Radio Buttons
        JLabel angleLabel = new JLabel("Angle Unit:");
        angleLabel.setFont(UI_BOLD);
        angleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(angleLabel);
        panel.add(Box.createVerticalStrut(5));

        JPanel anglePanel = new JPanel();
        anglePanel.setLayout(new BoxLayout(anglePanel, BoxLayout.Y_AXIS));
        anglePanel.setBackground(BG_PANEL);
        anglePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup angleGroup = new ButtonGroup();

        JRadioButton radiansButton = new JRadioButton("Radians");
        radiansButton.setBackground(BG_PANEL);
        radiansButton.setSelected(currentConfig.angleUnit() == AngleUnit.RADIANS);
        radiansButton.addActionListener(e -> updateConfig(config -> config.angleUnit(AngleUnit.RADIANS)));
        angleGroup.add(radiansButton);
        anglePanel.add(radiansButton);

        JRadioButton degreesButton = new JRadioButton("Degrees");
        degreesButton.setBackground(BG_PANEL);
        degreesButton.setSelected(currentConfig.angleUnit() == AngleUnit.DEGREES);
        degreesButton.addActionListener(e -> updateConfig(config -> config.angleUnit(AngleUnit.DEGREES)));
        angleGroup.add(degreesButton);
        anglePanel.add(degreesButton);

        panel.add(anglePanel);
        panel.add(Box.createVerticalStrut(15));

        // Decimal Places
        JLabel decimalLabel = new JLabel("Decimal Places:");
        decimalLabel.setFont(UI_BOLD);
        decimalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(decimalLabel);
        panel.add(Box.createVerticalStrut(5));

        JPanel decimalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        decimalPanel.setBackground(BG_PANEL);
        decimalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] decimalOptions = {"Full Precision", "2", "4", "6", "8", "10"};
        JComboBox<String> decimalCombo = new JComboBox<>(decimalOptions);
        int currentDecimalPlaces = currentConfig.decimalPlaces();
        if (currentDecimalPlaces == -1) {
            decimalCombo.setSelectedItem("Full Precision");
        } else {
            decimalCombo.setSelectedItem(String.valueOf(currentDecimalPlaces));
        }
        decimalCombo.addActionListener(e -> {
            String selected = (String) decimalCombo.getSelectedItem();
            int places = "Full Precision".equals(selected) ? -1 : Integer.parseInt(selected);
            updateConfig(config -> config.decimalPlaces(places));
        });
        decimalPanel.add(decimalCombo);
        panel.add(decimalPanel);
        panel.add(Box.createVerticalStrut(15));

        // Arithmetic Mode
        JCheckBox forceDouble = new JCheckBox("Force Double Arithmetic (faster, less precise)");
        forceDouble.setBackground(BG_PANEL);
        forceDouble.setAlignmentX(Component.LEFT_ALIGNMENT);
        forceDouble.setSelected(currentConfig.forceDoubleArithmetic());
        forceDouble.addActionListener(e ->
                updateConfig(config -> config.forceDoubleArithmetic(forceDouble.isSelected()))
        );
        panel.add(forceDouble);
        panel.add(Box.createVerticalStrut(15));

        // Feature Toggles
        JLabel featuresLabel = new JLabel("Feature Toggles:");
        featuresLabel.setFont(UI_BOLD);
        featuresLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(featuresLabel);
        panel.add(Box.createVerticalStrut(5));

        // Create feature checkboxes with proper wiring
        JCheckBox implicitMultCB = new JCheckBox("Implicit Multiplication (2x = 2*x)");
        implicitMultCB.setBackground(BG_PANEL);
        implicitMultCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        implicitMultCB.setSelected(currentConfig.implicitMultiplication());
        implicitMultCB.addActionListener(e ->
                updateConfig(config -> config.implicitMultiplication(implicitMultCB.isSelected()))
        );
        panel.add(implicitMultCB);

        JCheckBox vectorsCB = new JCheckBox("Vectors ({1, 2, 3})");
        vectorsCB.setBackground(BG_PANEL);
        vectorsCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        vectorsCB.setSelected(currentConfig.vectorsEnabled());
        vectorsCB.addActionListener(e ->
                updateConfig(config -> config.vectorsEnabled(vectorsCB.isSelected()))
        );
        panel.add(vectorsCB);

        JCheckBox matricesCB = new JCheckBox("Matrices ([[1, 2], [3, 4]])");
        matricesCB.setBackground(BG_PANEL);
        matricesCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        matricesCB.setSelected(currentConfig.matricesEnabled());
        matricesCB.addActionListener(e ->
                updateConfig(config -> config.matricesEnabled(matricesCB.isSelected()))
        );
        panel.add(matricesCB);

        JCheckBox unitsCB = new JCheckBox("Unit Conversions (100 meters in feet)");
        unitsCB.setBackground(BG_PANEL);
        unitsCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        unitsCB.setSelected(currentConfig.unitsEnabled());
        unitsCB.addActionListener(e ->
                updateConfig(config -> config.unitsEnabled(unitsCB.isSelected()))
        );
        panel.add(unitsCB);

        JCheckBox comprehensionsCB = new JCheckBox("List Comprehensions ({x^2 for x in 1..10})");
        comprehensionsCB.setBackground(BG_PANEL);
        comprehensionsCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        comprehensionsCB.setSelected(currentConfig.comprehensionsEnabled());
        comprehensionsCB.addActionListener(e ->
                updateConfig(config -> config.comprehensionsEnabled(comprehensionsCB.isSelected()))
        );
        panel.add(comprehensionsCB);

        JCheckBox lambdasCB = new JCheckBox("Lambda Expressions (x -> x^2)");
        lambdasCB.setBackground(BG_PANEL);
        lambdasCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        lambdasCB.setSelected(currentConfig.lambdasEnabled());
        lambdasCB.addActionListener(e ->
                updateConfig(config -> config.lambdasEnabled(lambdasCB.isSelected()))
        );
        panel.add(lambdasCB);

        JCheckBox userFunctionsCB = new JCheckBox("User-Defined Functions (f(x) := x^2)");
        userFunctionsCB.setBackground(BG_PANEL);
        userFunctionsCB.setAlignmentX(Component.LEFT_ALIGNMENT);
        userFunctionsCB.setSelected(currentConfig.userDefinedFunctionsEnabled());
        userFunctionsCB.addActionListener(e ->
                updateConfig(config -> config.userDefinedFunctionsEnabled(userFunctionsCB.isSelected()))
        );
        panel.add(userFunctionsCB);

        // Add glue to push everything to the top
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Helper to update configuration using a builder modifier function.
     * Preserves session state (variables and user-defined functions).
     */
    private void updateConfig(java.util.function.Function<MathEngineConfig.Builder, MathEngineConfig.Builder> modifier) {
        // Start with current config values using toBuilder()
        MathEngineConfig.Builder builder = currentConfig.toBuilder();

        // Apply the modification
        builder = modifier.apply(builder);

        // Build new config and update engine with session preservation
        currentConfig = builder.build();
        mathEngine = mathEngine.withConfig(currentConfig);
        formatter = StringNodeFormatter.withDecimalPlaces(currentConfig.decimalPlaces());

        updateStatusBar();
        updateSessionInfo();
        statusLabel.setText("Configuration updated");
    }

    private void updateVariablesPane() {
        StyledDocument doc = variablesPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            EvaluationContext context = mathEngine.getContext();
            Map<String, NodeConstant> variables = context.getLocalVariables();

            doc.insertString(doc.getLength(), "Session Variables (" + variables.size() + "):\n\n", keywordStyle);

            if (variables.isEmpty()) {
                doc.insertString(doc.getLength(), "No variables defined yet.\n\n", commentStyle);
                doc.insertString(doc.getLength(), "Define variables with: x := 5\n", commentStyle);
            } else {
                // Sort by name
                List<String> names = new ArrayList<>(variables.keySet());
                Collections.sort(names);

                for (String name : names) {
                    NodeConstant value = variables.get(name);

                    doc.insertString(doc.getLength(), name, promptStyle);
                    doc.insertString(doc.getLength(), " = ", commentStyle);

                    String valueStr = formatNode(value);
                    if (valueStr.length() > 100) {
                        valueStr = valueStr.substring(0, 97) + "...";
                    }
                    doc.insertString(doc.getLength(), valueStr, normalStyle);

                    doc.insertString(doc.getLength(), "  (" + value.getClass().getSimpleName() + ")", commentStyle);
                    doc.insertString(doc.getLength(), "\n", normalStyle);
                }
            }

        } catch (BadLocationException e) {
            // Ignore
        }
    }

    private void updateFunctionsPane() {
        StyledDocument doc = functionsPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            // Show user-defined functions first
            EvaluationContext context = mathEngine.getContext();
            Map<String, FunctionDefinition> userFunctions =
                    context.getLocalFunctions();

            if (!userFunctions.isEmpty()) {
                doc.insertString(doc.getLength(), "User-Defined Functions (" + userFunctions.size() + "):\n\n", keywordStyle);

                List<String> names = new ArrayList<>(userFunctions.keySet());
                Collections.sort(names);

                for (String name : names) {
                    FunctionDefinition func = userFunctions.get(name);
                    doc.insertString(doc.getLength(), "  " + name, promptStyle);
                    doc.insertString(doc.getLength(), "(" + String.join(", ", func.parameters()) + ")", normalStyle);
                    doc.insertString(doc.getLength(), "\n", normalStyle);
                }

                doc.insertString(doc.getLength(), "\n", normalStyle);
            }

            // Get built-in functions from the actual engine grouped by category
            Map<MathFunction.Category,
                    List<MathFunction>> functionsByCategory =
                    mathEngine.getFunctionsByCategory();

            doc.insertString(doc.getLength(), "Built-In Functions by Category:\n\n", keywordStyle);

            // Display each category
            for (MathFunction.Category category :
                    MathFunction.Category.values()) {

                List<MathFunction> functions =
                        functionsByCategory.get(category);

                if (functions == null || functions.isEmpty()) {
                    continue;
                }

                // Format category name (e.g., NUMBER_THEORY -> Number Theory)
                String categoryName = formatCategoryName(category.name());
                doc.insertString(doc.getLength(), "• " + categoryName + ":\n", resultStyle);

                // Collect unique function names (since a function may appear multiple times via aliases)
                Set<String> uniqueNames = new LinkedHashSet<>();
                for (MathFunction func : functions) {
                    uniqueNames.add(func.name());
                }

                // Display function names in wrapped lines
                StringBuilder line = new StringBuilder("  ");
                for (String name : uniqueNames) {
                    if (line.length() + name.length() + 2 > 60) {
                        doc.insertString(doc.getLength(), line.toString() + "\n", normalStyle);
                        line = new StringBuilder("  ");
                    }
                    line.append(name).append(", ");
                }
                if (line.length() > 2) {
                    String lineStr = line.toString();
                    lineStr = lineStr.substring(0, lineStr.length() - 2); // Remove trailing comma
                    doc.insertString(doc.getLength(), lineStr + "\n", normalStyle);
                }
                doc.insertString(doc.getLength(), "\n", normalStyle);
            }

            // Total count
            Set<String> allFunctionNames = mathEngine.getAllFunctionNames();
            doc.insertString(doc.getLength(), String.format("Total: %d function names (including aliases)\n",
                    allFunctionNames.size()), commentStyle);

        } catch (BadLocationException e) {
            // Ignore
        }
    }

    private String formatCategoryName(String categoryName) {
        // Convert UPPER_SNAKE_CASE to Title Case
        String[] words = categoryName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            result.append(word.substring(1));
        }
        return result.toString();
    }

    private void updateConstantsPane() {
        StyledDocument doc = constantsPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            // Get constants from the actual engine
            List<ConstantDefinition> constants =
                    mathEngine.getAllConstants();

            doc.insertString(doc.getLength(), "Mathematical Constants:\n\n", keywordStyle);

            for (ConstantDefinition constant : constants) {
                // Display primary name and aliases
                String names = constant.name();
                if (!constant.aliases().isEmpty()) {
                    names += ", " + String.join(", ", constant.aliases());
                }

                doc.insertString(doc.getLength(), String.format("%-25s", names), promptStyle);
                doc.insertString(doc.getLength(), " = ", commentStyle);

                // Display value
                String valueStr = constant.value().toString();
                if (valueStr.length() > 20) {
                    valueStr = valueStr.substring(0, 17) + "...";
                }
                doc.insertString(doc.getLength(), String.format("%-20s", valueStr), numberStyle);

                // Display description if available
                if (constant.description() != null && !constant.description().isEmpty()) {
                    doc.insertString(doc.getLength(), " (" + constant.description() + ")", commentStyle);
                }

                doc.insertString(doc.getLength(), "\n", normalStyle);
            }

            doc.insertString(doc.getLength(), "\n", normalStyle);
            doc.insertString(doc.getLength(), String.format("Total: %d constants defined\n", constants.size()), commentStyle);
        } catch (BadLocationException e) {
            // Ignore
        }
    }

    private void updateUnitsPane() {
        StyledDocument doc = unitsPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());

            // Get units from the actual engine
            Collection<UnitDefinition> units =
                    mathEngine.getAllUnits();

            doc.insertString(doc.getLength(), "Unit Conversions:\n\n", keywordStyle);

            // Group units by type (category)
            Map<String, List<UnitDefinition>> unitsByType =
                    new TreeMap<>();

            for (UnitDefinition unit : units) {
                unitsByType.computeIfAbsent(unit.type(), k -> new ArrayList<>()).add(unit);
            }

            // Display each type/category
            for (Map.Entry<String, List<UnitDefinition>> entry :
                    unitsByType.entrySet()) {

                // Format type name (e.g., "length" -> "Length")
                String typeName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                doc.insertString(doc.getLength(), "• " + typeName + ":\n", resultStyle);

                // Display units in this category
                List<UnitDefinition> typeUnits = entry.getValue();
                StringBuilder line = new StringBuilder("  ");

                for (UnitDefinition unit : typeUnits) {
                    String unitName = unit.singularName();

                    // Add short form/alias if available
                    if (!unit.aliases().isEmpty()) {
                        String firstAlias = unit.aliases().getFirst();
                        if (firstAlias.length() <= 4) { // Show short aliases
                            unitName += " (" + firstAlias + ")";
                        }
                    }

                    if (line.length() + unitName.length() + 2 > 60) {
                        doc.insertString(doc.getLength(), line + "\n", normalStyle);
                        line = new StringBuilder("  ");
                    }
                    line.append(unitName).append(", ");
                }

                if (line.length() > 2) {
                    String lineStr = line.toString();
                    lineStr = lineStr.substring(0, lineStr.length() - 2); // Remove trailing comma
                    doc.insertString(doc.getLength(), lineStr + "\n", normalStyle);
                }
                doc.insertString(doc.getLength(), "\n", normalStyle);
            }

            doc.insertString(doc.getLength(), String.format("Total: %d units across %d categories\n",
                    units.size(), unitsByType.size()), commentStyle);
            doc.insertString(doc.getLength(), "\nUsage: <value> <unit> in <target_unit>\n", commentStyle);
            doc.insertString(doc.getLength(), "Example: 100 meters in feet\n", commentStyle);

        } catch (BadLocationException e) {
            // Ignore
        }
    }

    private void resetEngine() {
        mathEngine = MathEngine.create(currentConfig);
        updateSessionInfo();
        statusLabel.setText("Engine reset - all variables and functions cleared");

        try {
            StyledDocument doc = outputPane.getStyledDocument();
            doc.insertString(doc.getLength(), "─────────────────────────────────────────────────────────\n", commentStyle);
            doc.insertString(doc.getLength(), "Engine reset.\n\n", keywordStyle);
        } catch (BadLocationException e) {
            // Ignore
        }
    }
}
