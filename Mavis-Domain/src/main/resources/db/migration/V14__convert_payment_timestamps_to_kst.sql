UPDATE payment SET requested_at = DATE_ADD(requested_at, INTERVAL 9 HOUR) WHERE requested_at IS NOT NULL;
UPDATE payment SET approved_at = DATE_ADD(approved_at, INTERVAL 9 HOUR) WHERE approved_at IS NOT NULL;
UPDATE payment SET canceled_at = DATE_ADD(canceled_at, INTERVAL 9 HOUR) WHERE canceled_at IS NOT NULL;
