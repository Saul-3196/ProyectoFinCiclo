<?php
include 'conexion.php';

// Recibimos los datos mediante POST desde Android
$email = $_POST['email'];
$password = $_POST['password'];

// Consultamos en la base de datos si el usuario existe
// Quitamos el JOIN de ciudades y ponemos u.ciudad
$sql = "SELECT u.id_usuario, u.nombre, u.email, u.password, u.id_rol, r.nombre_rol, u.ciudad, b.modalidad 
        FROM usuarios u 
        LEFT JOIN roles r ON u.id_rol = r.id_rol 
        LEFT JOIN tipos_bici b ON u.id_bici = b.id_bici 
        WHERE u.email = '$email'";

$result = $conexion->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    
    // Comprobamos que la contraseña del usuario sea correcta
    if (password_verify($password, $row['password'])) {
        
        $token = bin2hex(random_bytes(16));
        $id_usuario = $row['id_usuario'];
        
        // Actualizamos el token
        $sql_update_token = "UPDATE usuarios SET token = '$token' WHERE id_usuario = $id_usuario";
        
        if ($conexion->query($sql_update_token) === TRUE) {
            unset($row['password']); // eliminamos la contraseña por seguridad
            $row['status'] = "success";
            $row['token'] = $token;
            echo json_encode($row);
        }
    } else {
        // Contraseña incorrecta
        echo json_encode(array("status" => "error", "message" => "Contraseña incorrecta"));
    }
} else {
    // Usuario no encontrado 
    echo json_encode(array("status" => "error", "message" => "El usuario no existe"));
}

$conexion->close();
?>