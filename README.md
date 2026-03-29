# 🪐 Antigravity Agent Orchestrator (V3)

Antigravity es un framework de Inteligencia Artificial basado en Java (Spring Boot) y **LangChain4j**. Transforma tu entorno local en una agencia de desarrollo de software completamente autónoma.

Este orquestador no genera simples "respuestas de chat"; coordina un equipo de agentes que **crean archivos reales**, **leen tu código existente**, e **incluso ejecutan comandos en la consola** para testear el código que acaban de escribir.

---

## 🧠 El "Cerebro": Configuración del Modelo de IA

El core del sistema usa **LangChain4j**, lo que significa que el "cerebro" es agnóstico y puedes usar cualquier modelo.

### 1. Configuración Local (Ollama) - Por Defecto
El proyecto viene configurado para usar IA local y gratuita.
1. Instala Ollama (https://ollama.com/)
2. Descarga un modelo abriendo tu terminal y ejecutando: `ollama run llama3`
3. Verifica que en `src/main/resources/application.yml` tienes:
```yaml
langchain4j:
  ollama:
    chat-model:
      base-url: http://localhost:11434
      model-name: llama3
      temperature: 0.1
```

### 2. Cambiar a OpenAI / Claude
Si quieres que el cerebro sea GPT-4, simplemente agrega tu API Key en el `application.yml` bajo la configuración de `langchain4j.open-ai` y asegúrate de tener la dependencia `langchain4j-open-ai-spring-boot-starter` descomentada en tu `pom.xml`.

### 3. Memoria Cognitiva (RAG)
Los agentes tienen memoria inyectada. En la clase `RagConfig.java` inicializamos un `EmbeddingStore` en memoria utilizando el modelo ligero `all-minilm-l6-v2`. Esto permite que a medida que interactúas con los agentes, estos guarden los contextos técnicos de tu proyecto para recordarlos en futuras peticiones.

---

## 🚀 Uso en Conjunto (Team Mode / Pipeline)

El uso más poderoso del framework es la **Agencia Completa**. Los agentes se comunican entre sí de forma asíncrona y paralela usando **Eventos de Spring (`TaskEvent`)** y sincronizándose a través de una base de datos **H2 (JPA)** para evitar colisionar en los mismos archivos.

1. **Inicia la Aplicación:**
   ```bash
   mvn spring-boot:run
   ```
2. **Entra al modo interactivo:**
   Al iniciar, verás un prompt en tu consola: `antigravity>`.
3. **Pide un proyecto completo:**
   ```text
   antigravity> \generate Crea una API REST en Spring Boot para gestionar reservas de hotel, con un Frontend moderno en React.
   ```
4. **Disfruta del show:**
   * El `ProductManagerAgent` diseñará la arquitectura y los pasos.
   * El `BackendDeveloperAgent` y el `UXUIAgent` trabajarán **en paralelo** escribiendo código real en tu disco duro.
   * Luego, se activarán los agentes asíncronos de aseguramiento de calidad (`BackendQAAgent` y `FrontendQAAgent`).

### ♻️ El Bucle de Auto-Sanación (Reflection Loop)
Si el agente QA escribe los tests y ejecuta `mvn test` (usando su capacidad de abrir un CMD silencioso) y estos **fallan**, el QA cambiará el estado de la tarea y se la mandará de regreso al Developer con el error del compilador. ¡El sistema itera automáticamente hasta que el código pase las pruebas!

---

## 🕵️ Uso Individual (Llamar Agentes por Separado)

Dado que la arquitectura de la Versión 3 está totalmente **desacoplada usando eventos**, no necesitas pasar por el orquestador principal si solo quieres ayuda de un solo agente. 

Por ejemplo, si tú escribiste tu propia API de Spring Boot, pero te da pereza escribir los Tests con Mockito y JUnit, puedes llamar **exclusivamente** al `BackendQAAgent` enviando un evento a tu contexto de Spring:

```java
import com.antigravity.orchestrator.events.TaskEvent;

@Autowired
private TaskRepository taskRepository;
@Autowired
private ApplicationEventPublisher eventPublisher;

public void pedirAyudaAQA() {
    // 1. Creas la tarea dirigida solo al Rol que te interesa
    Task soloTask = Task.builder()
            .assignedTo(Role.BACKEND_QA)
            .status("PENDING")
            .description("Escribe pruebas unitarias con JUnit 5 para el archivo UserController.java adjunto.")
            .expectedOutput("Código de UserController.java...") 
            .build();
            
    // 2. Guardas la tarea en H2 para evitar colisiones
    soloTask = taskRepository.save(soloTask);
    
    // 3. ¡Lanzas el evento! Solo el BackendQAAgent tiene el @EventListener para este rol.
    eventPublisher.publishEvent(new TaskEvent(this, soloTask.getId()));
}
```

Al hacer esto, el Agente QA leerá tu código, escribirá los métodos de prueba en el disco usando su `FileWritingTool` y ejecutará los comandos de verificación con su `CommandLineExecutionTool`.

---

## 🛠️ System Tools (Las Herramientas del Agente)

Los agentes no están atrapados en tu chat, tienen interacción real con tu PC gracias a las herramientas en la carpeta `tools`:
* **`FileWritingTool`**: Capacidad para crear y reescribir código en el disco duro.
* **`FileReadingTool` & `DirectoryListingTool`**: Actúan como los "ojos" del agente, permitiéndole enlistar un proyecto de Java o React y leer los archivos existentes antes de codificar.
* **`CommandLineExecutionTool`**: Las "manos" del agente. Abren procesos subyacentes e invocan la línea de comandos de Windows (CMD) para probar si su código realmente compila o funciona antes de decírtelo.
