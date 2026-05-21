<?php
$hostname = 'localhost';
$username = 'root';
$password = '';
$database = 'cycling_together';

$conexion = new mysqli($hostname, $username, $password, $database);

if ($conexion->connect_errno) {
    echo "Fallo al conectar a MySQL: " . $conexion->connect_error;
} else {
    //Provisional para visualizar en el navegador que se establece la conexión
    //echo "¡has establecido la conexión!";
}
?>