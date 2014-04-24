package io.mikael.api.hilma.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.time.LocalDateTime;

/**
 * A full HILMA notice.
 */
@Data @NoArgsConstructor @AllArgsConstructor
@Builder(builderClassName = "Builder")
public class ScrapedNotice {

    /** Originally fetched URL. */
    public String link;

    /** Original scraped HTML. */
    public String html;

    /** The HILMA-specific notice ID of the form "2014-011132". */
    public String id;

    /** FI: Kansallinen hankintailmoitus, Hankintailmoitus, ... */
    public String type;

    /** FI: VI.5 Tämän ilmoituksen lähettämispäivä */
    public LocalDateTime published;

    /** FI: IV.3.4 Tarjousten vastaanottamisen määräaika */
    public LocalDateTime closes;

    /** FI: II.1.6 Yhteinen hankintanimikkeistö (CPV): Pääkohde */
    public String mainCpvCode;

    /** FI: II.1.1 Hankintaviranomaisen sopimukselle antama nimi */
    public String noticeName;

    /** FI: II.1.5 Sopimuksen tai hankinnan (hankintojen) lyhyt kuvaus */
    public String noticeDescription;

    /** FI: Virallinen nimi */
    public String organizationName;

}
