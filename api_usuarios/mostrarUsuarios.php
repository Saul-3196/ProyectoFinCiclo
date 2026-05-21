<?php
$conexion = new mysqli("localhost", "root", "", "panel");

$sql = "SELECT nombre FROM usuarios";
$resultado = $conexion->query($sql);

$lista_usuarios = array();
while($row = $resultado->fetch_assoc()){
	$lista_usuarios [] = array("nombre"=>$row["nombre"]);
}

echo json_encode($lista_usuarios);
$conexion->close();


?>