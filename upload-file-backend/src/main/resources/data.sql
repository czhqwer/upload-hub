INSERT INTO storage_config (id, type, endpoint, access_key, secret_key, bucket)
SELECT 1, 'local', 'http://localhost:10086', '', '', 'E:\data'
    WHERE NOT EXISTS (SELECT 1 FROM storage_config WHERE id = 1);

INSERT INTO storage_config (id, type, endpoint, access_key, secret_key, bucket)
SELECT 2, 'minio', 'http://localhost:9000', 'minio', 'minio123', 'upload-file'
    WHERE NOT EXISTS (SELECT 1 FROM storage_config WHERE id = 2);

INSERT INTO storage_config (id, type, endpoint, access_key, secret_key, bucket)
SELECT 3, 'oss', 'https://oss-cn-guangzhou.aliyuncs.com', 'ossAccessKeyID', 'ossAccessKeySecret', 'yourBucket'
    WHERE NOT EXISTS (SELECT 1 FROM storage_config WHERE id = 3);