package secretwishlist.model;

import com.google.gson.Gson;
import org.junit.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class ItemsAttributeConverterTest {

    private Gson gson = new Gson();
    private ItemsAttributeConverter converter = new ItemsAttributeConverter();

    @Test
    public void convertAttributeValueToArrayList() {
        Item item1 = new Item();
        Item item2 = new Item();

        item1.setId("id1");
        item2.setId("id2");

        ArrayList<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        String itemsString = gson.toJson(items);
        AttributeValue av = AttributeValue.builder().s(itemsString).build();

        ArrayList<Item> resultList = converter.transformTo(av);
        System.out.println(resultList);

        assertEquals(items.size(), resultList.size());
        assertEquals(av.s(), gson.toJson(resultList));
    }

    @Test
    public void convertArrayListToAttributeValue() {
        Item item1 = new Item();
        Item item2 = new Item();

        item1.setId("id1");
        item2.setId("id2");

        ArrayList<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        AttributeValue av = converter.transformFrom(items);
        System.out.println(av);

        assertEquals(gson.toJson(items), av.s());

    }

}
