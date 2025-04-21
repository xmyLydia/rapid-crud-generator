package com.rapidcrud.generator.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditDeadLetterRepository extends MongoRepository<AuditDeadLetterDocument, String> {
}