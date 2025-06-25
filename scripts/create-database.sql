-- Criar tabelas para o sistema de discagem automática

-- Tabela de dispositivos
CREATE TABLE IF NOT EXISTS devices (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'offline',
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    current_call VARCHAR(20),
    calls_today INTEGER DEFAULT 0,
    success_rate DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de listas de chamadas
CREATE TABLE IF NOT EXISTS call_lists (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de números de telefone
CREATE TABLE IF NOT EXISTS phone_numbers (
    id VARCHAR(50) PRIMARY KEY,
    list_id VARCHAR(50) REFERENCES call_lists(id) ON DELETE CASCADE,
    number VARCHAR(20) NOT NULL,
    name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'pending',
    attempts INTEGER DEFAULT 0,
    last_call TIMESTAMP,
    call_duration INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de histórico de chamadas
CREATE TABLE IF NOT EXISTS call_history (
    id VARCHAR(50) PRIMARY KEY,
    device_id VARCHAR(50) REFERENCES devices(id),
    number_id VARCHAR(50) REFERENCES phone_numbers(id),
    phone_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    duration INTEGER DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    error_message TEXT
);

-- Tabela de configurações
CREATE TABLE IF NOT EXISTS settings (
    key VARCHAR(50) PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserir configurações padrão
INSERT OR REPLACE INTO settings (key, value) VALUES
('conference_size', '6'),
('retry_attempts', '3'),
('call_timeout', '30'),
('delay_between_calls', '5'),
('max_concurrent_calls', '2');

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_phone_numbers_list_id ON phone_numbers(list_id);
CREATE INDEX IF NOT EXISTS idx_phone_numbers_status ON phone_numbers(status);
CREATE INDEX IF NOT EXISTS idx_call_history_device_id ON call_history(device_id);
CREATE INDEX IF NOT EXISTS idx_call_history_started_at ON call_history(started_at);
