<?php
include 'conexion.php';

$sql = "SELECT id_usuario, nombre, email, nivel_usuario, ciudad, id_rol FROM usuarios";
$resultado = $conexion->query($sql);

$usuarios = array();

if($resultado->num_rows > 0){
	while($fila = $resultado->fetch_assoc()){
		$usuarios[] = $fila;
	}
}

echo json_encode($usuarios);
$conexion->close();

?>