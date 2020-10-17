package secretwishlist.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;
import java.util.List;

@DynamoDbBean
public class Wishlist {

    private String name;
    private String id;
    private Instant created;
    private List<Item> items;

    public Wishlist() {
    }

    public Wishlist(Wishlist wishlist) {
        this.name = wishlist.getName();
        this.id = wishlist.getId();
        this.created = wishlist.getCreated();
        this.items = wishlist.getItems();
    }

    public void addItem(Item newItem) {
        items.add(newItem);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    @DynamoDbConvertedBy(ItemsAttributeConverter.class)
    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public boolean removeItem(String itemId) {
        Item itemToRemove = new Item();
        itemToRemove.setId(itemId);
        return items.remove(itemToRemove);

    }

}
