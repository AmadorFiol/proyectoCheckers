# Checkers Online

Juego de damas multijugador en tiempo real usando Java 21, Spring Boot, WebSocket y Thymeleaf.

## 🛠️ Tecnologías

- **Backend:** Java 21, Spring Boot 3.2.1
- **Frontend:** HTML5, CSS3, JavaScript (Vanilla)
- **Comunicación:** WebSocket + STOMP
- **Motor de plantillas:** Thymeleaf
- **Build:** Maven

## 📋 Prerrequisitos

- JDK 21 o superior pero anterior a 25
- Maven 3.6+
- Navegador web moderno basado en Chromium

## ⚡ Inicio Rápido

### 1. Clonar el repositorio
```bash
git clone https://github.com/AmadorFiol/proyectoCheckers.git
cd proyectoCheckers
```

### 2. Compilar el proyecto
```bash
mvn clean package
```

### 3. Ejecutar la aplicación
```bash
mvn spring-boot:run
```

### 2.2 y 3.2 Manera alternativa y mas sencillita
Alternativamente una vez clonado el repo lo puedes abrir desde tu IDE de preferencia para proyectos java y situandote en el archivo "CheckersApplication.java" le das al boton de ejecutar

### 4. Acceder a la aplicación
Si estas ejecutando la aplicación en el mismo dispositivo desde el que vas a acceder a la aplicación
- Accede desde tu navegador a: http://localhost:8080

Si vas a acceder a la aplicación desde otro dispositivo en la misma red que el que esta ejecuntandola
- En una terminal (como cmd) usa el comando ipconfig o equivalente en el dispositivo que esta ejecutando la aplicación y encuentra la IP
- En el navegador del otro dispositivo accede a: http://< IP >:8080

## 🎯 Cómo Jugar
⚠️NO PONER CARACTERES ESPECIALES NI EN LOS APODOS NI EN NOMBRES DE LA SALA⚠️
1. **Crear una sala:**
    - Ingresa un nombre para tu sala
    - Ingresa tu apodo
    - Click en "Crear Sala"
    - Comparte el código de sala con tu oponente

2. **Unirse a una sala:**
    - Ingresa el código de sala
    - Ingresa tu apodo
    - Click en "Unirse"

3. **Reglas del juego:**
    - Las fichas blancas mueven primero
    - Solo puedes mover en diagonal
    - Las capturas son obligatorias
    - Llega al otro extremo para coronar tu ficha
    - Gana capturando todas las fichas del oponente o bloqueándolo

## 📁 Estructura del Proyecto
```
src/main/
├── java/com/sjo/checkers/
│   ├── CheckersApplication.java
│   ├── config/         # Configuraciones
│   ├── controller/     # Controladores REST y WebSocket
│   ├── dto/            # Objetos de transferencia de datos
│   ├── model/          # Modelos del dominio
│   ├── service/        # Lógica de negocio
│   └── exception/      # Manejo de excepciones
└── resources/
    ├── static/
    │   ├── css/        # Estilos
    │   └── js/         # JavaScript
    └── templates/      # Vistas Thymeleaf
```

## 🔧 Configuración

Edita `src/main/resources/application.properties`:
```properties
server.port=8080
spring.application.name=checkers
logging.level.com.sjo.checkers=DEBUG
```

## 📝 API Endpoints

### REST
- `GET /` - Página principal
- `GET /room/{roomId}/lobby` - Sala de espera
- `GET /room/{roomId}/game` - Tablero de juego
- `GET /api/rooms` - Lista de salas disponibles
- `GET /api/rooms/{roomId}` - Info de una sala
- `GET /api/rooms/{roomId}/exists` - Verificar sala

### WebSocket
- `/ws-checkers` - Endpoint de conexión
- `/app/room/create` - Crear sala
- `/app/room/join` - Unirse a sala
- `/app/game/move` - Realizar movimiento
- `/topic/room/{roomId}` - Actualizaciones de sala
- `/topic/game/{roomId}` - Actualizaciones del juego


## 👥 Autores

- Amador Fiol Borel