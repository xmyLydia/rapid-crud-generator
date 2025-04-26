package com.rapidcrud.generator.mongo;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * pagination query
     */
    public Page<AuditLogDocument> findAllByPage(int page, int size) {
        return auditLogRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * condition query (such as action / entity / keyword query)
     */
    public List<AuditLogDocument> search(String action, String entity, String keyword) {
        Query query = new Query();
        if (!StringUtils.isEmpty(action)) {
            query.addCriteria(Criteria.where("action").is(action));
        }
        if (!StringUtils.isEmpty(entity)) {
            query.addCriteria(Criteria.where("entity").is(entity));
        }
        if (!StringUtils.isEmpty(keyword)) {
            query.addCriteria(Criteria.where("payload").regex(keyword, "i")); // ignore capitalization
        }
        return mongoTemplate.find(query, AuditLogDocument.class);
    }

    /**
     * batch save log documents into mongo db
     */
    public List<AuditLogDocument> saveAll(List<AuditLogDocument> documents) {
        return auditLogRepository.saveAll(documents);
    }
}

