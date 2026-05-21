<?php
//métodos de envío de datos
$username = $_POST["nombre"];
$password = $_POST["password"];

$conexion = new mysqli("localhost","root","","panel");

$sql = "SELECT * FROM usuarios WHERE nombre = '$username' AND password = '$password'";

$resultado = $conexion->query($sql);

if($resultado->num_rows > 0){
	echo json_encode(array("Success"=>"True"));
}else{
	echo json_encode(array("Success"=>"False"));
}


$conexion->close();

?>