-- Código interno do cliente na planilha: identifica o cliente e evita duplicar em uploads repetidos
ALTER TABLE clientes ADD COLUMN codigo_interno VARCHAR(255);
ALTER TABLE clientes ADD CONSTRAINT uk_clientes_codigo_interno UNIQUE (codigo_interno);

-- A planilha pode não trazer a data de contratação: sem data, o contrato fica fora do gráfico de evolução
ALTER TABLE contratos ALTER COLUMN data_inicio DROP NOT NULL;
