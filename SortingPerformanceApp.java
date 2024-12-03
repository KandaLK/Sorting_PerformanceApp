
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class SortingPerformanceApp extends JFrame {

    private JComboBox<String> columnSelector; //Column Headers
    private JButton sortButton;           // Individual Sorting button
    private JButton performanceButton;      // Button for get Performance of all algo
    private DefaultTableModel tableModel; 
    private JTable table;
    private JTextArea resultArea;
    private List<String[]> csvData;

    private String[] colHead;   // Extract Column headers before sorting
 
    public SortingPerformanceApp() { //App window
        setTitle("Sorting Performance Evaluation App");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initializeComponents();
        setResizable(true);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {            // 
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        //Upload Button GUI
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton uploadButton = new JButton("Upload CSV File");
        uploadButton.setPreferredSize(new Dimension(130, 30));
       
        uploadButton.addActionListener(e -> uploadCSVFile());
        filePanel.add(uploadButton);

        //Column Selection Drop down Button GUI
        columnSelector = new JComboBox<>();
        columnSelector.setPreferredSize(new Dimension(120, 30));
        columnSelector.setEnabled(false);
        filePanel.add(new JLabel("Select Column: "));
        filePanel.add(columnSelector);

        //algorithm Selection Drop down Button GUI
        sortButton = new JButton("Algorithms");
        sortButton.setPreferredSize(new Dimension(110, 30));
        sortButton.setEnabled(false);
        sortButton.addActionListener(e -> sortColumn());
        filePanel.add(sortButton);

        //Test all algorithm GUI
        performanceButton = new JButton("Find Best Algorithm");
        performanceButton.setPreferredSize(new Dimension(150, 30));
        performanceButton.setEnabled(false);
        performanceButton.addActionListener(e -> bestAlgorithm());
        filePanel.add(performanceButton); 

        topPanel.add(filePanel, BorderLayout.CENTER);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("CSV Data Display"));

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane tableScrollPane = new JScrollPane(table);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER); 

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Sorting Results"));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        bottomPanel.add(resultScrollPane, BorderLayout.CENTER);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, bottomPanel);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.7);
        splitPane.setContinuousLayout(true);

        // Add Panels to Frame
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    //
    private void uploadCSVFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

    int result = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();

        // Check if the selected file has a .csv extension
        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            JOptionPane.showMessageDialog(this, "Invalid file type. Please select a .CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
            fileChooser.setSelectedFile(null); // Clear the selected file
            return; // Exit the method
        }

        // Proceed to read the CSV file
        csvData = readCSV(selectedFile);

        if (csvData != null && !csvData.isEmpty()) {
            colHead = csvData.get(0); // Get the Header Values before sort
            csvData.remove(0);
            updateTablePreview();
            populateColumnSelector();
            sortButton.setEnabled(true);
            performanceButton.setEnabled(true);
            columnSelector.setEnabled(true);
            resultArea.setText("");
            performanceButton.setToolTipText("Click to test all the algorithms and find the fastest algorithm!"); // Add tooltip
            sortButton.setToolTipText("Click to select each algorithm and test the sortings"); // Add tooltip
        } else {
            JOptionPane.showMessageDialog(this, "Invalid or Empty csv File ! Try again !");
        }
    }
}

    // Read the .csv file and row by row
    private List<String[]> readCSV(File file) { 
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(","); //split into a array 
                boolean isEmpty = true;  // flag for checks
                for (String value : row) {
                    if (!value.trim().isEmpty()) {
                        isEmpty = false;
                        break;
                    }
                }
                if (!isEmpty) {
                    data.add(row);
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading the file.");
            return null;
        }
        return data;
    }

    
    private void updateTablePreview() {
        if (csvData != null) {
            String[] headers = colHead;
            
            DefaultTableModel model = new DefaultTableModel(headers,0);
        
            for (int i = 1; i < csvData.size(); i++) {
                model.addRow(csvData.get(i));
            }
            table.setModel(model);
        }
    }

    private void populateColumnSelector() {
        columnSelector.removeAllItems();
        if (csvData != null) {
            String[] headers = colHead;   //csvData.get(0)
            for (String header : headers) {
                columnSelector.addItem(header);
            }
        }
    }
    

    private void sortColumn() {
        int columnIndex = columnSelector.getSelectedIndex();
        if (csvData == null || columnIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please upload a CSV file and select a column.");
            return;
        }

        String algorithm = selectSortingAlgorithm();
        if (algorithm == null) {
            return;
        }

        List<String[]> rows = new ArrayList<>(csvData.subList(1, csvData.size()));

        
        long startTime = System.nanoTime();
        switch (algorithm) {
            case "Insertion Sort" ->
                insertionSort(rows, columnIndex);
            case "Shell Sort" ->
                shellSort(rows, columnIndex);
            case "Merge Sort" ->
                rows = mergeSort(rows, columnIndex);
            case "Quick Sort" ->
                quickSort(rows, columnIndex, 0, rows.size() - 1);
            case "Heap Sort" ->
                heapSort(rows, columnIndex);
        }
        long endTime = System.nanoTime();
        

        long executionTimes = (endTime - startTime) ;
        updateSortingResults(algorithm, executionTimes, columnIndex);
        displaySortedData(rows);
        
    }


    // Analyse all the algorithms
    private void bestAlgorithm(){     
        int columnIndex = columnSelector.getSelectedIndex();
        if (csvData == null || columnIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please upload a CSV file and select a column.");
            return;
        }
        
        List<String[]> rows = new ArrayList<>(csvData.subList(1, csvData.size()));
        String fastestAlgorithm = "";
        long fastestTime = Long.MAX_VALUE; 

        
        for (String algorithmss : new String[]{"Insertion Sort", "Shell Sort", "Merge Sort", "Quick Sort", "Heap Sort"}) {
            long startTime = System.nanoTime();

            switch (algorithmss) {
                case "Insertion Sort" ->
                    insertionSort(rows, columnIndex);
                case "Shell Sort" ->
                    shellSort(rows, columnIndex);
                case "Merge Sort" ->
                    rows = mergeSort(rows, columnIndex);
                case "Quick Sort" ->
                    quickSort(rows, columnIndex, 0, rows.size() - 1);
                case "Heap Sort" ->
                    heapSort(rows, columnIndex);
            }

            long endTime = System.nanoTime();
            long executionTimes = (endTime - startTime);
            performanceResultUpdate(algorithmss, executionTimes, columnIndex);

            // Check if this algorithm is the fastest
             if (executionTimes < fastestTime) {
                fastestAlgorithm = algorithmss;
                fastestTime = executionTimes;
                
            }
    
       }
      
         JOptionPane.showMessageDialog(
                null,
                String.format(
                        "Fastest Algorithm : %s\nColumn Name : %s\nExecution Time : %d ms",
                        fastestAlgorithm,
                        columnSelector.getSelectedItem(),
                        fastestTime / 1000000 // Convert nanoseconds to milliseconds
                ),
                "Best Sorting Algorithm !",
                JOptionPane.INFORMATION_MESSAGE
        ); 
   }
    
    
    private String selectSortingAlgorithm() {
        String[] algorithms = {"Insertion Sort", "Shell Sort", "Merge Sort", "Quick Sort", "Heap Sort"};
        return (String) JOptionPane.showInputDialog(
                this,
                "Choose a sorting algorithm :",
                "Sorting Algorithm ",
                JOptionPane.PLAIN_MESSAGE,
                null,
                algorithms,
                algorithms[0]
        );
    }


    
    //Display Result for individual algorithms exec times on textarea
    private void updateSortingResults (String algorithm, long executionTimes, int column ) {
        resultArea.append("\n--- --- --- --- SELECTED SORTING ALGORITHM PERFORMANCE ! --- --- --- ---\n\nAlgorithm Name : " + algorithm + "\n"+"Execution Time: " + (executionTimes/1000000) + " ms\n\n" );
        
        JOptionPane.showMessageDialog(this,"Selected Column :"+ colHead[column]+"\n"+"Algorithm Name : " + algorithm + "\n"
                + "Execution Time : " + (executionTimes/1000000) + " ms\n" ); 
    }



    
    //Display Result for best and other algorithms exec times on textarea
    private void performanceResultUpdate(String algorithm, long executionTime, int column ) {

        switch (algorithm) {
                case "Insertion Sort" ->
                     resultArea.append("""
                                       
                                       ==== ==== ==== PERFORMANCE OF ALGORITHMS ! ==== ==== ====\n
                                       Selected Column :"""+ colHead[column]+"\nAlgorithm Name : " + algorithm + ", Execution Time : " + (executionTime/1000000) + " ms"  );
                case "Shell Sort" ->
                     resultArea.append("""
                                       
                                       
                                       Selected Column :"""+ colHead[column]+"\nAlgorithm Name : " + algorithm + ", Execution Time : " + (executionTime/1000000) + " ms"  );
                case "Merge Sort" ->
                     resultArea.append("""
                                       
                                       
                                       Selected Column :"""+ colHead[column]+"\nAlgorithm Name : " + algorithm + ", Execution Time : " + (executionTime/1000000) + " ms"  );
                case "Quick Sort" ->
                     resultArea.append("""
                                       
                                       
                                       Selected Column :"""+ colHead[column]+"\nAlgorithm Name : " + algorithm + ", Execution Time : " + (executionTime/1000000) + " ms"  );
                case "Heap Sort" ->
                     resultArea.append("""
                                       
                                       
                                       Selected Column :"""+ colHead[column]+"\nAlgorithm Name : " + algorithm + ", Execution Time : " + (executionTime/1000000) + " ms\n"  );
            } 
    }
 


    private void displaySortedData(List<String[]> sortedRows) {
        DefaultTableModel model = new DefaultTableModel(colHead, 0);
        for (String[] row : sortedRows) {
            model.addRow(row);
        }
        table.setModel(model);
    }


    

    private void insertionSort(List<String[]> data, int column) {
        
        for (int i = 1; i < data.size(); i++) {
            String[] key = data.get(i);
            int j = i - 1;
            while (j >= 0 && compare(data.get(j)[column], key[column]) > 0) {
                data.set(j + 1, data.get(j));
                j--;
            }
            data.set(j + 1, key);
        }
    } 

    private void shellSort(List<String[]> data, int column) {
    
        int n = data.size();
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                String[] temp = data.get(i);
                int j;
                for (j = i; j >= gap && compare(data.get(j - gap)[column], temp[column]) > 0; j -= gap) {
                    data.set(j, data.get(j - gap));
                }
                data.set(j, temp);
            }
        }
    }

    private List<String[]> mergeSort(List<String[]> data, int column) {
        if (data.size() <= 1) {
            return data;
        }
        int mid = data.size() / 2;
        List<String[]> left = mergeSort(data.subList(0, mid), column);
        List<String[]> right = mergeSort(data.subList(mid, data.size()), column);
        return merge(left, right, column);
    }

    private List<String[]> merge(List<String[]> left, List<String[]> right, int column) {
        List<String[]> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (compare(left.get(i)[column], right.get(j)[column]) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        result.addAll(left.subList(i, left.size()));
        result.addAll(right.subList(j, right.size()));
        return result;
    }

    private void quickSort(List<String[]> data, int column, int low, int high) {
        if (low >= high) return;
        String pivot = data.get(high)[column];
        int lt = low, gt = high;
        int i = low;

        while (i <= gt) {
            int cmp = compare(data.get(i)[column], pivot);
            if (cmp < 0) {
                Collections.swap(data, lt++, i++);
            } else if (cmp > 0) {
                Collections.swap(data, i, gt--);
            } else {
                i++;
            }
        }

        quickSort(data, column, low, lt - 1);
        quickSort(data, column, gt + 1, high);
    }

    
    private void heapSort(List<String[]> data, int column) {
        int n = data.size();
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(data, column, n, i);
        }
        for (int i = n - 1; i > 0; i--) {
            Collections.swap(data, 0, i);
            heapify(data, column, i, 0);
        }
    }

    private void heapify(List<String[]> data, int column, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && compare(data.get(left)[column], data.get(largest)[column]) > 0) {
            largest = left;
        }
        if (right < n && compare(data.get(right)[column], data.get(largest)[column]) > 0) {
            largest = right;
        }
        if (largest != i) {
            Collections.swap(data, i, largest);
            heapify(data, column, n, largest);
        }
    }

    private int compare(String a, String b) {
        
        try {
            return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SortingPerformanceApp().setVisible(true));
    }
}
