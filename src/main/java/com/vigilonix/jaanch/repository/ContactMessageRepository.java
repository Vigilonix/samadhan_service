package com.vigilonix.jaanch.repository;
import com.vigilonix.jaanch.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, String> {

    // Method to find messages by contactId
    List<ContactMessage> findByContactId(String contactId);

    // Method to find messages by channel type
    List<ContactMessage> findByChannelType(String channelType);

    // Method to find messages by message body content (e.g., messages containing "Hello")
    List<ContactMessage> findByMessageBodyContaining(String text);
}
