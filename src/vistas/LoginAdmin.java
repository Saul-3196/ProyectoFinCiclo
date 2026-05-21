package vistas;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.SwingConstants;
import java.awt.Font;

public class LoginAdmin extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtEmail;
	private JPasswordField txtPassword;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginAdmin frame = new LoginAdmin();
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
	public LoginAdmin() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(243, 173, 78));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Email");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel.setBounds(28, 55, 75, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Contraseña");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setBounds(28, 91, 75, 14);
		contentPane.add(lblNewLabel_1);
		
		txtEmail = new JTextField();
		txtEmail.setBounds(150, 52, 144, 20);
		contentPane.add(txtEmail);
		txtEmail.setColumns(10);
		
		txtPassword = new JPasswordField();
		txtPassword.setBounds(150, 88, 144, 20);
		contentPane.add(txtPassword);
		
		JButton btnEntrar = new JButton("Entrar");
		btnEntrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String email = txtEmail.getText();
				String password = new String(txtPassword.getPassword());

				try {
				    // Se establece la conexión con el archivo
				    URL url = new URL("http://localhost/cycling_together_api/login.php");
				    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				    conn.setRequestMethod("POST");
				    conn.setDoOutput(true);
				    
				    String parametros = "email=" + email + "&password=" + password;
				    
				    OutputStream os = conn.getOutputStream();
				    os.write(parametros.getBytes());
				    os.flush();
				    os.close();
				    // Se lee la respuesta del servidor
				    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				    StringBuilder respuesta = new StringBuilder();
				    String linea;
				    while ((linea = br.readLine()) != null) {
				        respuesta.append(linea);
				    }
				    br.close();
				    
				    String jsonDevuelto = respuesta.toString();
				    //Mensaje de debug
				    System.out.println("El servidor responde: " + jsonDevuelto);
				    
				    // Comprobamos si el usuario que quiere acceder tiene el rol de administrador (Rol 1)
				    if (jsonDevuelto.contains("\"id_rol\":\"1\"") || jsonDevuelto.contains("\"id_rol\":1")) {
				        javax.swing.JOptionPane.showMessageDialog(null, "Acceso Concedido, Bienvenido");
				        PanelAdmin ventanaPrincipal = new PanelAdmin();
				        ventanaPrincipal.setVisible(true);
				        dispose(); 
				    } else {
				        javax.swing.JOptionPane.showMessageDialog(null, "Acceso Denegado. Correo/Clave incorrectos o no tienes permisos de Admin.", "Error de Login", javax.swing.JOptionPane.ERROR_MESSAGE);
				    }
				} catch (Exception ex) {
				    ex.printStackTrace();
				    javax.swing.JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos. Asegúrate de arrancar XAMPP", "Fallo de Red", javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnEntrar.setBounds(177, 165, 89, 23);
		contentPane.add(btnEntrar);
		
		JLabel lblNewLabel_2 = new JLabel("CYCLING TOGETHER");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(112, 11, 215, 30);
		contentPane.add(lblNewLabel_2);

	}
}
