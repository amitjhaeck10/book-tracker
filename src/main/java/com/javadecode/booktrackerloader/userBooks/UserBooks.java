package com.javadecode.booktrackerloader.userBooks;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.LocalDate;

@Table(value = "book_by_userid")
@Getter
@Setter
public class UserBooks {

    @PrimaryKey
    private UserBooksPrimaryKey key;

    @Column("reading_status")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String readingStatus;

    @Column("start_date")
    @CassandraType(type = CassandraType.Name.DATE)
    private LocalDate startDate;

    @Column("published_date")
    @CassandraType(type = CassandraType.Name.DATE)
    private LocalDate completedDate;

    @Column("rating")
    @CassandraType(type = CassandraType.Name.TEXT)
    private int rating;

    @Column("feedback")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String feedback;

}
