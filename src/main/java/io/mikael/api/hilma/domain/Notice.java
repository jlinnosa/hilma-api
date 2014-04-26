package io.mikael.api.hilma.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Builder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
    @Column(columnDefinition="TEXT")
    private String html;

    /** FI: Kansallinen hankintailmoitus, Hankintailmoitus, ... */
    private String type;

    /** FI: VI.5 Tämän ilmoituksen lähettämispäivä */
    private LocalDateTime published;

    /** FI: IV.3.4 Tarjousten vastaanottamisen määräaika */
    private LocalDateTime closes;

    /** FI: II.1.6 Yhteinen hankintanimikkeistö (CPV): Pääkohde */
    private String mainCpvCode;

    /** FI: II.1.1 Hankintaviranomaisen sopimukselle antama nimi */
    @Column(columnDefinition="TEXT")
    private String noticeName;

    /** FI: II.1.5 Sopimuksen tai hankinnan (hankintojen) lyhyt kuvaus */
    @Column(columnDefinition="TEXT")
    private String noticeDescription;

    /** FI: Virallinen nimi */
    @Column(columnDefinition="TEXT")
    private String organizationName;

    @Column(columnDefinition="TEXT")
    private String note;

}
