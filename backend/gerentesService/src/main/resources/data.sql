
insert into gerente (cpf, nome, email, telefone, administrador) values
('98574307084', 'Geniéve', 'ger1@bantads.com.br', '41555555555', false),
('64065268052', 'Godophredo', 'ger2@bantads.com.br', '4144444444', false),
('23862179060', 'Gyândula', 'ger3@bantads.com.br', '4133333333', false),
('40501740066', 'Adamântio', 'adm1@bantads.com.br', '4122222222', true)
ON CONFLICT (cpf) DO NOTHING;
