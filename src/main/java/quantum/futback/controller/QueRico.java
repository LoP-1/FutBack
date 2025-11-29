package quantum.futback.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//super endpoint de prueba para ver que todo funciona
@RestController
public class QueRico {

    @GetMapping("/esta-rico")
    public String estaRico() {
        return "si que rico :D ahora modificado";
    }

}
