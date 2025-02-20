package message.unit.model;

import static org.assertj.core.groups.Tuple.tuple;

import message.model.Message;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JSON serialization and deserialization of {@link Message} objects.
 * These tests ensure that the {@link Message} class is correctly serialized to and deserialized from JSON.
 */
@Tag("unit")
@JsonTest
class MessageJsonSerializationTests {

    /**
     * JacksonTester for testing the serialization and deserialization of a single {@link Message} object.
     */

    @Autowired
    private JacksonTester<Message> tester;

    /**
     * JacksonTester for testing the serialization and deserialization of a list of {@link Message} objects.
     */

    @Autowired
    private JacksonTester<Message[]> testerList;

    /**
     * Test for the serialization of a single {@link Message} object into JSON.
     * It checks that the serialized JSON matches the expected structure and values.
     *
     * @throws IOException if there is an error during serialization
     */
    @Test
    void messageSerializationTest() throws IOException {

        Message message = new Message(1L, "testData1", "jack");

        JsonContent<Message> result = tester.write(message);

        assertThat(result).isEqualToJson("expected.json");

        assertThat(result)
                .hasJsonPathNumberValue("@.id")
                .extractingJsonPathNumberValue("@.id")
                .isEqualTo(1);

        assertThat(result)
                .hasJsonPathStringValue("@.title")
                .extractingJsonPathStringValue("@.title")
                .isEqualTo("testData1");

        assertThat(result)
                .hasJsonPathStringValue("@.owner")
                .extractingJsonPathStringValue("@.owner")
                .isEqualTo("jack");
    }

    /**
     * Test for the deserialization of JSON into a single {@link Message} object.
     * It checks that the JSON content is correctly converted back into a {@link Message} object.
     *
     * @throws IOException if there is an error during deserialization
     */
    @Test
    void messageDeserializationTest() throws IOException {

        String expected = """
            {
                "id": 1,
                "title": "testData1",
                "owner": "jack"
            }
            """;

        Message result = tester.parseObject(expected);

        assertThat(result).isEqualTo(new Message(1L, "testData1", "jack"));
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("testData1");
        assertThat(result.getOwner()).isEqualTo("jack");
    }

    /**
     * Test for the serialization of a list of {@link Message} objects into JSON.
     * It checks that the serialized JSON matches the expected structure for a list.
     *
     * @throws IOException if there is an error during serialization
     */
    @Test
    void listSerializationTest() throws IOException {

        Message[] messages = {
                new Message(1L, "testData1", "jack"),
                new Message(2L, "testData2", "jack")
        };

        var jsonResult = testerList.write(messages);

        assertThat(jsonResult).isEqualToJson("list.json");

        assertThat(jsonResult).extractingJsonPathNumberValue("@[0].id").isEqualTo(1);
        assertThat(jsonResult).extractingJsonPathStringValue("@[0].title").isEqualTo("testData1");
        assertThat(jsonResult).extractingJsonPathStringValue("@[0].owner").isEqualTo("jack");

        assertThat(jsonResult).extractingJsonPathNumberValue("@[1].id").isEqualTo(2);
        assertThat(jsonResult).extractingJsonPathStringValue("@[1].title").isEqualTo("testData2");
        assertThat(jsonResult).extractingJsonPathStringValue("@[1].owner").isEqualTo("jack");
    }

    /**
     * Test for the deserialization of a list of {@link Message} objects from JSON.
     * It checks that the JSON content for a list of messages is correctly converted back into {@link Message} objects.
     *
     * @throws IOException if there is an error during deserialization
     */
    @Test
    void listDeserializationTest() throws IOException {

        String expectedJson = """
            [
               { "id": 1, "title": "testData1", "owner": "jack"},
               { "id": 2, "title": "testData2", "owner": "jack"}
            ]
            """;

        Message[] jsonResult = testerList.parseObject(expectedJson);

        assertThat(jsonResult)
                .hasSize(2)
                .extracting(Message::getId, Message::getTitle, Message::getOwner)
                .containsExactly(
                        tuple(1L, "testData1", "jack"),
                        tuple(2L, "testData2", "jack")
                );

        assertThat(jsonResult[0].getId()).isEqualTo(1L);
        assertThat(jsonResult[0].getTitle()).isEqualTo("testData1");
        assertThat(jsonResult[0].getOwner()).isEqualTo("jack");

        assertThat(jsonResult[1].getId()).isEqualTo(2L);
        assertThat(jsonResult[1].getTitle()).isEqualTo("testData2");
        assertThat(jsonResult[1].getOwner()).isEqualTo("jack");
    }
}