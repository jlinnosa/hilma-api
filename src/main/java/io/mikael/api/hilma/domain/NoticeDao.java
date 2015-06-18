package io.mikael.api.hilma.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(path = "notices")
public interface NoticeDao extends JpaRepository<Notice, String> {

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    <S extends Notice> S save(S entity);

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    void delete(String id);

}
