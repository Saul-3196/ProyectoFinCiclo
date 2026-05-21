<?php
include 'conexion.php';

// Usamos GET para consultar o POST para mayor consistencia con los otros archivos
if($_SERVER['REQUEST_METHOD'] == 'POST' || $_SERVER['REQUEST_METHOD'] == 'GET'){
    
    $id_usuario = isset($_REQUEST['id_usuario']) ? $_REQUEST['id_usuario'] : 0;

    // Consultamos los datos del usuario
    $sql = "SELECT * FROM usuarios WHERE id_usuario = '$id_usuario'";
    $resultado = $conexion->query($sql);

    if($resultado->num_rows > 0){
        $fila = $resultado->fetch_assoc();
        // Devolvemos el usuario encontrado
        echo json_encode($fila, JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(array("error" => "Usuario no encontrado"));
    }
}
$conexion->close();
?>