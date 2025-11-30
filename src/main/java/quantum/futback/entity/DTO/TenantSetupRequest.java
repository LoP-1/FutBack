package quantum.futback.entity.DTO;

public record TenantSetupRequest(
        String academyName,
        String adminFullName,
        String adminEmail,
        String adminDni,
        String adminPassword,
        String adminPhone
) {}