package secretwishlist.dao;

import secretwishlist.model.Secret;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class SecretsDao {

    private final String secretsTableName = System.getenv("secretsTable");
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    private final DynamoDbTable<Secret> secretsTable = dynamoDbEnhancedClient.table(secretsTableName, TableSchema.fromBean(Secret.class));

    public void updateSecret(Secret secret) {
        secretsTable.putItem(secret);
    }


}
