package secretwishlist.model;

public class Item {

    private String id;
    private String description;
    private String url;
    private boolean bought;
    private String buyer;
    private String buyersComment;

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", bought=" + bought +
                ", buyer='" + buyer + '\'' +
                ", buyersComment='" + buyersComment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Item && this.id.equalsIgnoreCase(((Item) obj).getId());
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getBuyersComment() {
        return buyersComment;
    }

    public void setBuyersComment(String buyersComment) {
        this.buyersComment = buyersComment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
