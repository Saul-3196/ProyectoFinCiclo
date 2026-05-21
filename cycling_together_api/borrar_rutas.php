<?php
include 'conexion.php';

// Verificamos que la petición sea POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    
    // Recogemos el ID de la ruta enviado desde Android
    $id_ruta = $_POST['id_ruta'];

    // Validamos que el ID no esté vacío
    if (!empty($id_ruta)) {
        // Preparamos la consulta
        $consulta = "DELETE FROM rutas WHERE id_ruta = '$id_ruta'";
        
        if (mysqli_query($conexion, $consulta)) {
            // Si se borró correctamente
            echo json_encode([
                "status" => "success", 
                "message" => "Ruta eliminada correctamente de la base de datos"
            ]);
        } else {
            // Si hubo un error en la consulta
            echo json_encode([
                "status" => "error", 
                "message" => "Error al ejecutar el borrado: " . mysqli_error($conexion)
            ]);
        }
    } else {
        echo json_encode([
            "status" => "error", 
            "message" => "ID de ruta no recibido"
        ]);
    }

    mysqli_close($conexion);
} else {
    echo json_encode([
        "status" => "error", 
        "message" => "Método no permitido"
    ]);
}
?>