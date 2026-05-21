<?php
// borrar_usuario.php
include 'conexion.php';

if (isset($_POST['id_usuario'])) {
    $id_usuario = $_POST['id_usuario'];
    
    // Primero, por si este usuario ha creado rutas, ponemos su id_creador a NULL 
    // para que las rutas no desaparezcan, pero queden "huerfanas" (como indica tu BD)
    $sql_rutas = "UPDATE rutas SET id_creador = NULL WHERE id_creador = ?";
    $stmt_rutas = $conexion->prepare($sql_rutas);
    $stmt_rutas->bind_param("i", $id_usuario);
    $stmt_rutas->execute();
    $stmt_rutas->close();

    // Ahora sí, borramos al usuario (sus inscripciones se borran solas por el ON DELETE CASCADE)
    $sql = "DELETE FROM usuarios WHERE id_usuario = ?";
    $stmt = $conexion->prepare($sql);
    $stmt->bind_param("i", $id_usuario);
    
    if ($stmt->execute()) {
        echo "Exito";
    } else {
        echo "Error: " . $conexion->error;
    }
    
    $stmt->close();
} else {
    echo "Error: Falta el ID del usuario.";
}
$conexion->close();
?>