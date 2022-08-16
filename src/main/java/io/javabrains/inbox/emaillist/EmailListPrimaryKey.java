package io.javabrains.inbox.emaillist;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PrimaryKeyClass
public class EmailListPrimaryKey {
    
    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String userId;

    @PrimaryKeyColumn(name = "label", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String label;

    @PrimaryKeyColumn(name = "created_time_uuid", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    @CassandraType(type = CassandraType.Name.TIMEUUID)
    private UUID timeId;
}
