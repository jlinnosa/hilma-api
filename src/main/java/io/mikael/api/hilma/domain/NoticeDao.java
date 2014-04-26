package io.mikael.api.hilma.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "notices")
public interface NoticeDao extends JpaRepository<ScrapedNotice, Long> {

}
