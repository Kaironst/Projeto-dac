CREATE SCHEMA IF NOT EXISTS cqrs_schema;

CREATE TABLE IF NOT EXISTS cqrs_schema.cliente(
  id bigint primary key,
  cpf varchar(255),
  email varchar(255),
  estado int,
  nome varchar(255),
  salario float,
  telefone varchar(255)
);

CREATE TABLE IF NOT EXISTS cqrs_schema.gerente(
 id bigint primary key,
 administrador boolean,
 cpf varchar(255),
 email varchar(255),
 nome varchar(255),
 telefone varchar(255)
);

CREATE TABLE IF NOT EXISTS cqrs_schema.endereco(
 id bigint primary key,
 cep varchar(255),
 cidade varchar(255),
 complemento varchar(255),
 estado varchar(255),
 logradouro varchar(255),
 numero int,
 cliente_id bigint
);

CREATE TABLE IF NOT EXISTS cqrs_schema.conta(
 id bigint primary key,
 cliente bigint,
 data_criacao date,
 gerente bigint,
 limite float,
 numero varchar(255),
 saldo float
);

CREATE TABLE IF NOT EXISTS cqrs_schema.item_historico(
 id bigint primary key,
 data_hora timestamp(6) without time zone,
 tipo int,
 valor_movimentacao float,
 conta_destino_id bigint,
 conta_origem_id bigint
);
