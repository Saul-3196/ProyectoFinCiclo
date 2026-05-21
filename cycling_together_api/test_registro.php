<?php
include 'conexion.php';

$nombre = 'Sonia Fernández';
$email = 'sonia@sonia.com';
$password = 'sonia';
$id_rol = 2;
$id_ciudad = 1;
$id_bici = 1;

$password_cifrada = password_hash($password, PASSWORD_BCRYPT);

$sql = "INSERT INTO usuarios (nombre, email, password, id_rol, id_ciudad, id_bici,token) VALUES ('$nombre', '$email', '$password_cifrada', $id_rol, $id_ciudad, $id_bici,NULL)";

if($conexion->query($sql) === TRUE){
	echo "Usuario de prueba insertado correctamente";
}else{
	echo "Error de conexión :" . $conexion->error;
}

$conexion->close();

?>