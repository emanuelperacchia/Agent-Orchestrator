# 🪐 Antigravity Agent Orchestrator (V3)

Antigravity es un framework de Inteligencia Artificial basado en Java (Spring Boot) y **LangChain4j**. Transforma tu entorno local en una agencia de desarrollo de software completamente autónoma.

Este orquestador no genera simples "respuestas de chat"; coordina un equipo de agentes que **crean archivos reales**, **leen tu código existente**, e **incluso ejecutan comandos en la consola** para testear el código que acaban de escribir.

---

## 🧠 El "Cerebro": Configuración del Modelo de IA

El core del sistema usa **LangChain4j**, lo que significa que el "cerebro" es agnóstico y puedes usar cualquier modelo.

### 1. Configuración Local (Ollama) - Por Defecto

El proyecto viene configurado para usar IA local y gratuita.

1. Instala Ollama (<https://ollama.com/>)
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

---

## 🛡️ Seguridad y Auditoría (Producción Ready)

La Versión 3 introduce un sistema de endurecimiento de seguridad diseñado para entornos de producción, permitiendo el control total sobre las capacidades de los agentes.

### 1. Configuración Persistente

Los ajustes de seguridad ya no son volátiles; se guardan en la base de datos **H2** y persisten tras reiniciar la aplicación. Puedes consultar y modificar estos estados mediante el flujo de eventos de los agentes.

### 2. Control de Configuración (`update-config`)

El sistema permite a los administradores (o a agentes supervisores) enviar comandos de actualización de seguridad:

```json
{
  "securityChecks": true,
  "auditLogs": true,
  "twoFactorAuth": false,
  "dataEncryption": true
}
```

### 3. Ejecución de Comandos Segura (Sandboxing & Blacklisting)

La herramienta `CommandLineExecutionTool` ha sido reforzada:

* **Blacklist**: Cuando `securityChecks` está activo, se bloquean automáticamente comandos peligrosos (ej. `rm -rf`, `chmod`, `chown`, `mkfs`).
* **Aislamiento**: Los comandos se ejecutan dentro de contenedores Docker efímeros (**Ubuntu 22.04**) con límites de memoria y acceso restringido al workspace.

### 4. Registros de Auditoría (`AuditLog`)

Si activas `auditLogs`, cada acción sensible queda registrada en la tabla `audit_logs`:

* **Cambios de Configuración**: Quién y qué se cambió.
* **Ejecución de Herramientas**: El comando exacto ejecutado por el agente y el resultado (SUCCESS/DENIED).
* **Alertas de Seguridad**: Intentos de Path Traversal o ejecución de comandos bloqueados.

---

## 🛠️ System Tools (Las Herramientas del Agente)

Los agentes interactúan con tu sistema mediante herramientas especializadas:

* **`FileWritingTool`**: Crea y modifica código en el disco duro.
* **`FileReadingTool` & `DirectoryListingTool`**: Permiten al agente "ver" la estructura de tu proyecto.
* **`CommandLineExecutionTool`**: Ejecuta procesos en un entorno controlado (Docker). Ahora incluye **auditoría** y **bloqueo de seguridad** dinámico.
* **`SecurityConfigTool`**: Permite la gestión dinámica de los flags de seguridad del orquestador.
* **`WorkspaceValidator`**: Garantiza que ningún agente pueda acceder a archivos fuera de la carpeta `./workspace` designada, evitando ataques de Path Traversal.
