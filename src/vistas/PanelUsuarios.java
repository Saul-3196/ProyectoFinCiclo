package vistas;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.SwingConstants;

public class PanelUsuarios extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTable tablaUsuarios;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    PanelUsuarios frame = new PanelUsuarios();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public PanelUsuarios() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(243, 173, 78));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        JLabel lblTitulo = new JLabel("PANEL DE CONTROL - GESTOR DE USUARIOS");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblTitulo.setBounds(206, 25, 425, 31);
        contentPane.add(lblTitulo);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(0, 81, 788, 413);
        contentPane.add(scrollPane);
        
        tablaUsuarios = new JTable();
        tablaUsuarios.setBackground(new Color(173, 216, 230)); 
        scrollPane.setColumnHeaderView(tablaUsuarios);
        
        JButton btnCargar = new JButton("Cargar Usuarios");
        btnCargar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnCargar.setBackground(new Color(0, 255, 128));
        btnCargar.setBounds(321, 506, 192, 23);
        btnCargar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URL url = new URL("http://localhost/cycling_together_api/listar_usuarios.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder respuesta = new StringBuilder();
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        respuesta.append(linea);
                    }
                    br.close();
                    
                    String jsonDevuelto = respuesta.toString();
                    org.json.JSONArray jsonArray = new org.json.JSONArray(jsonDevuelto);
                    
                    javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel();
                    modelo.addColumn("ID Usuario");
                    modelo.addColumn("Nombre");
                    modelo.addColumn("Email");
                    modelo.addColumn("Nivel");
                    modelo.addColumn("Ciudad");
                    modelo.addColumn("Rol");
                    
                    for (int i = 0; i < jsonArray.length(); i++) {
                        org.json.JSONObject usuario = jsonArray.getJSONObject(i);
                        
                        // Determinamos si es Admin o Usuario normal
                        String rolTexto = usuario.getString("id_rol").equals("1") ? "Admin" : "Usuario";
                        
                        modelo.addRow(new Object[]{
                            "ID: " + usuario.getString("id_usuario"),
                            "Nombre: " + usuario.getString("nombre"),
                            "Email: " + usuario.getString("email"),
                            // Usamos optString por si algún usuario tiene el nivel o ciudad vacío en la BD
                            "Nivel: " + usuario.optString("nivel_usuario", "N/A"),
                            "Población: " + usuario.optString("ciudad", "N/A"),
                            "Rol: " + rolTexto
                        });
                    }
                    
                    tablaUsuarios.setModel(modelo);
                    tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(20);
                    tablaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(160);
                    tablaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(200);
                    tablaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(100);
                    tablaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(120);
                    tablaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(80);
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javax.swing.JOptionPane.showMessageDialog(null, "Error al cargar los usuarios.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        contentPane.add(btnCargar);
        
        JButton btnBorrar = new JButton("Borrar Usuario");
        btnBorrar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnBorrar.setBackground(new Color(255, 0, 0));
        btnBorrar.setBounds(523, 506, 150, 23);
        btnBorrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaUsuarios.getSelectedRow();

                if (filaSeleccionada == -1) {
                    javax.swing.JOptionPane.showMessageDialog(null, "Selecciona un usuario de la tabla.", "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String idConTexto = tablaUsuarios.getValueAt(filaSeleccionada, 0).toString();
                String idUsuario = idConTexto.replace("ID: ", "").trim();
                String nombreUsuario = tablaUsuarios.getValueAt(filaSeleccionada, 1).toString();

                int confirmacion = javax.swing.JOptionPane.showConfirmDialog(null, 
                    "¿Seguro que quieres eliminar al usuario: " + nombreUsuario + "?", 
                    "Confirmar Borrado", 
                    javax.swing.JOptionPane.YES_NO_OPTION, 
                    javax.swing.JOptionPane.WARNING_MESSAGE);

                if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
                    try {
                        URL url = new URL("http://localhost/cycling_together_api/borrar_usuarios.php");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setDoOutput(true);
                        
                        String parametros = "id_usuario=" + idUsuario;
                        OutputStream os = conn.getOutputStream();
                        os.write(parametros.getBytes());
                        os.flush();
                        os.close();
                        
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        br.close();
                        
                        javax.swing.JOptionPane.showMessageDialog(null, "Usuario eliminado correctamente.");
                        btnCargar.doClick(); 
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javax.swing.JOptionPane.showMessageDialog(null, "Error de red al intentar borrar.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        contentPane.add(btnBorrar);
        
        JButton btnVolver = new JButton("Volver a Rutas");
        btnVolver.setBackground(new Color(192, 192, 192));
        btnVolver.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnVolver.setBounds(121, 506, 150, 23);
        btnVolver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PanelAdmin ventanaRutas = new PanelAdmin();
                ventanaRutas.setVisible(true);
                dispose();
            }
        });
        contentPane.add(btnVolver);
        
    }
}