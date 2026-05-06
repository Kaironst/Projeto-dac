insert into conta (numero, cliente, gerente, saldo, limite, data_criacao) values 
  -- Catharyna - Geniéve
  (
    '1291', 
    (select id from users_schema.cliente where cpf = '12912861012'),
    (select id from gerentes_schema.gerente where cpf = '98574307084'),
    800.00, 5000.00, '2000-01-01'
  ),
  -- Cleuddônio - Godophredo
  (
    '0950',
    (select id from users_schema.cliente where cpf = '09506382000'),
    (select id from gerentes_schema.gerente where cpf = '64065268052'),
    -10000.00, 10000.00, '1990-10-10'
  ),
  -- Catiana - Gyândula
  (
    '8573',
    (select id from users_schema.cliente where cpf = '85733854057'),
    (select id from gerentes_schema.gerente where cpf = '23862179060'),
    -1000.00, 1500.00, '2012-12-12'
  ),
  -- Cutardo - Geniéve
  (
    '5887', 
    (select id from users_schema.cliente where cpf = '58872160006'),
    (select id from gerentes_schema.gerente where cpf = '98574307084'),
    150000.00, 0, '2022-02-22'
  ),
  -- Coândria - Godophredo
  (
    '7617',
    (select id from users_schema.cliente where cpf = '76179646090'),
    (select id from gerentes_schema.gerente where cpf = '64065268052'),
    1500.00, 0, '2025-01-01'
  )
ON CONFLICT (numero) DO NOTHING;


insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0 , 1000.00, '2020-01-01 10:00:00', 
    (select id from users_schema.cliente where cpf = '12912861012'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2020-01-01 10:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0, 900.00, '2020-01-01 11:00:00', 
    (select id from users_schema.cliente where cpf = '12912861012'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2020-01-01 11:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 1, 550.00, '2020-01-01 12:00:00', 
    (select id from users_schema.cliente where cpf = '12912861012'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2020-01-01 12:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 1, 350.00, '2020-01-01 13:00:00', 
    (select id from users_schema.cliente where cpf = '12912861012'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2020-01-01 13:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0, 2000.00, '2020-01-10 15:00:00', 
    (select id from users_schema.cliente where cpf = '12912861012'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2020-01-10 15:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 1, 500.00, '2020-01-15 08:00:00', 
    (select id from users_schema.cliente where cpf = '12912861012'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2020-01-15 08:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 2, 1700.00, '2020-01-20 12:00:00', 
    (select id from users_schema.cliente where cpf = '12912861012'),
    (select id from users_schema.cliente where cpf = '09506382000') 
  where not exists ( select 1 from item_historico where data_hora = '2020-01-20 12:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0, 1000.00, '2025-01-01 12:00:00', 
    (select id from users_schema.cliente where cpf = '09506382000'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2025-01-01 12:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0, 5000.00, '2025-01-02 10:00:00', 
    (select id from users_schema.cliente where cpf = '09506382000'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2025-01-02 10:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 1, 200.00, '2025-01-10 10:00:00', 
    (select id from users_schema.cliente where cpf = '09506382000'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2025-01-10 10:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0, 7000.00, '2025-02-05 10:00:00', 
    (select id from users_schema.cliente where cpf = '09506382000'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2025-02-05 10:00:00');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0, 1000.00, '2025-05-05', 
    (select id from users_schema.cliente where cpf = '85733854057'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2025-05-05');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 1, 2000.00, '2025-05-06', 
    (select id from users_schema.cliente where cpf = '85733854057'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2025-05-06');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0, 150000.00, '2025-06-01', 
    (select id from users_schema.cliente where cpf = '58872160006'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2025-06-01');

insert into item_historico (tipo, valor_movimentacao, data_hora ,conta_origem_id, conta_destino_id)
  select 0, 1500.00, '2025-07-01', 
    (select id from users_schema.cliente where cpf = '76179646090'),
    null 
  where not exists ( select 1 from item_historico where data_hora = '2025-07-01');
