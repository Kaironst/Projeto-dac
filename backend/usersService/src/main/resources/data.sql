insert into cliente (cpf, nome, email, salario, telefone, estado) values
('12912861012', 'Catharyna', 'cli1@bantads.com.br', 10000.00, '41999999999', 1),
('09506382000', 'Cleuddônio', 'cli2@bantads.com.br', 20000.00, '4188888888', 1),
('85733854057', 'Catianna', 'cli3@bantads.com.br', 3000.00, '4177777777', 1),
('58872160006', 'Cutardo', 'cli4@bantads.com.br', 500.00, '4166666666', 1),
('76179646090', 'Coândrya', 'cli5@bantads.com.br', 1500.00, '4155555555', 1)
ON CONFLICT (cpf) DO NOTHING;

insert into endereco (logradouro, numero, complemento, cep, cidade, estado, cliente_id)
  select 'rua joaquim joão', 123, 'perto da esquina', '11245987', 'jão caipira do leste', 'ES', (
        select id from cliente where cpf = '12912861012'
      )
  where not exists (
        select 1 from endereco where logradouro = 'rua joaquim joão'
      );

insert into endereco (logradouro, numero, complemento, cep, cidade, estado, cliente_id)
  select 'rua josefina josara', 697, 'ao lado da lojinha do zé', '11245987', 'jão caipira do norte', 'ES', (
        select id from cliente where cpf = '09506382000'
      )
  where not exists (
        select 1 from endereco where logradouro = 'rua josefina josara'
      );

insert into endereco (logradouro, numero, complemento, cep, cidade, estado, cliente_id)
  select 'rua azulada', 14, 'segundo andar', '11243347', 'jão caipira do oeste', 'ES', (
        select id from cliente where cpf = '85733854057'
      )
  where not exists (
        select 1 from endereco where logradouro = 'rua azulada'
      );

insert into endereco (logradouro, numero, complemento, cep, cidade, estado, cliente_id)
  select 'rua do morrinho', 183, 'no topo da rampa', '17666987', 'jão caipira do leste', 'ES', (
        select id from cliente where cpf = '58872160006'
      )
  where not exists (
        select 1 from endereco where logradouro = 'rua do morrinho'
      );

insert into endereco (logradouro, numero, complemento, cep, cidade, estado, cliente_id)
  select 'rua campo pequeno', 128, 'perto da quadra de futebol', '13257227', 'jão caipira do sul', 'ES', (
        select id from cliente where cpf = '76179646090'
      )
  where not exists (
        select 1 from endereco where logradouro = 'rua campo pequeno'
      );
