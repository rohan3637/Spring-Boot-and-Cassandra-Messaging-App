package io.javabrains.inbox.message;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "messages_by_id")
public class Message {
    
    @Id
    @PrimaryKeyColumn(name = "time_uuid", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID id;

    @CassandraType(type = Name.TEXT)
    private String from;

    @CassandraType(type = Name.LIST, typeArguments = Name.TEXT)
    private List<String> to;

    @CassandraType(type = Name.TEXT)
    private String subject;

    @CassandraType(type = Name.TEXT)
    private String body;
}
