Cycling Together 🚲 - You'll never Ride Alone.
¡Bienvenido a Cycling Together! Este proyecto es un ecosistema tecnológico completo diseñado para conectar a la comunidad ciclista local. Nace de una necesidad real en el deporte: ayudar a los ciclistas a encontrar compañeros de su mismo nivel o modalidad, y eliminar el miedo a explorar rutas desconocidas en solitario.
Este ecosistema constituye mi trabajo de fin de ciclo para el Grado Superior de Desarrollo Multiplataforma.

📂ESTRUCTURA DEL REPOSITORIO

El proyecto está organizado de froma limpia y modular:

1. App con el código fuente de la aplicación de Android.
2. cycling_together_api: Contiene el backend del ecosistema. Una API REST construida en PHP que centraliza la lógica de negocio y conecta la interfaz móvil con la base de datos.
3. Panel de Administración de escriotorio: Contiene las vistas del panel y su código backend. Este Panel permite a los usuarios moderadores ejercer un control total sobre la aplicación.

🚴‍♂️CARACTERÍSTICAS DE LA APLICACIÓN
🔐 Autenticación de Usuarios: Registro e inicio de sesión seguro para los ciclistas.
🔍 Buscador de Rutas con Filtros: Panel interactivo para buscar rutas filtrando por localidad, nivel de dificultad (baja, media, alta, extrema) y modalidad de bicicleta.
🗺️ Integración con Google Maps: Visualización detallada del trazado de la ruta en el mapa a partir de coordenadas comprimidas (*Polylines*), incluyendo la marca del punto de encuentro.
👥 Sistema de Inscripción: Contador en tiempo real (`Apuntados`) que muestra el número y los nombres de los participantes que se han unido a la grupeta para esa salida.
⚙️ Perfil Personalizado: Configuración del perfil de usuario indicando la modalidad principal (Carretera, MTB, Gravel, E-Bike) y otros campos.

⚒️TECNOLOGÍAS EMPLEADAS
1. Lenguaje: Kotlin (Desarrollo nativo en Android Studio).
2. Arquitectura de Navegación: *Navigation Component* para garantizar un flujo suave basado en una única actividad (*Single Activity Architecture*).
3. Volley para la gestión asíncrona de peticiones HTTP y consumo de datos en formato JSON.
4. Google Maps SDK para Android.

📐 Arquitectura General del Ecosistema

Aunque este repositorio aloja el cliente móvil, la aplicación está integrada dentro de un ecosistema descentralizado:

1. Cliente Móvil (Este repositorio): Desarrollado en Kotlin, realiza peticiones asíncronas HTTP a un servidor externo.
2. Backend (API REST): Desarrollado en PHP, encargado de recibir los parámetros de la app, procesar la lógica de negocio y comunicarse con el almacenamiento.
3. Base de Datos: Estructura relacional en MySQL que asegura la persistencia de usuarios, rutas, roles y el control de participantes mediante restricciones de clave foránea.

🔧 Retos Técnicos Superados en Android

1. Trazado Eficiente de Rutas: Integración de *Polylines* para decodificar cadenas alfanuméricas provenientes del servidor y dibujar los recorridos en el mapa sin penalizar el rendimiento del dispositivo.
2. Gestión del Ciclo de Vida y Sesiones: Control estricto de la pila de actividades y uso de *SharedPreferences* para evitar "sesiones fantasma" o retornos no deseados a pantallas de login tras la autenticación.
3. Control de Concurrencia en la UI: Desactivación temporal de componentes interactivos (como botones de acción) durante el envío de peticiones para prevenir llamadas duplicadas a la API mientras se aguarda la respuesta del servidor.
