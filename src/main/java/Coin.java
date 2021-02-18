public class Coin {
    private String code;
    private String description;
    private String country;
    private String startValidity;
    private String endValidity;

    public Coin() {
    }

    public Coin(String code, String description, String country, String startValidity, String endValidity) {
        this.code = code;
        this.description = description;
        this.country = country;
        this.startValidity = startValidity;
        this.endValidity = endValidity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStartValidity() {
        return startValidity;
    }

    public void setStartValidity(String startValidity) {
        this.startValidity = startValidity;
    }

    public String getEndValidity() {
        return endValidity;
    }

    public void setEndValidity(String endValidity) {
        this.endValidity = endValidity;
    }

    @Override
    public String toString() {
        return "Coin{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", country='" + country + '\'' +
                ", startValidity='" + startValidity + '\'' +
                ", endValidity='" + endValidity + '\'' +
                '}';
    }
}
