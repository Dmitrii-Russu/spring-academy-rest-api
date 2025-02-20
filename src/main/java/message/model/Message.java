package message.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Id;

/**
 * Represents a message entity.
 * This entity is used to store messages with a title and owner.
 * The message has an automatically generated ID.
 */
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Content must not be blank") // Restricts null, "", and " " (spaces)
    private String title;
    private String owner;

    protected Message() {}
    public Message(Long id, String title, String owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
    }

    public Long getId() { return id;}
    public String getTitle() { return title;}
    public void setTitle(String title) { this.title = title;}
    public String getOwner() { return owner;}
    public void setOwner(String owner) { this.owner = owner;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        return id != null && id.equals(((Message) o).getId());
    }

    @Override
    public int hashCode() { return getClass().hashCode();}
}
