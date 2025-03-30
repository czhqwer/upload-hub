CREATE TABLE IF NOT EXISTS storage_config (
                                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                                              type TEXT,
                                              endpoint TEXT,
                                              access_key TEXT,
                                              secret_key TEXT,
                                              bucket TEXT
);

CREATE TABLE IF NOT EXISTS upload_file (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           upload_id TEXT NOT NULL,
                                           file_identifier TEXT NOT NULL,
                                           file_name TEXT NOT NULL,
                                           object_key TEXT NOT NULL,
                                           total_size INTEGER NOT NULL,
                                           chunk_size INTEGER NOT NULL,
                                           chunk_num INTEGER NOT NULL,
                                           is_finish INTEGER NOT NULL,
                                           content_type TEXT,
                                           access_url TEXT,
                                           download_url TEXT,
                                           create_time INTEGER,
                                           storage_type TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_file_identifier ON upload_file (file_identifier);
CREATE UNIQUE INDEX IF NOT EXISTS uq_upload_id ON upload_file (upload_id);

CREATE TABLE IF NOT EXISTS "user_config" (
                                             "id" INTEGER PRIMARY KEY AUTOINCREMENT,
                                             "username" TEXT NOT NULL DEFAULT 'admin',
                                             "password" TEXT DEFAULT NULL,
                                             "is_main_user" INTEGER NOT NULL DEFAULT 1,
                                             "create_time" TEXT DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS "shared_file" (
                                             "id" INTEGER PRIMARY KEY AUTOINCREMENT,
                                             "file_identifier" TEXT NOT NULL,
                                             "create_time" TEXT DEFAULT NULL,
                                             UNIQUE ("file_identifier")
    );