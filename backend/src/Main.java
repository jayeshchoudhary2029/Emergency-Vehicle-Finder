
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        VehicleService vehicleService = new VehicleService();

        server.createContext("/api/vehicles", exchange -> {
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
            );
            exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
            );

            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            StringBuilder json = new StringBuilder("[");
            var vehicles = vehicleService.getAllVehicles();

            for (int i = 0; i < vehicles.size(); i++) {
                Vehicle v = vehicles.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id\":\"").append(v.getId()).append("\",")
                    .append("\"type\":\"").append(v.getType()).append("\",")
                    .append("\"location\":\"").append(v.getLocation()).append("\",")
                    .append("\"distance\":").append(v.getDistance()).append(",")
                    .append("\"priority\":").append(v.getPriority()).append(",")
                    .append("\"status\":\"").append(v.getStatus()).append("\"")
                    .append("}");
            }

            json.append("]");

            byte[] response = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);

            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });

        server.createContext("/api/nearest", exchange -> {
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
            );
            exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
            );

            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            String query = exchange.getRequestURI().getRawQuery();
            String type = null;

            if (query != null) {
                for (String part : query.split("&")) {
                    String[] pair = part.split("=", 2);

                    if (pair.length == 2 && pair[0].equals("type")) {
                        type = java.net.URLDecoder.decode(
                            pair[1], StandardCharsets.UTF_8
                        );
                        break;
                    }
                }
            }

            if (type == null || type.isBlank()) {
                byte[] error = "{\"error\":\"Please provide an emergency type.\"}"
                    .getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, error.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(error);
                }
                return;
            }

            Vehicle nearest = vehicleService.findNearestVehicle(type);
            String responseText;

            if (nearest == null) {
                responseText = "{\"message\":\"No available vehicle found.\"}";
            } else {
                responseText = "{"
                    + "\"id\":\"" + nearest.getId() + "\","
                    + "\"type\":\"" + nearest.getType() + "\","
                    + "\"location\":\"" + nearest.getLocation() + "\","
                    + "\"distance\":" + nearest.getDistance() + ","
                    + "\"priority\":" + nearest.getPriority() + ","
                    + "\"status\":\"" + nearest.getStatus() + "\""
                    + "}";
            }

            byte[] response = responseText.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);

            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });

        
        server.createContext("/api/search", exchange -> {
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
            );
            exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
            );

            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            String query = exchange.getRequestURI().getRawQuery();
            String targetId = null;

            if (query != null) {
                for (String part : query.split("&")) {
                    String[] pair = part.split("=", 2);

                    if (pair.length == 2 && pair[0].equals("id")) {
                        targetId = java.net.URLDecoder.decode(
                            pair[1], StandardCharsets.UTF_8
                        );
                        break;
                    }
                }
            }

            String responseText;
            int statusCode = 200;

            if (targetId == null || targetId.isBlank()) {
                responseText = "{\"error\":\"Please provide a vehicle ID.\"}";
                statusCode = 400;
            } else {
                Vehicle found = vehicleService.linearSearchById(targetId);

                if (found == null) {
                    responseText = "{\"message\":\"Vehicle not found.\"}";
                } else {
                    responseText = "{"
                        + "\"id\":\"" + found.getId() + "\","
                        + "\"type\":\"" + found.getType() + "\","
                        + "\"location\":\"" + found.getLocation() + "\","
                        + "\"distance\":" + found.getDistance() + ","
                        + "\"priority\":" + found.getPriority() + ","
                        + "\"status\":\"" + found.getStatus() + "\""
                        + "}";
                }
            }

            byte[] response = responseText.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, response.length);

            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });

                
        server.createContext("/api/binary-search", exchange -> {
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
            );
            exchange.getResponseHeaders().set(
                "Content-Type", "application/json"
            );

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String targetId = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);

                    if (pair.length == 2 && pair[0].equals("id")) {
                        targetId = java.net.URLDecoder.decode(
                            pair[1],
                            java.nio.charset.StandardCharsets.UTF_8
                        );
                        break;
                    }
                }
            }

            if (targetId == null || targetId.trim().isEmpty()) {
                String response =
                    "{\"error\":\"Please provide a vehicle ID.\"}";
                byte[] bytes = response.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8
                );
                exchange.sendResponseHeaders(400, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
                return;
            }

            Vehicle vehicle = vehicleService.binarySearchById(targetId);

            String response;

            if (vehicle != null) {
                response = String.format(
                    "{\"id\":\"%s\",\"type\":\"%s\","
                    + "\"location\":\"%s\",\"distance\":%s,"
                    + "\"priority\":%d,\"status\":\"%s\"}",
                    vehicle.getId(),
                    vehicle.getType(),
                    vehicle.getLocation(),
                    vehicle.getDistance(),
                    vehicle.getPriority(),
                    vehicle.getStatus()
                );
            } else {
                response = "{\"message\":\"Vehicle not found.\"}";
            }

            byte[] bytes = response.getBytes(
                java.nio.charset.StandardCharsets.UTF_8
            );

            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

                
        server.createContext("/api/sort", exchange -> {
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
            );
            exchange.getResponseHeaders().set(
                "Content-Type", "application/json"
            );

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<Vehicle> sortedVehicles =
                vehicleService.sortVehiclesByDistance();

            StringBuilder json = new StringBuilder("[");
            
            for (int i = 0; i < sortedVehicles.size(); i++) {
                Vehicle vehicle = sortedVehicles.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append(String.format(
                    "{\"id\":\"%s\",\"type\":\"%s\","
                    + "\"location\":\"%s\",\"distance\":%s,"
                    + "\"priority\":%d,\"status\":\"%s\"}",
                    vehicle.getId(),
                    vehicle.getType(),
                    vehicle.getLocation(),
                    vehicle.getDistance(),
                    vehicle.getPriority(),
                    vehicle.getStatus()
                ));
            }

            json.append("]");

            byte[] bytes = json.toString().getBytes(
                java.nio.charset.StandardCharsets.UTF_8
            );

            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });


        server.start();
        System.out.println("Backend running at http://localhost:8080");
    }
}
