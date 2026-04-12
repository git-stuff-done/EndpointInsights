.PHONY: start stop restart

start:
	docker compose -f compose.dev.yaml up -d

stop:
	docker compose -f compose.dev.yaml down

restart:
	docker compose -f compose.dev.yaml down
	docker compose -f compose.dev.yaml up -d
