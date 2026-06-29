package com.app.demo.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "alias_code")
    private String aliasCode;

    @Column(name = "activity")
    private String activity;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "log_date")
    private LocalDate logDate;

    @Column(name = "log_time")
    private LocalTime logTime;

    @Column(name = "device")
    private String device;

    @Column(name = "content_name")
    private String contentName;

    @Column(name = "user_id")
    private String userId;
}
