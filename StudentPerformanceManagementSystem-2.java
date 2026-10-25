import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Student Performance Management System
 * Single-file Java Swing mini project.
 *
 * Run:
 *   javac StudentPerformanceManagementSystem.java
 *   java StudentPerformanceManagementSystem
 *
 * Data is saved to students.dat in the same folder.
 */
public class StudentPerformanceManagementSystem extends JFrame {
    private static final String DATA_FILE = "students.dat";
    private static final String[] SUBJECTS = {
        "Mathematics", "Physics", "Chemistry", "Programming", "DBMS"
    };

    private final List<Student> students = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"Roll No.", "Name", "Department", "Total / 500", "Percentage", "Grade"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField rollField = new JTextField(12);
    private final JTextField nameField = new JTextField(16);
    private final JTextField departmentField = new JTextField(14);
    private final JTextField searchField = new JTextField(16);
    private final JTextField[] markFields = new JTextField[SUBJECTS.length];
    private final JLabel summaryLabel = new JLabel("Students: 0");
    private final JLabel resultLabel = new JLabel("Select a student to view results.");

    public StudentPerformanceManagementSystem() {
        super("Student Performance Management System");
        loadStudents();
        buildUI();
        refreshTable("");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 690);
        setMinimumSize(new Dimension(850, 560));
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(242, 246, 252));

        JLabel title = new JLabel("  STUDENT PERFORMANCE MANAGEMENT SYSTEM");
        title.setOpaque(true);
        title.setBackground(new Color(24, 43, 73));
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard & Students", buildStudentsPanel());
        tabs.addTab("Marks Entry", buildMarksPanel());
        tabs.addTab("Results & Rankings", buildResultsPanel());
        add(tabs, BorderLayout.CENTER);

        summaryLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(summaryLabel, BorderLayout.SOUTH);
    }

    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Register Student"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        addFormField(form, c, 0, "Roll number:", rollField);
        addFormField(form, c, 1, "Full name:", nameField);
        addFormField(form, c, 2, "Department:", departmentField);

        JButton addButton = button("Add Student");
        JButton updateButton = button("Update Selected");
        JButton deleteButton = button("Delete Selected");
        JButton clearButton = button("Clear Form");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(clearButton);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(actions, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search name / roll / department:"));
        searchPanel.add(searchField);
        JButton searchButton = button("Search");
        JButton showAllButton = button("Show All");
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);
        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(searchPanel, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateSelectedStudent());
        deleteButton.addActionListener(e -> deleteSelectedStudent());
        clearButton.addActionListener(e -> clearStudentForm());
        searchButton.addActionListener(e -> refreshTable(searchField.getText().trim()));
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable("");
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });
        return panel;
    }

    private JPanel buildMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Enter marks (0–100)"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Student roll number:"), c);
        JTextField marksRollField = new JTextField(15);
        c.gridx = 1;
        form.add(marksRollField, c);

        for (int i = 0; i < SUBJECTS.length; i++) {
            c.gridy = i + 1; c.gridx = 0;
            form.add(new JLabel(SUBJECTS[i] + ":"), c);
            markFields[i] = new JTextField(8);
            c.gridx = 1;
            form.add(markFields[i], c);
        }
        JButton saveMarksButton = button("Save Marks");
        JButton loadMarksButton = button("Load Student Marks");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(saveMarksButton);
        buttons.add(loadMarksButton);
        c.gridy = SUBJECTS.length + 1; c.gridx = 0; c.gridwidth = 2;
        form.add(buttons, c);

        JTextArea note = new JTextArea(
            "Instructions:\n" +
            "• Register a student in the Dashboard tab first.\n" +
            "• Enter a roll number and marks from 0 to 100 for each subject.\n" +
            "• Saving marks updates the student's result and ranking.\n" +
            "• Grade: A+ (90+), A (80+), B (70+), C (60+), D (40+), F (<40)."
        );
        note.setEditable(false);
        note.setOpaque(false);
        note.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(form, BorderLayout.NORTH);
        panel.add(note, BorderLayout.CENTER);

        saveMarksButton.addActionListener(e -> {
            String roll = marksRollField.getText().trim();
            Student student = findStudent(roll);
            if (student == null) {
                showError("Student not found. Register the student first.");
                return;
            }
            try {
                int[] marks = new int[SUBJECTS.length];
                for (int i = 0; i < marks.length; i++) {
                    String value = markFields[i].getText().trim();
                    if (value.isEmpty()) throw new IllegalArgumentException("Enter marks for " + SUBJECTS[i] + ".");
                    marks[i] = Integer.parseInt(value);
                    if (marks[i] < 0 || marks[i] > 100)
                        throw new IllegalArgumentException(SUBJECTS[i] + " marks must be between 0 and 100.");
                }
                student.marks = marks;
                saveStudents();
                refreshTable(searchField.getText().trim());
                JOptionPane.showMessageDialog(this, "Marks saved successfully for " + student.name + ".");
            } catch (NumberFormatException ex) {
                showError("Marks must be whole numbers from 0 to 100.");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        loadMarksButton.addActionListener(e -> {
            Student student = findStudent(marksRollField.getText().trim());
            if (student == null) {
                showError("Student not found.");
                return;
            }
            for (int i = 0; i < SUBJECTS.length; i++)
                markFields[i].setText(student.marks == null ? "" : String.valueOf(student.marks[i]));
        });

        // Keep the roll field accessible via the panel's client property.
        panel.putClientProperty("marksRollField", marksRollField);
        return panel;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        resultLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultLabel.setBorder(BorderFactory.createTitledBorder("Selected Student Result"));
        panel.add(resultLabel, BorderLayout.NORTH);

        DefaultTableModel rankingModel = new DefaultTableModel(
            new String[]{"Rank", "Roll No.", "Name", "Department", "Total / 500", "Percentage", "Grade"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable rankingTable = new JTable(rankingModel);
        rankingTable.setRowHeight(25);
        panel.add(new JScrollPane(rankingTable), BorderLayout.CENTER);

        JButton refresh = button("Refresh Results & Rankings");
        JButton report = button("Show Selected Student Result");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(refresh);
        buttons.add(report);
        panel.add(buttons, BorderLayout.SOUTH);

        Runnable updateRanking = () -> {
            rankingModel.setRowCount(0);
            List<Student> sorted = new ArrayList<>(students);
            sorted.sort((a, b) -> Integer.compare(b.total(), a.total()));
            int rank = 1;
            for (Student s : sorted) {
                rankingModel.addRow(new Object[]{rank++, s.roll, s.name, s.department,
                    s.total(), String.format(Locale.US, "%.2f%%", s.percentage()), s.grade()});
            }
        };
        refresh.addActionListener(e -> updateRanking.run());
        report.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected < 0) {
                showError("Select a student in the Dashboard & Students tab first.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(selected);
            String roll = String.valueOf(tableModel.getValueAt(modelRow, 0));
            Student s = findStudent(roll);
            if (s != null) showStudentResult(s);
        });
        updateRanking.run();
        return panel;
    }

    private void addFormField(JPanel panel, GridBagConstraints c, int row, String label, JTextField field) {
        c.gridy = row; c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(field, c);
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(37, 99, 235));
        b.setForeground(Color.WHITE);
        return b;
    }

    private void addStudent() {
        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        if (roll.isEmpty() || name.isEmpty() || department.isEmpty()) {
            showError("Please enter roll number, full name, and department.");
            return;
        }
        if (findStudent(roll) != null) {
            showError("A student with this roll number already exists.");
            return;
        }
        students.add(new Student(roll, name, department));
        saveStudents();
        refreshTable("");
        clearStudentForm();
        JOptionPane.showMessageDialog(this, "Student registered successfully.");
    }

    private void updateSelectedStudent() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Select a student from the table first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        String oldRoll = String.valueOf(tableModel.getValueAt(modelRow, 0));
        Student student = findStudent(oldRoll);
        String newRoll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        if (student == null) return;
        if (newRoll.isEmpty() || name.isEmpty() || department.isEmpty()) {
            showError("All student fields are required.");
            return;
        }
        Student duplicate = findStudent(newRoll);
        if (duplicate != null && duplicate != student) {
            showError("Another student already uses that roll number.");
            return;
        }
        student.roll = newRoll;
        student.name = name;
        student.department = department;
        saveStudents();
        refreshTable(searchField.getText().trim());
        JOptionPane.showMessageDialog(this, "Student updated successfully.");
    }

    private void deleteSelectedStudent() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Select a student to delete.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        String roll = String.valueOf(tableModel.getValueAt(modelRow, 0));
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete student " + roll + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            students.removeIf(s -> s.roll.equalsIgnoreCase(roll));
            saveStudents();
            refreshTable(searchField.getText().trim());
            clearStudentForm();
        }
    }

    private void fillFormFromSelection() {
        int selected = table.getSelectedRow();
        if (selected < 0) return;
        int modelRow = table.convertRowIndexToModel(selected);
        String roll = String.valueOf(tableModel.getValueAt(modelRow, 0));
        Student s = findStudent(roll);
        if (s != null) {
            rollField.setText(s.roll);
            nameField.setText(s.name);
            departmentField.setText(s.department);
        }
    }

    private void clearStudentForm() {
        rollField.setText("");
        nameField.setText("");
        departmentField.setText("");
        table.clearSelection();
    }

    private void refreshTable(String query) {
        tableModel.setRowCount(0);
        String q = query.toLowerCase(Locale.ROOT);
        for (Student s : students) {
            if (q.isEmpty() || s.roll.toLowerCase(Locale.ROOT).contains(q)
                || s.name.toLowerCase(Locale.ROOT).contains(q)
                || s.department.toLowerCase(Locale.ROOT).contains(q)) {
                tableModel.addRow(new Object[]{s.roll, s.name, s.department, s.total(),
                    String.format(Locale.US, "%.2f%%", s.percentage()), s.grade()});
            }
        }
        long withMarks = students.stream().filter(s -> s.marks != null).count();
        summaryLabel.setText("Students: " + students.size() + "     |     Results entered: " +
            withMarks + "     |     Subjects per student: " + SUBJECTS.length);
    }

    private Student findStudent(String roll) {
        for (Student s : students)
            if (s.roll.equalsIgnoreCase(roll)) return s;
        return null;
    }

    private void showStudentResult(Student s) {
        StringBuilder text = new StringBuilder("<html><b>" + escapeHtml(s.name) + "</b> (" +
            escapeHtml(s.roll) + ")<br>Department: " + escapeHtml(s.department) + "<br>");
        if (s.marks == null) {
            text.append("Marks have not been entered yet.</html>");
        } else {
            text.append("<br>");
            for (int i = 0; i < SUBJECTS.length; i++)
                text.append(escapeHtml(SUBJECTS[i])).append(": ").append(s.marks[i]).append("/100<br>");
            text.append("<br>Total: ").append(s.total()).append("/500")
                .append(" &nbsp; Percentage: ").append(String.format(Locale.US, "%.2f%%", s.percentage()))
                .append(" &nbsp; Grade: ").append(s.grade()).append("</html>");
        }
        resultLabel.setText(text.toString());
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Check Input", JOptionPane.WARNING_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    private void loadStudents() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object data = in.readObject();
            if (data instanceof ArrayList<?>) {
                for (Object item : (ArrayList<?>) data)
                    if (item instanceof Student) students.add((Student) item);
            }
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null,
                "Could not load saved data. A new empty list will be used.\n" + ex.getMessage(),
                "Data Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveStudents() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(new ArrayList<>(students));
        } catch (IOException ex) {
            showError("Could not save student data: " + ex.getMessage());
        }
    }

    private static class Student implements Serializable {
        private static final long serialVersionUID = 1L;
        String roll;
        String name;
        String department;
        int[] marks;

        Student(String roll, String name, String department) {
            this.roll = roll;
            this.name = name;
            this.department = department;
            this.marks = null;
        }

        int total() {
            if (marks == null) return 0;
            int sum = 0;
            for (int mark : marks) sum += mark;
            return sum;
        }

        double percentage() {
            return marks == null ? 0.0 : total() / (double) (SUBJECTS.length * 100) * 100.0;
        }

        String grade() {
            if (marks == null) return "Not graded";
            double p = percentage();
            if (p >= 90) return "A+";
            if (p >= 80) return "A";
            if (p >= 70) return "B";
            if (p >= 60) return "C";
            if (p >= 40) return "D";
            return "F";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new StudentPerformanceManagementSystem().setVisible(true);
        });
    }
}
