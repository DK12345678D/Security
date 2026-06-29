package com.app.demo.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.app.demo.model.AuditLog;
import com.app.demo.repo.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String userId, String userType, String aliasCode, String moduleName, String activity, String device, String contentName) {
        AuditLog auditEntry = new AuditLog();
        auditEntry.setUserId(userId);
        auditEntry.setUserType(userType);
        auditEntry.setAliasCode(aliasCode);
        auditEntry.setModuleName(moduleName);
        auditEntry.setActivity(activity);
        auditEntry.setLogDate(LocalDate.now());
        auditEntry.setLogTime(LocalTime.now());
        auditEntry.setDevice(device);
        auditEntry.setContentName(contentName);
        auditLogRepository.save(auditEntry);
    }
}
