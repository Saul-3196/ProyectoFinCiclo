<?php
$username = $_POST["nombre"];
$password = $_POST["password"];

$conexion = new mysqli("localhost","root","","panel");

$sql = "INSERT INTO usuarios (nombre, password) VALUES ('$username','$password')";

if($conexion->query($sql) === TRUE){
	echo json_encode(array("Success"=>"True"));
}else{
	echo json_encode(array("Success"=>"False"));
}

$conexion->close();

?>