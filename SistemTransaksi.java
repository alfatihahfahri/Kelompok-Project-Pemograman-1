/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package kasircoffeeshop;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author user
 */
public class SistemTransaksi extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SistemTransaksi.class.getName());
    private Map<Integer, Integer> produkMap = new HashMap<>();
    private int nomorItem = 0;

    /**
     * Creates new form SistemTransaksi
     */
    public SistemTransaksi() {
        initComponents();
        setupListeners();
        loadProduk();
    }

    private void setupListeners() {
        jButton1.addActionListener(e -> tambahItem());
        jButton2.addActionListener(e -> simpanTransaksi());
        jButton3.addActionListener(e -> bersih());
        jButton4.addActionListener(e -> print());
        jButton5.addActionListener(e -> kembaliKeMenu());
        jComboBox1.addActionListener(e -> updateHarga());
    }

    private void loadProduk() {
        jComboBox1.removeAllItems();
        java.sql.Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            String query = "SELECT id_menu, nama_menu, harga FROM tb_menu ORDER BY nama_menu";
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int idMenu = rs.getInt("id_menu");
                String namaProduk = rs.getString("nama_menu");
                int harga = rs.getInt("harga");
                produkMap.put(idMenu, harga);
                jComboBox1.addItem(namaProduk + " (Rp" + harga + ")");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error load produk: " + ex.getMessage());
        }
    }

    private void updateHarga() {
        if (jComboBox1.getSelectedIndex() == -1) return;
        String selected = (String) jComboBox1.getSelectedItem();
        String[] parts = selected.split("\\(Rp");
        if (parts.length > 1) {
            String hargaStr = parts[1].replace(")", "");
            jTextField1.setText(hargaStr);
        }
    }

    private void tambahItem() {
        if (jComboBox1.getSelectedIndex() == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih produk terlebih dahulu!");
            return;
        }
        if (jTextField2.getText().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus diisi!");
            return;
        }

        try {
            String namaProduk = jComboBox1.getSelectedItem().toString().split("\\(")[0].trim();
            int harga = Integer.parseInt(jTextField1.getText());
            int jumlah = Integer.parseInt(jTextField2.getText());
            int subtotal = harga * jumlah;

            nomorItem++;
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
            model.addRow(new Object[]{nomorItem, namaProduk, harga, jumlah, subtotal});

            jTextField2.setText("");
            jComboBox1.requestFocus();
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Format input salah!");
        }
    }

    private void simpanTransaksi() {
        if (jTable1.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Belanja masih kosong!");
            return;
        }

        java.sql.Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            conn.setAutoCommit(false);
            int totalBelanja = 0;
            for (int i = 0; i < jTable1.getRowCount(); i++) {
                totalBelanja += (int) jTable1.getValueAt(i, 4);
            }

            String queryTransaksi = "INSERT INTO tb_transaksi (tanggal, id_kasir, total) VALUES (NOW(), ?, ?)";
            java.sql.PreparedStatement ps1 = conn.prepareStatement(queryTransaksi, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps1.setInt(1, UserSession.idKasir);
            ps1.setInt(2, totalBelanja);
            ps1.executeUpdate();

            java.sql.ResultSet rs = ps1.getGeneratedKeys();
            int idTransaksi = 0;
            if (rs.next()) {
                idTransaksi = rs.getInt(1);
            }
            ps1.close();

            String queryDetail = "INSERT INTO tb_detail_transaksi (id_transaksi, id_menu, harga_satuan, jumlah, subtotal) VALUES (?, ?, ?, ?, ?)";
            java.sql.PreparedStatement ps2 = conn.prepareStatement(queryDetail);

            for (int i = 0; i < jTable1.getRowCount(); i++) {
                String namaProduk = (String) jTable1.getValueAt(i, 1);
                int harga = (int) jTable1.getValueAt(i, 2);
                int jumlah = (int) jTable1.getValueAt(i, 3);
                int subtotal = (int) jTable1.getValueAt(i, 4);

                int idMenu = getIdMenuByNama(namaProduk);
                ps2.setInt(1, idTransaksi);
                ps2.setInt(2, idMenu);
                ps2.setInt(3, harga);
                ps2.setInt(4, jumlah);
                ps2.setInt(5, subtotal);
                ps2.addBatch();
            }
            ps2.executeBatch();
            ps2.close();

            conn.commit();
            conn.setAutoCommit(true);
            javax.swing.JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!");
            bersih();
        } catch (java.sql.SQLException ex) {
            try {
                conn.rollback();
            } catch (java.sql.SQLException e) {}
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private int getIdMenuByNama(String namaProduk) {
        java.sql.Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return -1;

        try {
            String query = "SELECT id_menu FROM tb_menu WHERE nama_menu = ?";
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, namaProduk);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_menu");
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (java.sql.SQLException ex) {}
        return -1;
    }

    private void bersih() {
        jComboBox1.setSelectedIndex(-1);
        jTextField1.setText("");
        jTextField2.setText("");
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        nomorItem = 0;
    }

    private void kembaliKeMenu() {
        SistemKasir menuUtama = new SistemKasir();
        menuUtama.setVisible(true);
        this.dispose();
    }

    private void print() {
        if (jTable1.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Belanja masih kosong!");
            return;
        }

        try {
            int total = 0;
            StringBuilder html = new StringBuilder();
            html.append("<html><head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<title>INVOICE - KASIR COFFEESHOP</title>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            html.append("h1 { text-align: center; color: #654321; }");
            html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
            html.append("th, td { border: 1px solid #654321; padding: 10px; text-align: left; }");
            html.append("th { background-color: #654321; color: white; }");
            html.append(".total { text-align: right; font-weight: bold; font-size: 16px; }");
            html.append("</style>");
            html.append("</head><body>");
            html.append("<h1>KASIR COFFEESHOP</h1>");
            html.append("<p><strong>Kasir:</strong> ").append(UserSession.namaKasir).append("</p>");
            html.append("<p><strong>Tanggal:</strong> ").append(new java.util.Date()).append("</p>");
            html.append("<table>");
            html.append("<tr><th>No</th><th>Nama Produk</th><th>Harga</th><th>Jumlah</th><th>Subtotal</th></tr>");

            for (int i = 0; i < jTable1.getRowCount(); i++) {
                int no = (int) jTable1.getValueAt(i, 0);
                String nama = (String) jTable1.getValueAt(i, 1);
                int harga = (int) jTable1.getValueAt(i, 2);
                int jumlah = (int) jTable1.getValueAt(i, 3);
                int subtotal = (int) jTable1.getValueAt(i, 4);
                total += subtotal;

                html.append("<tr>");
                html.append("<td>").append(no).append("</td>");
                html.append("<td>").append(nama).append("</td>");
                html.append("<td>Rp").append(harga).append("</td>");
                html.append("<td>").append(jumlah).append("</td>");
                html.append("<td>Rp").append(subtotal).append("</td>");
                html.append("</tr>");
            }

            html.append("</table>");
            html.append("<p class='total'>TOTAL: Rp").append(total).append("</p>");
            html.append("<p style='text-align: center; margin-top: 30px;'>Terima Kasih</p>");
            html.append("</body></html>");

            String timestamp = String.valueOf(System.currentTimeMillis());
            String filePath = System.getProperty("user.home") + "/Invoices_KasirCoffeeshop_" + timestamp + ".html";
            java.nio.file.Files.write(java.nio.file.Paths.get(filePath), html.toString().getBytes());

            int open = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Invoice berhasil dibuat!\nBuka file? (Klik Yes untuk buka di browser)",
                    "Print Invoice", javax.swing.JOptionPane.YES_NO_OPTION);

            if (open == javax.swing.JOptionPane.YES_OPTION) {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
                    }
                } catch (Exception e) {}
            }
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error cetak invoice: " + ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(250, 240, 230));

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel1.setText("Nama Produk");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel2.setText("Harga");

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel3.setText("Jumlah");

        jButton1.setBackground(new java.awt.Color(250, 240, 230));
        jButton1.setFont(new java.awt.Font("DialogInput", 1, 14)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/shopping-cart_1124171.png"))); // NOI18N
        jButton1.setText("Tambah");

        jLabel4.setFont(new java.awt.Font("DialogInput", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(101, 51, 0));
        jLabel4.setText("Daftar Belanja");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "No", "Nama Produk", "Harga", "Jumlah", "Subtotal"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton2.setBackground(new java.awt.Color(0, 204, 102));
        jButton2.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/save 20x20.png"))); // NOI18N
        jButton2.setText("Simpan");

        jButton3.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/bersih.png"))); // NOI18N
        jButton3.setText("Bersih");

        jButton4.setBackground(new java.awt.Color(51, 153, 255));
        jButton4.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/printer_684906.png"))); // NOI18N
        jButton4.setText("Print");

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/coffe_cup_40x40.png"))); // NOI18N

        jButton5.setFont(new java.awt.Font("DialogInput", 1, 14)); // NOI18N
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/left-arrow_12334759.png"))); // NOI18N
        jButton5.setText("Back");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(3, 3, 3)
                            .addComponent(jButton2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(130, 130, 130)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 755, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jComboBox1, 0, 190, Short.MAX_VALUE)
                                    .addComponent(jTextField1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton1))
                                .addGap(0, 483, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel5))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>                        

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new SistemTransaksi().setVisible(true));
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration                   
}
