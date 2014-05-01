package io.mikael.api.hilma.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Builder;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * A full HILMA notice.
 */
@Data @NoArgsConstructor @AllArgsConstructor
@Builder(builderClassName = "Builder")
@Entity @Table(name = "scraped_notices")
@ToString(exclude="html")
public class Notice {

    /** The HILMA-specific notice ID of the form "2014-011132". */
    @Id
    private String id;

    /** Originally fetched URL. */
    private String link;

    /** Original scraped HTML. */
    @Column(columnDefinition="VARCHAR")
    @JsonIgnore
    private String html;

    /** FI: Kansallinen hankintailmoitus, Hankintailmoitus, ... */
    private String type;

    /** FI: VI.5 Tämän ilmoituksen lähettämispäivä */
    @Type(type="org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime published;

    /** FI: IV.3.4 Tarjousten vastaanottamisen määräaika */
    @Type(type="org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime closes;

    /** FI: II.1.6 Yhteinen hankintanimikkeistö (CPV): Pääkohde */
    @JsonProperty("cpv")
    private String mainCpvCode;

    /** FI: II.1.1 Hankintaviranomaisen sopimukselle antama nimi */
    @Column(columnDefinition="VARCHAR")
    @JsonProperty("name")
    private String noticeName;

    /** FI: II.1.5 Sopimuksen tai hankinnan (hankintojen) lyhyt kuvaus */
    @Column(columnDefinition="VARCHAR")
    @JsonProperty("description")
    private String noticeDescription;

    /** FI: Virallinen nimi */
    @Column(columnDefinition="VARCHAR")
    @JsonProperty("organization")
    private String organizationName;

    @Column(columnDefinition="VARCHAR")
    private String note;

}
