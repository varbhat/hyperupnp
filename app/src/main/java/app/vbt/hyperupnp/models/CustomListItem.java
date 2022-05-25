package app.vbt.hyperupnp.models;

public class CustomListItem {

    private final String title;
    private int icon;
    private String description;
    private String description2;
    private String iconUrl;

    protected CustomListItem(int icon) {
        this(icon, null, null, null, null);
    }

    public CustomListItem(int icon, String title, String description) {
        this(icon, null, title, description, null);
    }

    public CustomListItem(int icon, String iconUrl, String title,
                          String description, String description2) {
        this.icon = icon;
        this.iconUrl = iconUrl;
        this.title = title;
        this.description = description;
        this.description2 = description2;
    }

    @Override
    public String toString() {
        return this.title;
    }

    public String getId() {
        return "";
    }

    public int getIcon() {
        return this.icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription2() {
        return this.description2;
    }

    public void setDescription2(String description) {
        this.description2 = description;
    }
}
