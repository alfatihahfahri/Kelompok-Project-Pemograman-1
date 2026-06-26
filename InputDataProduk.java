/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package kasircoffeeshop;

/**
 *
 * @author user
 */
public class InputDataProduk extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(InputDataProduk.class.getName());
    private int idMenuSelected = -1;

    /**
     * Creates new form InputDataProduk
     */
    public InputDataProduk() {
        initComponents();
        styleTableHeader();
        styleButtons();
        updateLabel4and5();
        loadData();
        jButton1.addActionListener(e -> simpan());
        jButton2.addActionListener(e -> edit());
        jButton3.addActionListener(e -> hapus());
        jButton4.addActionListener(e -> bersih());
        jButton5.addActionListener(e -> kembaliKeMenu());
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableRowSelected(evt);
            }
        });
    }

    private void styleTableHeader() {
        jTable1.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            {
                setBackground(new java.awt.Color(139, 69, 19));
                setForeground(java.awt.Color.WHITE);
                setFont(getFont().deriveFont(java.awt.Font.BOLD));
                setHorizontalAlignment(javax.swing.JLabel.CENTER);
            }
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        jTable1.setBackground(java.awt.Color.WHITE);
    }

    private void styleButtons() {
        jButton1.setBackground(new java.awt.Color(76, 175, 80));
        jButton1.setForeground(java.awt.Color.WHITE);
        jButton1.setFont(jButton1.getFont().deriveFont(java.awt.Font.BOLD));

        jButton2.setBackground(new java.awt.Color(33, 150, 243));
        jButton2.setForeground(java.awt.Color.WHITE);
        jButton2.setFont(jButton2.getFont().deriveFont(java.awt.Font.BOLD));

        jButton3.setBackground(new java.awt.Color(244, 67, 54));
        jButton3.setForeground(java.awt.Color.WHITE);
        jButton3.setFont(jButton3.getFont().deriveFont(java.awt.Font.BOLD));

        jButton4.setBackground(java.awt.Color.WHITE);
        jButton4.setForeground(java.awt.Color.BLACK);
        jButton4.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.GRAY));
    }

    private void updateLabel4and5() {
        jLabel4.setText("Harga");
        jLabel5.setText("Stok");
    }

    private void loadData() {
        java.sql.Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);

            String query = "SELECT id_menu, nama_menu, harga, stok FROM tb_menu";
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getInt("harga"),
                    rs.getInt("stok")
                });
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error load data: " + ex.getMessage());
        }
    }

    private void tableRowSelected(java.awt.event.MouseEvent evt) {
        int row = jTable1.getSelectedRow();
        if (row != -1) {
            idMenuSelected = (int) jTable1.getValueAt(row, 0);
            jTextField1.setText(String.valueOf(idMenuSelected));
            jTextField2.setText((String) jTable1.getValueAt(row, 1));
            jTextField4.setText(String.valueOf(jTable1.getValueAt(row, 2)));
            jTextField3.setText(String.valueOf(jTable1.getValueAt(row, 3)));
        }
    }

    private void simpan() {
        if (jTextField2.getText().isEmpty() || jTextField3.getText().isEmpty() || jTextField4.getText().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        String namaMenu = jTextField2.getText();
        int harga = Integer.parseInt(jTextField4.getText());
        int stok = Integer.parseInt(jTextField3.getText());

        java.sql.Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            if (idMenuSelected != -1) {
                String query = "UPDATE tb_menu SET nama_menu = ?, harga = ?, stok = ? WHERE id_menu = ?";
                java.sql.PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, namaMenu);
                ps.setInt(2, harga);
                ps.setInt(3, stok);
                ps.setInt(4, idMenuSelected);
                ps.executeUpdate();
                ps.close();
                conn.close();
                javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil diupdate!");
            } else {
                String query = "INSERT INTO tb_menu (nama_menu, harga, stok) VALUES (?, ?, ?)";
                java.sql.PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, namaMenu);
                ps.setInt(2, harga);
                ps.setInt(3, stok);
                ps.executeUpdate();
                ps.close();
                conn.close();
                javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!");
            }
            bersih();
            loadData();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void edit() {
        if (idMenuSelected == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih produk dari tabel terlebih dahulu!");
            return;
        }

        if (jTextField2.getText().isEmpty() || jTextField3.getText().isEmpty() || jTextField4.getText().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return;
        }

        String namaMenu = jTextField2.getText();
        int harga = Integer.parseInt(jTextField4.getText());
        int stok = Integer.parseInt(jTextField3.getText());

        java.sql.Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            String query = "UPDATE tb_menu SET nama_menu = ?, harga = ?, stok = ? WHERE id_menu = ?";
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, namaMenu);
            ps.setInt(2, harga);
            ps.setInt(3, stok);
            ps.setInt(4, idMenuSelected);
            ps.executeUpdate();
            ps.close();
            conn.close();

            javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil diupdate!");
            bersih();
            loadData();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void hapus() {
        if (idMenuSelected == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih produk dari tabel terlebih dahulu!");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Yakin hapus produk ini?", "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;

        java.sql.Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try {
            String query = "DELETE FROM tb_menu WHERE id_menu = ?";
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, idMenuSelected);
            ps.executeUpdate();
            ps.close();
            conn.close();

            javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!");
            bersih();
            loadData();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void bersih() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        idMenuSelected = -1;
        jTable1.clearSelection();
    }

    private void kembaliKeMenu() {
        SistemKasir menuUtama = new SistemKasir();
        menuUtama.setVisible(true);
        this.dispose();
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTable1.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            {
                setBackground(new java.awt.Color(139, 69, 19));
                setForeground(java.awt.Color.WHITE);
                setFont(getFont().deriveFont(java.awt.Font.BOLD));
                setHorizontalAlignment(javax.swing.JLabel.CENTER);
            }
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        jTable1.setBackground(java.awt.Color.WHITE);
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(250, 240, 230));

        jPanel2.setBackground(new java.awt.Color(103, 51, 0));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 8, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("DialogInput", 1, 14)); // NOI18N
        jLabel1.setText("Input Data Produk");

        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel2.setText("ID produk");

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel3.setText("Nama Produk");

        jTextField2.addActionListener(this::jTextField2ActionPerformed);

        jLabel4.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel4.setText("Stok");

        jLabel5.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel5.setText("Harga");

        jTextField4.addActionListener(this::jTextField4ActionPerformed);

        jButton1.setBackground(new java.awt.Color(0, 204, 51));
        jButton1.setFont(new java.awt.Font("DialogInput", 1, 13)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/save 20x20.png"))); // NOI18N
        jButton1.setText("Simpan");

        jButton2.setBackground(new java.awt.Color(0, 153, 255));
        jButton2.setFont(new java.awt.Font("DialogInput", 1, 13)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/edit 20x20.png"))); // NOI18N
        jButton2.setText("Edit");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setBackground(new java.awt.Color(255, 51, 51));
        jButton3.setFont(new java.awt.Font("DialogInput", 1, 13)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/delete 20x20.png"))); // NOI18N
        jButton3.setText("Hapus");

        jButton4.setFont(new java.awt.Font("DialogInput", 1, 13)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/bersih.png"))); // NOI18N
        jButton4.setText("Bersih");

        jLabel6.setFont(new java.awt.Font("DialogInput", 1, 14)); // NOI18N
        jLabel6.setText("Daftar Produk");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID Produk", "Nama Produk", "Harga", "Stok"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton5.setFont(new java.awt.Font("DialogInput", 1, 14)); // NOI18N
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kasircoffeeshop/left-arrow_12334759.png"))); // NOI18N
        jButton5.setText("Back");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(25, 25, 25)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel4)
                                                    .addComponent(jLabel5))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel2)
                                                    .addComponent(jLabel3))
                                                .addGap(21, 21, 21)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(271, 271, 271)))
                                .addGap(18, 18, 18)
                                .addComponent(jButton1)
                                .addGap(7, 7, 7)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5))
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton5)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(51, 51, 51))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(41, 41, 41)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addContainerGap())
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

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {                                            
        // TODO add your handling code here:
    }                                           

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {                                            
        // TODO add your handling code here:
    }                                           

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
    }                                        

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
        java.awt.EventQueue.invokeLater(() -> new InputDataProduk().setVisible(true));
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration                   
}
