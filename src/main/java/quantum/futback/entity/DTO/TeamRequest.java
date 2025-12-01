package quantum.futback.entity.DTO;

public class TeamRequest {

    private String name;
    private String category;
    private Boolean isOwnTeam;
    private Boolean isActive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getIsOwnTeam() {
        return isOwnTeam;
    }

    public void setIsOwnTeam(Boolean isOwnTeam) {
        this.isOwnTeam = isOwnTeam;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}