package quantum.futback.entity.DTO;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

public class PlayerRequest {

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String dni;
    private String dominantFoot;
    private Integer jerseyNumber;
    private UUID teamId;
    private UUID positionPrimaryId;
    private UUID positionSecondaryId;
    private String parentDni;
    private String parentFullName;
    private MultipartFile photoFile;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getDominantFoot() { return dominantFoot; }
    public void setDominantFoot(String dominantFoot) { this.dominantFoot = dominantFoot; }

    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }

    public UUID getTeamId() { return teamId; }
    public void setTeamId(UUID teamId) { this.teamId = teamId; }

    public UUID getPositionPrimaryId() { return positionPrimaryId; }
    public void setPositionPrimaryId(UUID positionPrimaryId) { this.positionPrimaryId = positionPrimaryId; }

    public UUID getPositionSecondaryId() { return positionSecondaryId; }
    public void setPositionSecondaryId(UUID positionSecondaryId) { this.positionSecondaryId = positionSecondaryId; }

    public String getParentDni() { return parentDni; }
    public void setParentDni(String parentDni) { this.parentDni = parentDni; }

    public String getParentFullName() { return parentFullName; }
    public void setParentFullName(String parentFullName) { this.parentFullName = parentFullName; }

    public MultipartFile getPhotoFile() { return photoFile; }
    public void setPhotoFile(MultipartFile photoFile) { this.photoFile = photoFile; }
}
