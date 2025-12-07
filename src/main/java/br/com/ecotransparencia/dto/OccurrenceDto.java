package br.com.ecotransparencia.dto;

public class OccurrenceDto {

    private String id;
    private String category;
    private String date;
    private String description;
    private String source;
    private String sourceUrl;
    private String status;

    public OccurrenceDto() {
    }

    public OccurrenceDto(String id, String category, String date, String description,
                         String source, String sourceUrl, String status) {
        this.id = id;
        this.category = category;
        this.date = date;
        this.description = description;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
