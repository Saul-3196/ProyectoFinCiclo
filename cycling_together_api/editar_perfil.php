<?php
include 'conexion.php';

if($_SERVER['REQUEST_METHOD'] == 'POST'){
    $id_usuario = $_POST['id_usuario'];
    $nombre = $_POST['nombre'];
    $email = $_POST['email'];
    $sexo = $_POST['sexo'];
    
    // Tratamiento de la fecha de nacimiento, en caso de venir vacía, se inserta un NULL
    $fecha_nacimiento = !empty($_POST['fecha_nacimiento']) ? "'" . $_POST['fecha_nacimiento'] . "'" : "NULL";
    
    $nivel_usuario = $_POST['nivel_usuario'];
    
    // Tratamiento del id de la bici, en caso de 0, establecemos un NULL
    $id_bici = $_POST['id_bici'] != "0" ? $_POST['id_bici'] : "NULL";
    
    $ciudad = $_POST['ciudad'];

    // Sanitizamos los textos libres neutralizando caracteres especiales
    $nombre = mysqli_real_escape_string($conexion, $nombre);
    $email = mysqli_real_escape_string($conexion, $email);
    $ciudad = mysqli_real_escape_string($conexion, $ciudad);

    // Hacemos el UPDATE
    $sql = "UPDATE usuarios SET 
            nombre = '$nombre', 
            email = '$email', 
            sexo = '$sexo', 
            fecha_nacimiento = $fecha_nacimiento, 
            nivel_usuario = '$nivel_usuario', 
            id_bici = $id_bici,
            ciudad = '$ciudad'
            WHERE id_usuario = $id_usuario";

    if($conexion->query($sql) === TRUE){
        echo json_encode(array("status"=> "success", "message"=> "Perfil actualizado correctamente"));
    } else {
        echo json_encode(array("status"=> "error", "message"=> "Error SQL: " . $conexion->error));
    }
}
$conexion->close();
?>