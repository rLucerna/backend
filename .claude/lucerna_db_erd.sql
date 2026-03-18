-- 1. 테이블 생성
CREATE TABLE "letters" (
                           "id" BIGINT NOT NULL,
                           "sender_id" BIGINT NOT NULL,
                           "receiver_id" BIGINT NOT NULL,
                           "parent_letter_id" BIGINT NULL,
                           "content" TEXT NOT NULL,
                           "is_anonymous" BOOLEAN NULL,
                           "created_at" TIMESTAMP NULL,
                           "Key" VARCHAR(255) NOT NULL
);

CREATE TABLE "user_social" (
                               "id" BIGINT NOT NULL,
                               "user_id" BIGINT NOT NULL,
                               "provider" VARCHAR(20) NOT NULL,
                               "social_email" VARCHAR(255) NULL,
                               "linked_at" TIMESTAMP NULL
);

CREATE TABLE "volunteer_favorites_info" (
                                            "volunteer_code" VARCHAR(50) NOT NULL,
                                            "title" VARCHAR(200) NOT NULL,
                                            "deadline" TIMESTAMP NOT NULL,
                                            "manager_name" VARCHAR(50) NULL,
                                            "thumbnail_url" VARCHAR(500) NULL,
                                            "last_synced_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL
);

CREATE TABLE "users" (
                         "id" BIGINT NOT NULL,
                         "keycloak_id" VARCHAR(36) NOT NULL,
                         "email" VARCHAR(255) NOT NULL,
                         "last_login_at" TIMESTAMP NULL,
                         "status" VARCHAR(20) DEFAULT 'ACTIVE' NULL,
                         "created_at" TIMESTAMP NULL,
                         "updated_at" TIMESTAMP NULL
);

CREATE TABLE "user_app_conf" (
                                 "id" BIGINT NOT NULL,
                                 "settings" JSONB NOT NULL,
                                 "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE "posts" (
                         "id" BIGINT NOT NULL,
                         "lodge_id" BIGINT NOT NULL,
                         "notebook_id" BIGINT NULL,
                         "title" VARCHAR(200) NOT NULL,
                         "thumbnail_url" VARCHAR(500) NULL,
                         "status" VARCHAR(20) DEFAULT 'DRAFT' NOT NULL CHECK ("status" IN ('DRAFT', 'PUBLISHED', 'DELETED')),
                         "visibility_status" VARCHAR(20) DEFAULT 'ALL' NOT NULL CHECK ("visibility_status" IN ('ALL', 'SUBSCRIBER', 'PRIVATE')),
                         "view_count" INT NOT NULL,
                         "location_info" VARCHAR(255) NULL,
                         "created_at" TIMESTAMP NOT NULL,
                         "updated_at" TIMESTAMP NOT NULL,
                         "deleted_at" TIMESTAMP NULL,
                         "collection_sum" INT NOT NULL
);

CREATE TABLE "user_notebook_collection" (
                                            "notebook_id" BIGINT NOT NULL,
                                            "collecter_id" BIGINT NOT NULL,
                                            "collection_at" TIMESTAMP NOT NULL,
                                            "memo" TEXT NULL
);

CREATE TABLE "post_images" (
                               "id" BIGINT NOT NULL,
                               "post_id" BIGINT NOT NULL,
                               "image_url" VARCHAR(500) NOT NULL,
                               "origin_name" VARCHAR(255) NULL,
                               "file_size" BIGINT NULL,
                               "created_at" TIMESTAMP NULL
);

CREATE TABLE "user_volunteer_favorites" (
                                            "volunteer_code" VARCHAR(50) NOT NULL,
                                            "user_id" BIGINT NOT NULL,
                                            "created_at" TIMESTAMP NOT NULL
);

CREATE TABLE "lodges" (
                          "id" BIGINT NOT NULL,
                          "user_id" BIGINT NOT NULL,
                          "nickname" VARCHAR(50) NOT NULL,
                          "introduction" VARCHAR(100) NULL,
                          "bio" TEXT NULL,
                          "profile_image_url" VARCHAR(500) NULL,
                          "visibility_status" VARCHAR(20) DEFAULT 'ALL' NULL CHECK ("visibility_status" IN ('ALL', 'SUBSCRIBER', 'PRIVATE')),
                          "follower_count" INT NOT NULL,
                          "following_count" INT NOT NULL
);

CREATE TABLE "post_contents" (
                                 "post_id" BIGINT NOT NULL,
                                 "content" TEXT NOT NULL,
                                 "Field" VARCHAR(255) NULL
);

CREATE TABLE "user_post_collection" (
                                        "collecter_id" BIGINT NOT NULL,
                                        "post_id" BIGINT NOT NULL,
                                        "collection_at" TIMESTAMP NOT NULL,
                                        "memo" TEXT NULL
);

CREATE TABLE "notebooks" (
                             "id" BIGINT NOT NULL,
                             "lodge_id" BIGINT NOT NULL,
                             "name" VARCHAR(100) NOT NULL,
                             "color_code" VARCHAR(7) NULL,
                             "display_order" INT NULL,
                             "visibility_status" VARCHAR(20) DEFAULT 'ALL' NOT NULL CHECK ("visibility_status" IN ('ALL', 'SUBSCRIBER', 'PRIVATE')),
                             "created_at" TIMESTAMP NOT NULL,
                             "collection_sum" INT NOT NULL
);

CREATE TABLE "subscriptions" (
                                 "subscriber_id" BIGINT NOT NULL,
                                 "target_lodge_id" BIGINT NOT NULL,
                                 "created_at" TIMESTAMP NOT NULL
);

-- 2. 기본키(Primary Key) 제약조건 추가
ALTER TABLE "letters" ADD CONSTRAINT "PK_LETTERS" PRIMARY KEY ("id");
ALTER TABLE "user_social" ADD CONSTRAINT "PK_USER_SOCIAL" PRIMARY KEY ("id", "user_id");
ALTER TABLE "volunteer_favorites_info" ADD CONSTRAINT "PK_VOLUNTEER_FAVORITES_INFO" PRIMARY KEY ("volunteer_code");
ALTER TABLE "users" ADD CONSTRAINT "PK_USERS" PRIMARY KEY ("id");
ALTER TABLE "user_app_conf" ADD CONSTRAINT "PK_USER_APP_CONF" PRIMARY KEY ("id");
ALTER TABLE "posts" ADD CONSTRAINT "PK_POSTS" PRIMARY KEY ("id");
ALTER TABLE "user_notebook_collection" ADD CONSTRAINT "PK_USER_NOTEBOOK_COLLECTION" PRIMARY KEY ("notebook_id", "collecter_id");
ALTER TABLE "post_images" ADD CONSTRAINT "PK_POST_IMAGES" PRIMARY KEY ("id");
ALTER TABLE "user_volunteer_favorites" ADD CONSTRAINT "PK_USER_VOLUNTEER_FAVORITES" PRIMARY KEY ("volunteer_code", "user_id");
ALTER TABLE "lodges" ADD CONSTRAINT "PK_LODGES" PRIMARY KEY ("id");
ALTER TABLE "post_contents" ADD CONSTRAINT "PK_POST_CONTENTS" PRIMARY KEY ("post_id");
ALTER TABLE "user_post_collection" ADD CONSTRAINT "PK_USER_POST_COLLECTION" PRIMARY KEY ("collecter_id", "post_id");
ALTER TABLE "notebooks" ADD CONSTRAINT "PK_NOTEBOOKS" PRIMARY KEY ("id");
ALTER TABLE "subscriptions" ADD CONSTRAINT "PK_SUBSCRIPTIONS" PRIMARY KEY ("subscriber_id", "target_lodge_id");

-- 3. 유니크(Unique) 제약조건 추가
ALTER TABLE "users" ADD CONSTRAINT "UQ_USERS_KEYCLOAK_ID" UNIQUE ("keycloak_id");

-- 4. 외래키(Foreign Key) 제약조건 추가
ALTER TABLE "letters" ADD CONSTRAINT "FK_users_TO_letters_sender" FOREIGN KEY ("sender_id") REFERENCES "users" ("id");
ALTER TABLE "letters" ADD CONSTRAINT "FK_users_TO_letters_receiver" FOREIGN KEY ("receiver_id") REFERENCES "users" ("id");
ALTER TABLE "letters" ADD CONSTRAINT "FK_letters_TO_letters_parent" FOREIGN KEY ("parent_letter_id") REFERENCES "letters" ("id");

ALTER TABLE "user_social" ADD CONSTRAINT "FK_users_TO_user_social" FOREIGN KEY ("user_id") REFERENCES "users" ("id");
ALTER TABLE "user_app_conf" ADD CONSTRAINT "FK_users_TO_user_app_conf" FOREIGN KEY ("id") REFERENCES "users" ("id");

ALTER TABLE "lodges" ADD CONSTRAINT "FK_users_TO_lodges" FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "notebooks" ADD CONSTRAINT "FK_lodges_TO_notebooks" FOREIGN KEY ("lodge_id") REFERENCES "lodges" ("id");

ALTER TABLE "posts" ADD CONSTRAINT "FK_lodges_TO_posts" FOREIGN KEY ("lodge_id") REFERENCES "lodges" ("id");
ALTER TABLE "posts" ADD CONSTRAINT "FK_notebooks_TO_posts" FOREIGN KEY ("notebook_id") REFERENCES "notebooks" ("id");

ALTER TABLE "post_contents" ADD CONSTRAINT "FK_posts_TO_post_contents" FOREIGN KEY ("post_id") REFERENCES "posts" ("id");
ALTER TABLE "post_images" ADD CONSTRAINT "FK_posts_TO_post_images" FOREIGN KEY ("post_id") REFERENCES "posts" ("id");

ALTER TABLE "user_notebook_collection" ADD CONSTRAINT "FK_notebooks_TO_user_notebook_collection" FOREIGN KEY ("notebook_id") REFERENCES "notebooks" ("id");
ALTER TABLE "user_notebook_collection" ADD CONSTRAINT "FK_users_TO_user_notebook_collection" FOREIGN KEY ("collecter_id") REFERENCES "users" ("id");

ALTER TABLE "user_post_collection" ADD CONSTRAINT "FK_users_TO_user_post_collection" FOREIGN KEY ("collecter_id") REFERENCES "users" ("id");
ALTER TABLE "user_post_collection" ADD CONSTRAINT "FK_posts_TO_user_post_collection" FOREIGN KEY ("post_id") REFERENCES "posts" ("id");

ALTER TABLE "user_volunteer_favorites" ADD CONSTRAINT "FK_volunteer_favorites_info_TO_user_volunteer_favorites" FOREIGN KEY ("volunteer_code") REFERENCES "volunteer_favorites_info" ("volunteer_code");
ALTER TABLE "user_volunteer_favorites" ADD CONSTRAINT "FK_users_TO_user_volunteer_favorites" FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "subscriptions" ADD CONSTRAINT "FK_users_TO_subscriptions" FOREIGN KEY ("subscriber_id") REFERENCES "users" ("id");
ALTER TABLE "subscriptions" ADD CONSTRAINT "FK_lodges_TO_subscriptions" FOREIGN KEY ("target_lodge_id") REFERENCES "lodges" ("id");

-- 5. 코멘트(Comment) 추가
COMMENT ON COLUMN "user_social"."provider" IS '구글, 트위터, 카카오 등';
COMMENT ON COLUMN "user_social"."social_email" IS '소셜 로그인을 통해 얻은 계정 정보';
COMMENT ON COLUMN "user_social"."linked_at" IS '연결 시각';
COMMENT ON COLUMN "volunteer_favorites_info"."volunteer_code" IS '외부 API에서 준 고유 코드(1365 자원봉사 코드)';
COMMENT ON COLUMN "users"."keycloak_id" IS '키클락에서 가져온 고유 id';
COMMENT ON COLUMN "users"."email" IS '가입할 때 입력한 이메일';
COMMENT ON COLUMN "users"."status" IS '활동 상태';
COMMENT ON COLUMN "user_app_conf"."settings" IS '앱 설정은 자주 바뀔 수 있는 값이므로 속성을 따로 만들지 않고 json으로 통합 관리';
COMMENT ON COLUMN "posts"."status" IS '글 작성 상태.';
COMMENT ON COLUMN "user_notebook_collection"."memo" IS '메모 길이 설정 필요';
COMMENT ON COLUMN "post_images"."image_url" IS 'cdn 서버에 있는 이미지 url';
COMMENT ON COLUMN "user_volunteer_favorites"."volunteer_code" IS '외부 API에서 준 고유 코드(1365 자원봉사 코드)';
COMMENT ON COLUMN "lodges"."introduction" IS '소유자의 짧은소개글';
COMMENT ON COLUMN "post_contents"."content" IS 'json으로 콘텐츠 저장. 안에 수많은 글 설정이 있음';
COMMENT ON COLUMN "user_post_collection"."memo" IS '메모 길이 설정 필요';
COMMENT ON COLUMN "notebooks"."lodge_id" IS '수첩이 보관된 오두막 정보 필요';
COMMENT ON COLUMN "notebooks"."color_code" IS '수첩 색깔 설정 시 필요';
COMMENT ON COLUMN "notebooks"."display_order" IS '정렬 방법';
COMMENT ON COLUMN "notebooks"."visibility_status" IS '공개 상태';