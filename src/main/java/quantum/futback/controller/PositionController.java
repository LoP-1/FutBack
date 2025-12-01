package quantum.futback.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import quantum.futback.entity.DTO.PositionRequest;
import quantum.futback.entity.Position;
import quantum.futback.services.PositionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    /**
     * POST /api/positions: Crea una nueva posición.
     */
    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody PositionRequest request) {
        // Mapeo básico:
        Position newPosition = new Position();
        newPosition.setName(request.getName());
        newPosition.setAbbreviation(request.getAbbreviation());
        newPosition.setArea(request.getArea());

        Position createdPosition = positionService.createPosition(newPosition);
        return new ResponseEntity<>(createdPosition, HttpStatus.CREATED);
    }

    /**
     * GET /api/positions: Obtiene todas las posiciones del tenant actual.
     */
    @GetMapping
    public ResponseEntity<List<Position>> getAllPositions() {
        List<Position> positions = positionService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    /**
     * GET /api/positions/{id}: Obtiene una posición por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Position> getPositionById(@PathVariable UUID id) {
        Position position = positionService.getPositionById(id);
        return ResponseEntity.ok(position);
    }

    /**
     * PUT /api/positions/{id}: Actualiza los datos de una posición.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable UUID id, @RequestBody PositionRequest request) {
        // Mapeo básico:
        Position updatedPositionDetails = new Position();
        updatedPositionDetails.setName(request.getName());
        updatedPositionDetails.setAbbreviation(request.getAbbreviation());
        updatedPositionDetails.setArea(request.getArea());

        Position updatedPosition = positionService.updatePosition(id, updatedPositionDetails);
        return ResponseEntity.ok(updatedPosition);
    }

    /**
     * DELETE /api/positions/{id}: Elimina una posición.
     * La validación de borrado se maneja en PositionService y devuelve 409 CONFLICT si falla.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable UUID id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}