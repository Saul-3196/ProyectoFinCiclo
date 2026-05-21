<?php
include 'conexion.php';

$id_ruta = $_POST['id_ruta'];
$id_usuario = $_POST['id_usuario'];

$sql = "DELETE FROM participantes_ruta WHERE id_ruta = '$id_ruta' AND id_usuario = '$id_usuario'";

if ($conexion->query($sql) === TRUE) {
    echo "success";
} else {
    echo "error";
}
$conexion->close();
?>