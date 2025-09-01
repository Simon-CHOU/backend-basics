# lab020 OSS-like

## requirements
1. Create an upload interface: Implement single file upload. Return documentId after uploading
2. Implement an upload interface to enable sharded uploading of large files. Return documentId after uploading
3. Implement a download interface to download files based on the documentId
4.  File information sharding and encryption are stored in the file system D:\dev\FilePlatform\data. Reasonable tables are created to save file metadata and sharding information, and file system directories are divided reasonably
5. use h2 in-mem database as dbms. use flyway to manage database schema.
7. Application.com distinguishes dev stg prd. This project only focuses on Dev, and the corresponding configuration values for stg prd can be filled with placeholders

## testing
1. unit-testing 100% coverage
2. integration-testing 100% coverage
3. e2e-testing 100% coverage