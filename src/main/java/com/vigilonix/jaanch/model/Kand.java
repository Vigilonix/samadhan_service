package com.vigilonix.jaanch.model;

import com.vigilonix.jaanch.enums.KandTag;
import com.vigilonix.jaanch.pojo.Person;
import com.vigilonix.jaanch.pojo.Section;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.UUID;

@Entity
@Table
@Getter
@AllArgsConstructor
@Builder
@Setter
@NoArgsConstructor
@ToString
public class Kand {
    @Id
    @Column
    private UUID uuid;

    @Column
    private String firNo;

    @Column
    private Double lat;

    @Column
    private Double lang;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<KandTag> tags;

    @Column
    private UUID targetGeoHierarchyNodeUuid;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Section> sections;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> sectionPayload;


    @Column
    private Long incidentTime;

    @Column
    private Boolean isBns;

    @Column
    private Long createdAt;
    @Column
    private Long modifiedAt;

    @ManyToOne
    private User updatedBy;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Person> victims;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Person> informants;

    @Column
    //in case zeroFir source and targetGeoHierarchyUuid
    private String sourceGeoHierarchyNodeUuid;


    @Column
    private String firFilePath;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> mediaPaths;

//    private String chargeSheetPath;

    /*
    omitted preliminaryReport, informant/victim sign Fir


     */
}
