package ru.yandextask.anotherdisk.entity;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Entity
@ToString

@Table(name = "items", schema = "storage")
public class ItemEntity {
    @Id
    @Column(name ="id", nullable = false)
    private String id;

    @Column(name = "url")
    private String url;

    @Column(name = "date", nullable = false, columnDefinition = "TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "parent_id") //insertable = false, updatable = false)
    private String parentId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "size")
    private Integer size;

//    @ManyToOne
//    @JoinColumn(name = "parent_id", referencedColumnName = "id")
//    private ItemEntity parent;
//
//    @LazyCollection(LazyCollectionOption.FALSE)
//    @OneToMany (mappedBy = "parent")
//    private List<ItemEntity> children;

    public ItemEntity() {

    }

}
