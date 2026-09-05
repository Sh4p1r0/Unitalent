
CREATE OR REPLACE FUNCTION fn_total_postulaciones(p_id_oferta INT)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
    v_total INT;
BEGIN
    SELECT COUNT(*) INTO v_total
    FROM postulaciones
    WHERE id_oferta = p_id_oferta;

    RETURN v_total;
END;
$$;


CREATE OR REPLACE FUNCTION fn_nivel_empleabilidad(p_id_estudiante INT)
RETURNS VARCHAR
LANGUAGE plpgsql
AS $$
DECLARE
    v_aceptadas INT;
BEGIN
    SELECT COUNT(*) INTO v_aceptadas
    FROM postulaciones
    WHERE id_estudiante = p_id_estudiante AND estado = 'Aceptada';

    IF v_aceptadas > 0 THEN
        RETURN 'Empleable';
    ELSE
        RETURN 'En búsqueda';
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE sp_registrar_postulacion(
    p_id_estudiante INT,
    p_id_oferta INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado_oferta VARCHAR;
    v_existe INT;
BEGIN
    SELECT estado INTO v_estado_oferta FROM ofertas WHERE id_oferta = p_id_oferta;

    IF v_estado_oferta IS NULL THEN
        RAISE EXCEPTION 'La oferta % no existe', p_id_oferta;
    END IF;

    IF v_estado_oferta <> 'Activa' THEN
        RAISE EXCEPTION 'No se puede postular: la oferta % no está activa', p_id_oferta;
    END IF;

    SELECT COUNT(*) INTO v_existe
    FROM postulaciones
    WHERE id_estudiante = p_id_estudiante AND id_oferta = p_id_oferta;

    IF v_existe > 0 THEN
        RAISE EXCEPTION 'El estudiante % ya postuló a la oferta %', p_id_estudiante, p_id_oferta;
    END IF;

    INSERT INTO postulaciones (id_estudiante, id_oferta, estado)
    VALUES (p_id_estudiante, p_id_oferta, 'Enviada');
END;
$$;


CREATE OR REPLACE FUNCTION trg_fn_validar_oferta_activa()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR;
BEGIN
    SELECT estado INTO v_estado FROM ofertas WHERE id_oferta = NEW.id_oferta;

    IF v_estado <> 'Activa' THEN
        RAISE EXCEPTION 'No se permite postular a una oferta cerrada (id_oferta=%)', NEW.id_oferta;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_validar_oferta_activa ON postulaciones;

CREATE TRIGGER trg_validar_oferta_activa
BEFORE INSERT ON postulaciones
FOR EACH ROW
EXECUTE FUNCTION trg_fn_validar_oferta_activa();
