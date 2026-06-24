-- PostgreSQL 16+ DDL Schema for PG/Hostel Management SaaS application
-- Shared-Schema Multi-Tenant Isolation using workspace_id

-- 1. Organizations Table
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Workspaces Table (Tenants mapping)
CREATE TABLE workspaces (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    subdomain VARCHAR(100) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Users Table (Handles roles and masked government IDs)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- Enum: OWNER, MANAGER, TENANT
    gov_id_type VARCHAR(100), -- e.g., PASSPORT, AADHAAR, DRIVING_LICENSE
    gov_id_number_masked VARCHAR(100), -- Masked value (e.g., *****-1234)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_email_workspace UNIQUE (workspace_id, email)
);

-- 4. Rooms Table (Supports total and vacant beds tracking)
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    room_number VARCHAR(50) NOT NULL,
    total_beds INT NOT NULL,
    vacant_beds INT NOT NULL,
    price_per_month DECIMAL(12, 2) NOT NULL,
    CONSTRAINT uq_room_number_workspace UNIQUE (workspace_id, room_number),
    CONSTRAINT chk_vacant_beds CHECK (vacant_beds >= 0 AND vacant_beds <= total_beds)
);

-- Index optimization for active vacancy searches
CREATE INDEX idx_vacant_rooms ON rooms(id) WHERE vacant_beds > 0;

-- 5. Leases Table (Supports MONTHLY and FIXED_PERIOD types)
CREATE TABLE leases (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    tenant_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    lease_type VARCHAR(50) NOT NULL, -- Enum: MONTHLY, FIXED_PERIOD
    start_date DATE NOT NULL,
    end_date DATE, -- Null for monthly lease type
    rent_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- Enum: ACTIVE, TERMINATED, COMPLETED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Invoices Table
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    lease_id BIGINT NOT NULL REFERENCES leases(id) ON DELETE RESTRICT,
    amount DECIMAL(12, 2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- Enum: PENDING, PAID, OVERDUE
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Invoice Items Table
CREATE TABLE invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL
);

-- 8. Payments Table (Immutable ledger entries)
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    invoice_id BIGINT REFERENCES invoices(id) ON DELETE SET NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL, -- Enum: UPI, CARD, CASH, NET_BANKING
    transaction_reference VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexing workspace_id on all tables for tenant-routing performance
CREATE INDEX idx_users_workspace ON users(workspace_id);
CREATE INDEX idx_rooms_workspace ON rooms(workspace_id);
CREATE INDEX idx_leases_workspace ON leases(workspace_id);
CREATE INDEX idx_invoices_workspace ON invoices(workspace_id);
CREATE INDEX idx_payments_workspace ON payments(workspace_id);
