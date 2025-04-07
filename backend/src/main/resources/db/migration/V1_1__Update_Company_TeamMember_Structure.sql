-- 备份现有数据
CREATE TABLE IF NOT EXISTS companies_backup AS SELECT * FROM companies;
CREATE TABLE IF NOT EXISTS team_members_backup AS SELECT * FROM team_members;
CREATE TABLE IF NOT EXISTS users_backup AS SELECT * FROM users;

-- 修改 companies 表结构
-- 1. 移除外键约束
ALTER TABLE companies DROP FOREIGN KEY IF EXISTS companies_ibfk_1;

-- 2. 修改 ID 列为自增
ALTER TABLE companies MODIFY id BIGINT AUTO_INCREMENT;

-- 3. 添加创建时间和更新时间字段
ALTER TABLE companies ADD COLUMN IF NOT EXISTS create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 4. 迁移用户创建时间到公司创建时间
UPDATE companies c 
JOIN users u ON c.id = u.id 
SET c.create_time = u.create_time, c.update_time = u.update_time;

-- 修改 team_members 表
-- 1. 确保表存在
CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    companyId BIGINT,
    name VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'member' NOT NULL,
    username VARCHAR(50),
    userId BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 修改字段名称
ALTER TABLE team_members CHANGE COLUMN IF EXISTS companyId company_id BIGINT;

-- 3. 添加 user_id 字段，并从 userId 迁移数据
ALTER TABLE team_members ADD COLUMN IF NOT EXISTS user_id BIGINT;
UPDATE team_members SET user_id = userId WHERE userId IS NOT NULL;
ALTER TABLE team_members DROP COLUMN IF EXISTS userId;

-- 4. 添加外键约束
-- 注意：只有在确保数据完整性后才添加约束
ALTER TABLE team_members
ADD CONSTRAINT IF NOT EXISTS fk_team_member_company
FOREIGN KEY (company_id) REFERENCES companies(id)
ON DELETE CASCADE;

ALTER TABLE team_members
ADD CONSTRAINT IF NOT EXISTS fk_team_member_user
FOREIGN KEY (user_id) REFERENCES users(id)
ON DELETE SET NULL;

-- 从现有公司用户创建管理员团队成员
INSERT INTO team_members (company_id, name, position, email, phone, role, username, user_id, create_time, update_time)
SELECT 
    c.id as company_id,
    COALESCE(c.contact_person, u.real_name, 'Admin') as name,
    COALESCE(c.contact_position, 'HR') as position,
    u.email,
    u.phone,
    'admin' as role,
    u.username,
    u.id as user_id,
    u.create_time,
    u.update_time
FROM 
    companies c
JOIN 
    users u ON c.id = u.id
WHERE
    NOT EXISTS (
        SELECT 1 FROM team_members tm 
        WHERE tm.company_id = c.id AND tm.role = 'admin'
    );

-- 不使用 flyway_schema_history，因为这可能是手动运行的脚本
-- 如果使用 Flyway，它会自动记录版本 