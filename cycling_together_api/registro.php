<?php
include 'conexion.php';

// Recibimos los datos enviados por POST desde Android
$nombre = $_POST['nombre'];
$email = $_POST['email'];
$password = $_POST['password'];

// 1. Comprobamos si el usuario existe 
$sql_check = "SELECT * FROM usuarios WHERE email = '$email'";
$result = $conexion->query($sql_check);

if($result->num_rows > 0){
    echo json_encode(array("status"=>"error", "message"=>"Este email ya está registrado"));
} else {
    // Ciframos la contraseña
    $password_cifrada = password_hash($password, PASSWORD_BCRYPT);
    
    // 2. Insertamos el nuevo usuario
    $sql_insert = "INSERT INTO usuarios (nombre, email, password, id_rol) VALUES ('$nombre','$email','$password_cifrada', 2)";

    // 3. Ejecución de la consulta 
    if($conexion->query($sql_insert) === TRUE){
        // 4. Respuesta JSON (Corregido: status con 't')
        echo json_encode(array("status"=>"success", 
            "message"=>"Usuario registrado correctamente",
            ));
    } else {
        echo json_encode(array("status"=>"error", "message"=>"Error al registrar el usuario: " . $conexion->error));
    }
}
$conexion->close();
?>