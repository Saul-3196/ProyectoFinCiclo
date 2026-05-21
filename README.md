Cycling Together 🚲 - You'll never Ride Alone.
¡Bienvenido a Cycling Together! Este proyecto es un ecosistema tecnológico completo diseñado para conectar a la comunidad ciclista local. Nace de una necesidad real en el deporte: ayudar a los ciclistas a encontrar compañeros de su mismo nivel o modalidad, y eliminar el miedo a explorar rutas desconocidas en solitario.
Este ecosistema constituye mi trabajo de fin de ciclo para el Grado Superior de Desarrollo Multiplataforma.

Este repositorio contiene el código fuente de la aplicación móvil de la aplicación para móvil de Android.

🚴‍♂️CARACTERÍSTICAS DE LA APLICACIÓN
🔐 Autenticación de Usuarios: Registro e inicio de sesión seguro para los ciclistas.
🔍 Buscador de Rutas con Filtros: Panel interactivo para buscar rutas filtrando por localidad, nivel de dificultad (baja, media, alta, extrema) y modalidad de bicicleta.
🗺️ Integración con Google Maps: Visualización detallada del trazado de la ruta en el mapa a partir de coordenadas comprimidas (*Polylines*), incluyendo la marca del punto de encuentro.
👥 Sistema de Inscripción: Contador en tiempo real (`Apuntados`) que muestra el número y los nombres de los participantes que se han unido a la grupeta para esa salida.
⚙️ Perfil Personalizado: Configuración del perfil de usuario indicando la modalidad principal (Carretera, MTB, Gravel, E-Bike) y otros campos.

⚒️TECNOLOGÍAS EMPLEADAS
Lenguaje: Kotlin (Desarrollo nativo en Android Studio).
Arquitectura de Navegación: *Navigation Component* para garantizar un flujo suave basado en una única actividad (*Single Activity Architecture*).
Volley para la gestión asíncrona de peticiones HTTP y consumo de datos en formato JSON.
Google Maps SDK para Android.

📐 Arquitectura General del Ecosistema

Aunque este repositorio aloja el cliente móvil, la aplicación está integrada dentro de un ecosistema descentralizado:

1. Cliente Móvil (Este repositorio): Desarrollado en Kotlin, realiza peticiones asíncronas HTTP a un servidor externo.
2. Backend (API REST): Desarrollado en PHP, encargado de recibir los parámetros de la app, procesar la lógica de negocio y comunicarse con el almacenamiento.
3. Base de Datos: Estructura relacional en MySQL que asegura la persistencia de usuarios, rutas, roles y el control de participantes mediante restricciones de clave foránea.

 🔧 Retos Técnicos Superados en Android

* Trazado Eficiente de Rutas: Integración de *Polylines* para decodificar cadenas alfanuméricas provenientes del servidor y dibujar los recorridos en el mapa sin penalizar el rendimiento del dispositivo.
* Gestión del Ciclo de Vida y Sesiones: Control estricto de la pila de actividades y uso de *SharedPreferences* para evitar "sesiones fantasma" o retornos no deseados a pantallas de login tras la autenticación.
* Control de Concurrencia en la UI: Desactivación temporal de componentes interactivos (como botones de acción) durante el envío de peticiones para prevenir llamadas duplicadas a la API mientras se aguarda la respuesta del servidor.
