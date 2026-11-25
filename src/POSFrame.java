import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class POSFrame extends JFrame {

    private final List<Product> products = new ArrayList<>();
    private final List<CartItem> cart = new ArrayList<>();
    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    private JTable productTable;
    private JTable cartTable;
    private JTextField qtyField;
    private JLabel selectedLabel;
    private JLabel totalLabel;
    private JLabel pointsLabel;
    private JTextArea receiptArea;

    private JButton addButton;
    private JButton checkoutButton;
    private JButton printButton;
    private JMenuItem exitItem;
    private JMenuItem aboutItem;

    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;

    public POSFrame() {
        setTitle("POIN Off-Sales - Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initProductData();
        initComponents();
        bindEvents();

        setSize(1000, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initProductData() {
        products.add(new Product("P001", "Air Mineral 600ml", 3000));
        products.add(new Product("P002", "Kopi Sachet", 5000));
        products.add(new Product("P003", "Roti isi", 8000));
        products.add(new Product("P004", "Snack Keripik", 6000));
        products.add(new Product("P005", "Minuman Botol", 12000));
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // bar menu
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        exitItem = new JMenuItem("Exit");
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        //panel kiri produk
        JPanel productPanel = new JPanel(new BorderLayout(0, 8));
        productPanel.setBorder(BorderFactory.createTitledBorder("Produk"));

        productModel = new DefaultTableModel(
                new Object[] {"ID", "Nama Produk", "Harga (Rp)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Product p : products) {
            productModel.addRow(new Object[] {
                    p.getId(), p.getName(), p.getPrice()
            });
        }

        productTable = new JTable(productModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        selectedLabel = new JLabel("Pilih produk dari tabel di kiri");
        JPanel selectedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        selectedPanel.add(selectedLabel);

        qtyField = new JTextField("1", 4);
        addButton = new JButton("Add to Cart");
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        qtyPanel.add(new JLabel("Qty:"));
        qtyPanel.add(qtyField);
        qtyPanel.add(addButton);

        JPanel productBottomPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        productBottomPanel.add(selectedPanel);
        productBottomPanel.add(qtyPanel);
        productPanel.add(productBottomPanel, BorderLayout.SOUTH);

        //panel Kanan keranjang
        JPanel cartPanel = new JPanel(new BorderLayout(0, 8));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Keranjang"));

        cartModel = new DefaultTableModel(
                new Object[] {"ID", "Nama Produk", "Qty", "Harga", "Subtotal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        totalLabel = new JLabel("Total: " + formatRupiah(0), SwingConstants.CENTER);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));

        pointsLabel = new JLabel("Points: 0", SwingConstants.CENTER);

        JPanel labelsPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        labelsPanel.add(totalLabel);
        labelsPanel.add(pointsLabel);

        checkoutButton = new JButton("Checkout");
        printButton = new JButton("Cetak");

        JPanel buttonsGrid = new JPanel(new GridLayout(2, 1, 0, 4));
        buttonsGrid.add(checkoutButton);
        buttonsGrid.add(printButton);

        JPanel buttonsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        buttonsWrapper.add(buttonsGrid);

        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.add(labelsPanel, BorderLayout.NORTH);
        summaryPanel.add(buttonsWrapper, BorderLayout.CENTER);

        receiptArea = new JTextArea(8, 30);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel receiptPanel = new JPanel(new BorderLayout(0, 4));
        receiptPanel.add(new JLabel("Struk:", SwingConstants.CENTER), BorderLayout.NORTH);
        receiptPanel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);

        JPanel cartBottomPanel = new JPanel(new BorderLayout(0, 8));
        cartBottomPanel.add(summaryPanel, BorderLayout.NORTH);
        cartBottomPanel.add(receiptPanel, BorderLayout.CENTER);

        cartPanel.add(cartBottomPanel, BorderLayout.SOUTH);

        //panel utama
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mainPanel.add(productPanel);
        mainPanel.add(cartPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void bindEvents() {
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                Product selected = products.get(row);
                selectedLabel.setText(
                        "Dipilih: " + selected.getId() +
                                " - " + selected.getName() +
                                " (" + formatRupiah(selected.getPrice()) + ")"
                );
            }
        });

        addButton.addActionListener(e -> addSelectedProductToCart());
        checkoutButton.addActionListener(e -> checkout());
        printButton.addActionListener(e -> printReceipt());

        exitItem.addActionListener(e -> dispose());
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "POIN Off-Sales - Praktikum Java Swing",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );
    }

    private void addSelectedProductToCart() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pilih produk terlebih dahulu dari tabel.",
                    "Produk belum dipilih",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String qtyText = qtyField.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Qty wajib diisi.",
                    "Input tidak valid",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Qty harus berupa bilangan bulat.",
                    "Input tidak valid",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (qty <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Qty harus lebih dari 0.",
                    "Input tidak valid",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Product selected = products.get(row);
        boolean found = false;
        for (int i = 0; i < cart.size(); i++) {
            CartItem existing = cart.get(i);
            if (existing.getProduct().getId().equals(selected.getId())) {
                cart.set(i, new CartItem(selected, existing.getQty() + qty));
                found = true;
                break;
            }
        }
        if (!found) {
            cart.add(new CartItem(selected, qty));
        }

        refreshCartTable();
        updateSummary();
        qtyField.setText("1");
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (CartItem item : cart) {
            Product p = item.getProduct();
            cartModel.addRow(new Object[] {
                    p.getId(),
                    p.getName(),
                    item.getQty(),
                    p.getPrice(),
                    item.getSubtotal()
            });
        }
    }

    private void updateSummary() {
        int total = 0;
        for (CartItem item : cart) {
            total += item.getSubtotal();
        }
        int points = total / 1000;

        totalLabel.setText("Total: " + formatRupiah(total));
        pointsLabel.setText("Points: " + points);
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Keranjang masih kosong.",
                    "Checkout tidak dapat diproses",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int total = 0;
        StringBuilder receipt = new StringBuilder();
        receipt.append("===== POIN OFF-SALES =====\n");
        receipt.append("Toko: Demo Toko\n");
        receipt.append("========================================\n");
        receipt.append(String.format("%-4s %-18s %3s %12s%n", "ID", "Nama", "Qty", "Subtotal"));
        receipt.append("----------------------------------------\n");

        for (CartItem item : cart) {
            Product p = item.getProduct();
            int subtotal = item.getSubtotal();
            total += subtotal;
            receipt.append(String.format(
                    "%-4s %-18s %3d %12s%n",
                    p.getId(),
                    p.getName(),
                    item.getQty(),
                    formatRupiah(subtotal)
            ));
        }

        int points = total / 1000;
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("TOTAL:          %s%n", formatRupiah(total)));
        receipt.append(String.format("POINTS DIDAPAT: %d (1 point per Rp 1000)%n", points));
        receipt.append("========================================\n");
        receipt.append("Terima kasih! Silakan kunjungi kembali.\n");

        receiptArea.setText(receipt.toString());
        receiptArea.setCaretPosition(0);

        cart.clear();
        refreshCartTable();
        updateSummary();
    }

    private void printReceipt() {
        if (receiptArea.getText().isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Belum ada struk yang dapat dicetak.",
                    "Struk kosong",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            receiptArea.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Gagal mencetak: " + ex.getMessage(),
                    "Error Print",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String formatRupiah(int amount) {
        return currencyFormat.format(amount);
    }
}