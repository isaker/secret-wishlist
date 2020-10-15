package secretwishlist.dao;

import secretwishlist.model.Wishlist;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Optional;

public class WishlistDao {

    private final String wishlistsTableName = System.getenv("wishlistsTable");
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    private final DynamoDbTable<Wishlist> wishlistsTable = dynamoDbEnhancedClient.table(wishlistsTableName, TableSchema.fromBean(Wishlist.class));

    public void updateWishlist(Wishlist wishlist) {
        wishlistsTable.putItem(wishlist);
    }

    public Optional<Wishlist> getWishlist(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Optional.ofNullable(wishlistsTable.getItem(key));
    }


}
