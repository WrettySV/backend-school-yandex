package ru.yandextask.anotherdisk.dto.response;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@Entity
@ToString
@Table(name = "items", schema = "storage")
public class SystemItemResponse {
    @Id
    @Column(name ="id", nullable = false)
    private String id;

    @Column(name = "url")
    private String url;

    @Column(name = "parent_id", insertable = false, updatable = false)
    private String parentId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "size")
    private Integer size;

    @Column(name = "date", nullable = false)
    private String date;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    @JsonBackReference
    private SystemItemResponse parent;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "parent")
    private List<SystemItemResponse> children = new ArrayList<>();

    public SystemItemResponse() {
    }
}
