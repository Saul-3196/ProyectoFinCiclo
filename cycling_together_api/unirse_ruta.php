<?php
include 'conexion.php';

$id_ruta = $_POST['id_ruta'];
$id_usuario = $_POST['id_usuario'];

$sql = "INSERT IGNORE INTO participantes_ruta (id_ruta, id_usuario) VALUES ('$id_ruta', '$id_usuario')";

if ($conexion->query($sql) === TRUE) {
    echo "success";
} else {
    echo "error";
}
$conexion->close();
?>