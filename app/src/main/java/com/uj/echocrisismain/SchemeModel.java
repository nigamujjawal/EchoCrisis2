package com.uj.echocrisismain;

public class SchemeModel {
    private String title;
    private String description;
    private String eligibility;
    private String link;

    public SchemeModel(String title, String description, String eligibility, String link) {
        this.title = title;
        this.description = description;
        this.eligibility = eligibility;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEligibility() {
        return eligibility;
    }

    public String getLink() {
        return link;
    }
}
