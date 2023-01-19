## RoomInfo

**Request**

`POST: /roominfo`

**body:**

```json
{
	"nameRoom": "string"
}
```

**Response**

```json
{
	"isExist": "boolean",
	"body": {
		"roomName": "string",
		"countUsers": "number"
	}
}
```

## Create room

### difficulty and custom game

**Request**

POST: /createroom

```json
{
	"nameRoom": "string",
	"difficulty": "easy" | "medium" | "hard",

}
```

**or**

```json
{
	"nameRoom": "string",
	"settings": {
		"rows": "number",
		"columns": "number",
		"bombs": "number"
	}
}
```

**Response**

```json
{
	"success": "boolean",
	"message": "string"
}
```

message optional

**Example response**

```json
{
	"status": false,
	"message": "room already exists"
}
```

```json
{
	"status": true
}
```

# Socket request

`url: /minesweeper-socket?roomName={roomName}&playerName={playerName}`

all websocket requests has the follow format:

```json
{
	"action": "{action}",
	"body": {
		// body
	}
}
```

`action` can be: `openCell` `setFlag` ``

# Template

## Title

**Request**

```http
code
```

**Response**

```typescript
{
	code;
}
```

**Example response**

```json
{
 code
}
```

планы:
для сокета создать экшен restart для перезапуска игры после конца с теми же настройками
Добавить к ячейке информацию о пользователе: сколько открыл ячеек и сколько поставил флагов
в gameState возвращать список пользователей и информацию по каждому юзеру (сколько открыл ячеек) и добавить имя пользователя кто завершил игру
