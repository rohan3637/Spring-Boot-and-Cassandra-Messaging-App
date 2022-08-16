package io.javabrains.inbox.message;

import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends CassandraRepository<Message, UUID> {
    //
}
