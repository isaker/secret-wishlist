package secretwishlist.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
public class Wishlist {

    private String name;
    private String id;
    private Instant created;
    private List<Item> items;

    public Wishlist(String name) {
        this.name = name;
        this.id = RandomStringUtils.randomAlphanumeric(8);
        this.created = Instant.now();
        this.items = new ArrayList<>();
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

    @DynamoDBHashKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBTypeConverted( converter = InstantConverter.class )
    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    static public class InstantConverter implements DynamoDBTypeConverter<String, Instant> {
        @Override
        public String convert(Instant instant) {
            return instant.toString();
        }

        @Override
        public Instant unconvert(String s) {
            return Instant.parse(s);
        }
    }
}
