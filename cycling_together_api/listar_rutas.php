<?php
include 'conexion.php';

// Cambiamos el SELECT para que solo coja rutas de HOY en adelante
// Y además, las ordenamos para que las más próximas salgan las primeras
$sql = "SELECT r.*, u.nombre as nombre_creador, b.modalidad,
        (SELECT COUNT(*) FROM participantes_ruta pr WHERE pr.id_ruta = r.id_ruta) AS num_participantes,
        (SELECT GROUP_CONCAT(u_part.nombre SEPARATOR ', ') 
         FROM participantes_ruta pr 
         INNER JOIN usuarios u_part ON pr.id_usuario = u_part.id_usuario 
         WHERE pr.id_ruta = r.id_ruta) AS nombres_participantes
        FROM rutas r 
        LEFT JOIN usuarios u ON r.id_creador = u.id_usuario 
        LEFT JOIN tipos_bici b ON r.id_bici = b.id_bici
        WHERE r.fecha >= CURDATE()
        ORDER BY r.fecha ASC, r.hora ASC";

$resultado = $conexion->query($sql);
$rutas = array();

if($resultado->num_rows > 0){
    while($fila = $resultado->fetch_assoc()){
        $rutas[] = $fila;
    }
}

echo json_encode($rutas, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);

$conexion->close();
?>