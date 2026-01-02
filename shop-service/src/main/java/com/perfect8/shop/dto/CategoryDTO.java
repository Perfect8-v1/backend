package com.perfect8.shop.dto;

import jakarta.validation.constraints.*;

public class CategoryDTO {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description too long")
    private String description;

    @Size(max = 100, message = "Slug too long")
    @Pattern(regexp = "^[a-z0-9\\-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    private String slug;

    private Long parentId;

    @Size(max = 500, message = "Image URL too long")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$",
            message = "Invalid image URL format")
    private String imageUrl;

    @Size(max = 500, message = "Icon URL too long")
    private String iconUrl;

    private Boolean active = true;

    private Boolean featured = false;

    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder;

    // SEO fields
    @Size(max = 255, message = "Meta title too long")
    private String metaTitle;

    @Size(max = 500, message = "Meta description too long")
    private String metaDescription;

    @Size(max = 200, message = "Meta keywords too long")
    private String metaKeywords;

    // Display preferences
    @Size(max = 50, message = "Color code too long")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color code format")
    private String colorCode;

    @Size(max = 100, message = "CSS class name too long")
    private String cssClass;

    // Navigation settings
    private Boolean showInMenu = true;
    private Boolean showInFooter = false;
    private Boolean showOnHomePage = false;

    // E-commerce specific
    @Size(max = 200, message = "Banner text too long")
    private String bannerText;

    @Size(max = 500, message = "Banner image URL too long")
    private String bannerImageUrl;

    @Size(max = 100, message = "Template name too long")
    private String template; // For different category page layouts

    // Default constructor
    public CategoryDTO() {}

    // Constructor with required fields
    public CategoryDTO(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaKeywords() {
        return metaKeywords;
    }

    public void setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public Boolean getShowInMenu() {
        return showInMenu;
    }

    public void setShowInMenu(Boolean showInMenu) {
        this.showInMenu = showInMenu;
    }

    public Boolean getShowInFooter() {
        return showInFooter;
    }

    public void setShowInFooter(Boolean showInFooter) {
        this.showInFooter = showInFooter;
    }

    public Boolean getShowOnHomePage() {
        return showOnHomePage;
    }

    public void setShowOnHomePage(Boolean showOnHomePage) {
        this.showOnHomePage = showOnHomePage;
    }

    public String getBannerText() {
        return bannerText;
    }

    public void setBannerText(String bannerText) {
        this.bannerText = bannerText;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    // Utility methods
    public boolean isParentCategory() {
        return parentId == null;
    }

    public boolean isSubcategory() {
        return parentId != null;
    }

    public boolean hasVisualElements() {
        return (imageUrl != null && !imageUrl.trim().isEmpty()) ||
                (iconUrl != null && !iconUrl.trim().isEmpty()) ||
                (bannerImageUrl != null && !bannerImageUrl.trim().isEmpty());
    }

    public boolean isVisibleInNavigation() {
        return Boolean.TRUE.equals(showInMenu) ||
                Boolean.TRUE.equals(showInFooter) ||
                Boolean.TRUE.equals(showOnHomePage);
    }

    public String getDisplayTitle() {
        return metaTitle != null && !metaTitle.trim().isEmpty() ? metaTitle : name;
    }

    public String getDisplayDescription() {
        return metaDescription != null && !metaDescription.trim().isEmpty() ? metaDescription : description;
    }

    public String generateSlugFromName() {
        if (name == null) {
            return null;
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    public void autoGenerateSlug() {
        if (slug == null || slug.trim().isEmpty()) {
            this.slug = generateSlugFromName();
        }
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", parentId=" + parentId +
                ", active=" + active +
                ", featured=" + featured +
                ", displayOrder=" + displayOrder +
                '}';
    }
}