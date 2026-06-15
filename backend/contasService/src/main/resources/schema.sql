CREATE SCHEMA IF NOT EXISTS contas_schema;

CREATE SEQUENCE IF NOT EXISTS numero_seq MINVALUE 0 MAXVALUE 9999 CYCLE;
CREATE OR REPLACE FUNCTION gerar_numero()
RETURNS TEXT AS '
DECLARE
    new_code TEXT;
    is_taken BOOLEAN;
BEGIN
    LOOP
        new_code := LPAD(((NEXTVAL(''numero_seq'') * 48271 + 3187) % 10000)::TEXT, 4, ''0'');
        
        --checa por conflitos
        SELECT EXISTS(SELECT 1 FROM conta WHERE numero = new_code) INTO is_taken;
        IF NOT is_taken THEN
            RETURN new_code;
        END IF;
    END LOOP;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION forcar_numero_conta()
RETURNS TRIGGER AS '
BEGIN
    IF NEW.numero IS NULL OR NEW.numero = '''' THEN
        NEW.numero := gerar_numero();
    END IF;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_gerar_numero_conta ON conta;

CREATE TRIGGER trigger_gerar_numero_conta
BEFORE INSERT ON conta
FOR EACH ROW
EXECUTE FUNCTION forcar_numero_conta();
