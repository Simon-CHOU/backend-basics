-- Create file metadata table
CREATE TABLE file_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    encryption_key VARCHAR(64) NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster lookups
CREATE INDEX idx_file_metadata_document_id ON file_metadata(document_id);
CREATE INDEX idx_file_metadata_upload_date ON file_metadata(upload_date);
CREATE INDEX idx_file_metadata_file_hash ON file_metadata(file_hash);

-- Create file chunks table for sharded uploads
CREATE TABLE file_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chunk_id VARCHAR(36) NOT NULL UNIQUE,
    document_id VARCHAR(36) NOT NULL,
    chunk_number INTEGER NOT NULL,
    total_chunks INTEGER NOT NULL,
    chunk_size BIGINT NOT NULL,
    chunk_path VARCHAR(500) NOT NULL,
    chunk_hash VARCHAR(64) NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_assembled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for file chunks
CREATE INDEX idx_file_chunks_document_id ON file_chunks(document_id);
CREATE INDEX idx_file_chunks_chunk_number ON file_chunks(document_id, chunk_number);
CREATE INDEX idx_file_chunks_upload_date ON file_chunks(upload_date);
CREATE INDEX idx_file_chunks_is_assembled ON file_chunks(is_assembled);

-- Create upload sessions table to track multi-part uploads
CREATE TABLE upload_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    total_size BIGINT NOT NULL,
    total_chunks INTEGER NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    chunks_uploaded INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED, FAILED, EXPIRED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- Create indexes for upload sessions
CREATE INDEX idx_upload_sessions_session_id ON upload_sessions(session_id);
CREATE INDEX idx_upload_sessions_status ON upload_sessions(status);
CREATE INDEX idx_upload_sessions_expires_at ON upload_sessions(expires_at);

-- Create audit log table for tracking file operations
CREATE TABLE file_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    operation VARCHAR(20) NOT NULL, -- UPLOAD, DOWNLOAD, DELETE
    user_ip VARCHAR(45),
    user_agent VARCHAR(500),
    operation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);

-- Create index for audit log
CREATE INDEX idx_file_audit_log_document_id ON file_audit_log(document_id);
CREATE INDEX idx_file_audit_log_operation_date ON file_audit_log(operation_date);
CREATE INDEX idx_file_audit_log_operation ON file_audit_log(operation);