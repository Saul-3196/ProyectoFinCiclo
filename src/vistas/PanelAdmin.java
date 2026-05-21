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

public class PanelAdmin extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable tablaRutas;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PanelAdmin frame = new PanelAdmin();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public PanelAdmin() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(243, 173, 78));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("PANEL DE CONTROL - GESTOR DE RUTAS");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblNewLabel.setBackground(new Color(255, 128, 64));
		lblNewLabel.setBounds(211, 29, 400, 41);
		contentPane.add(lblNewLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 81, 788, 413);
		contentPane.add(scrollPane);
		
		tablaRutas = new JTable();
		tablaRutas.setBackground(new Color(173, 216, 230));
		scrollPane.setColumnHeaderView(tablaRutas);
		
		JButton btnCargar = new JButton("Cargar Rutas");
		btnCargar.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnCargar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					//Establecemos la conexión con nuestro archivo
				    URL url = new URL("http://localhost/cycling_together_api/listar_rutas.php");
				    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				    conn.setRequestMethod("GET");
				    
				    // Lectura de la respuesta del servidor
				    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				    StringBuilder respuesta = new StringBuilder();
				    String linea;
				    while ((linea = br.readLine()) != null) {
				        respuesta.append(linea);
				    }
				    br.close();
				    
				    // Conversión de la respuesta del servidor a un JSON
				    String jsonDevuelto = respuesta.toString();
				    System.out.println("El PHP devuelve esto: " + jsonDevuelto);
				    org.json.JSONArray jsonArray = new org.json.JSONArray(jsonDevuelto);
				    
				    // Preparación del modelo para la visualización
				    javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel();
				    modelo.addColumn("ID Ruta");
				    modelo.addColumn("Nombre de la Ruta");
				    modelo.addColumn("Localidad de Inicio");
				    modelo.addColumn("Distancia (km)");
				    modelo.addColumn("Dificultad");
				    modelo.addColumn("Fecha");
				    
				    // Se recorre el JSON y se inserta cada ruta como una fila dentro de la tabla
				    for (int i = 0; i < jsonArray.length(); i++) {
				        org.json.JSONObject ruta = jsonArray.getJSONObject(i);
				        
				        modelo.addRow(new Object[]{
				            "ID:" + ruta.getString("id_ruta"),
				            "Título: " + ruta.getString("titulo"),
				            "Punto de Encuentro: " + ruta.get("localidad"),
				            "KMs: " + ruta.getString("distancia"),
				            "Dificultad: " + ruta.getString("dificultad"),
				            "Fecha: " + ruta.getString("fecha")
				        });
				    }
				    
				    tablaRutas.setModel(modelo);
				 // Ajustes del ancho de las columnas
				    tablaRutas.getColumnModel().getColumn(0).setPreferredWidth(40);
				    tablaRutas.getColumnModel().getColumn(1).setPreferredWidth(180);
				    tablaRutas.getColumnModel().getColumn(2).setPreferredWidth(180);
				    tablaRutas.getColumnModel().getColumn(3).setPreferredWidth(60);
				    tablaRutas.getColumnModel().getColumn(4).setPreferredWidth(120);
				    tablaRutas.getColumnModel().getColumn(5).setPreferredWidth(140);
				    
				} catch (Exception ex) {
				    ex.printStackTrace();
				    javax.swing.JOptionPane.showMessageDialog(null, "Error al cargar las rutas.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnCargar.setBackground(new Color(0, 255, 128));
		btnCargar.setBounds(83, 516, 192, 23);
		contentPane.add(btnCargar);
		
		JButton btnBorrar = new JButton("Borrar Ruta Seleccionada");
		btnBorrar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnBorrar.setBackground(new Color(255, 0, 0));
        btnBorrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaRutas.getSelectedRow();
                // Mensaje informativo en caso de no seleccionar una ruta
                if (filaSeleccionada == -1) {
                    javax.swing.JOptionPane.showMessageDialog(null, "Por favor, haz clic sobre la ruta que desees borrar.", "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Quitamos el "ID", lo reemplazamos por una cadena vacía para que el servidor sólo coja el ID de la ruta
                String idConTexto = tablaRutas.getValueAt(filaSeleccionada, 0).toString();
                String idRuta = idConTexto.replace("ID:", "").replace("ID: ", "").trim();

                int confirmacion = javax.swing.JOptionPane.showConfirmDialog(null, 
                    "¿Estás seguro de que quieres eliminar la ruta con ID " + idRuta + "?\nEsta acción borrará también a los participantes inscritos en ella.", 
                    "Confirmar Borrado", 
                    javax.swing.JOptionPane.YES_NO_OPTION, 
                    javax.swing.JOptionPane.QUESTION_MESSAGE);

                if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
                    try {
                        URL url = new URL("http://localhost/cycling_together_api/borrar_rutas.php");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setDoOutput(true);
                        
                        String parametros = "id_ruta=" + idRuta;
                        OutputStream os = conn.getOutputStream();
                        os.write(parametros.getBytes());
                        os.flush();
                        os.close();
                        
                        // Leemos la respuesta y la imprimimos
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder respuestaPHP = new StringBuilder();
                        String linea;
                        while ((linea = br.readLine()) != null) {
                            respuestaPHP.append(linea);
                        }
                        br.close();
                        
                        System.out.println("Respuesta de borrar_rutas.php: " + respuestaPHP.toString());
                        
                        javax.swing.JOptionPane.showMessageDialog(null, "¡Ruta eliminada con éxito!");
                        // Automáticamente se recarga la tabla de las rutas
                        btnCargar.doClick();
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javax.swing.JOptionPane.showMessageDialog(null, "Error de red al intentar borrar la ruta.", "Fallo de conexión", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        btnBorrar.setBounds(321, 516, 192, 23);
        contentPane.add(btnBorrar);
        
        JButton btnUsuarios = new JButton("Gestionar Usuarios");
        btnUsuarios.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		PanelUsuarios ventanaUsuarios = new PanelUsuarios();
                ventanaUsuarios.setVisible(true);
                dispose();
        	}
        });
        btnUsuarios.setBackground(new Color(192, 192, 192));
        btnUsuarios.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnUsuarios.setBounds(570, 517, 163, 23);
        contentPane.add(btnUsuarios);
	}
}
    
