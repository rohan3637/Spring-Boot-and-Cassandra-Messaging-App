package io.javabrains.inbox.folders;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(value = "unread_email_stats")
public class UnreadMessageStats {
    
    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String id;

    @PrimaryKeyColumn(name = "label", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String label;

    @CassandraType(type = Name.COUNTER)
    private int unreadCount;
}
