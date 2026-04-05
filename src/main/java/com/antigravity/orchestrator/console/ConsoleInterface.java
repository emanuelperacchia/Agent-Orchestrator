package com.antigravity.orchestrator.console;

import com.antigravity.orchestrator.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class ConsoleInterface implements CommandLineRunner {

    private final OrchestratorService orchestratorService;

    @Override
    @SuppressWarnings("resource")
    public void run(String... args) throws Exception {
        System.out.println("======================================================");
        System.out.println("  ANTIGRAVITY AGENT ORCHESTRATOR - INTERACTIVE SHELL  ");
        System.out.println("======================================================");
        System.out.println("Type '\\help' for available commands.");
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("\nantigravity> ");
            if (!scanner.hasNextLine()) break; // Exit loop if no more input
            
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (input.startsWith("\\")) {
                handleCommand(input);
            } else {
                System.out.println("No se reconoce el comando. Usa '\\generate <prompt>' para crear un proyecto, o '\\help'.");
            }
        }
    }
    
    private void handleCommand(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "\\exit":
            case "\\quit":
                System.out.println("Saliendo de Antigravity. ¡Hasta pronto!");
                System.exit(0);
                break;
            case "\\help":
                System.out.println("Comandos Disponibles:");
                System.out.println("  \\generate <idea>  - Inicia todo el equipo de agentes para crear el proyecto.");
                System.out.println("  \\status           - Muestra el estado del equipo.");
                System.out.println("  \\exit             - Cierra la aplicación de consola.");
                break;
            case "\\generate":
                if (parts.length < 2 || parts[1].trim().isEmpty()) {
                    System.out.println("Error: Por favor escribe tu idea. Ejemplo: \\generate Crea una API de carrito de compras.");
                } else {
                    String prompt = parts[1].trim();
                    System.out.println("=> Activando equipo de agentes en segundo plano para el proyecto: " + prompt);
                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            orchestratorService.runProjectGeneration(prompt);
                            System.out.print("\n[!] Proyecto finalizado. Presiona Enter o escribe un comando...\nantigravity> ");
                        } catch (Exception e) {
                            System.err.print("\n[X] Error inesperado en los agentes: " + e.getMessage() + "\nantigravity> ");
                        }
                    });
                }
                break;
            case "\\status":
                System.out.println("Todos los agentes están en reposo esperando proyectos.");
                break;
            default:
                System.out.println("Comando desconocido: " + command + ". Escribe '\\help' para ver la lista.");
        }
    }
}
