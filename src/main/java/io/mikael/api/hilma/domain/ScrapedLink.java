package io.mikael.api.hilma.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.time.LocalDateTime;

/**
 * A "new item" link from the "new items" page.
 */
@Data @NoArgsConstructor @AllArgsConstructor
@Builder(builderClassName = "Builder")
public class ScrapedLink {

    public String id;

    /** FI: Julkaistu. */
    public LocalDateTime published;

    /** FI: Tarjousten määräaika. */
    public LocalDateTime closes;

    public String link;

    public String name;

    public String type;

}
