<?php
include 'conexion.php';

if($_SERVER['REQUEST_METHOD'] == 'POST'){
    // Recogemos todos los datos enviados desde Android
    $titulo = $_POST['titulo'];
    $localidad = $_POST['localidad'];
    $distancia = $_POST['distancia'];
    $desnivel = $_POST['desnivel'];
    $dificultad = $_POST['dificultad'];
    $id_bici = $_POST['id_bici'];
    $fecha = $_POST['fecha'];
    $hora = $_POST['hora'];
    $punto_encuentro = $_POST['punto_encuentro'];
    $descripcion = $_POST['descripcion'];
    $id_creador = $_POST['id_creador'];
    $latitud = $_POST['latitud'];
    $longitud = $_POST['longitud'];
    $mapa_trazado = $_POST['mapa_trazado'];
    $mapa_trazado = mysqli_real_escape_string($conexion, $mapa_trazado);


    // SQL Corregido: 14 campos exactos según tu estructura de tabla 'rutas'
    $sql = "INSERT INTO rutas (titulo, localidad, distancia, desnivel, dificultad, id_bici, fecha, hora, punto_encuentro, descripcion, id_creador, latitud, longitud, mapa_trazado) VALUES ('$titulo', '$localidad', '$distancia', '$desnivel', '$dificultad', '$id_bici', '$fecha', '$hora', '$punto_encuentro', '$descripcion', '$id_creador', '$latitud', '$longitud', '$mapa_trazado')";
    
    if($conexion->query($sql) === TRUE){
        echo json_encode(array("status"=> "success", "message"=> "Ruta creada correctamente"));
    } else {
        echo json_encode(array("status"=> "error", "message"=> "Error SQL: " . $conexion->error));
    }
}
$conexion->close();
?>