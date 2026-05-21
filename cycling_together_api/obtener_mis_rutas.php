<?php
include 'conexion.php';

if($_SERVER['REQUEST_METHOD'] == 'POST' || $_SERVER['REQUEST_METHOD'] == 'GET'){
    
    $id_usuario = isset($_REQUEST['id_usuario']) ? $_REQUEST['id_usuario'] : 0;

    // Buscador de rutas en orden de publicación
    $sql = "SELECT * FROM rutas WHERE id_creador = '$id_usuario' ORDER BY fecha DESC";
    $resultado = $conexion->query($sql);

    $rutas = array();
    
    if($resultado->num_rows > 0){
        while($fila = $resultado->fetch_assoc()){
            $rutas[] = $fila;
        }
    }
    
    // Devolvemos el array de rutas protegiendo las barras de la polilínea
    echo json_encode($rutas, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
}
$conexion->close();
?>