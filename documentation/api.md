# Post request

## Room join

**Request**

```http
 url: expample.com/?roomjoin
 params: {
	nameRoom: string
	nameUser: string
 }
```

**Response**

```json
{
 status: boolean
 message?: string
}
```

**Example response**

```json
{
	status:false
	message: "room does not exist"
}
```

## Create room

### difficulty

**Request**

```http
url: expample.com/?createroom
params: {
	nameRoom: string
	difficulty: "easy" | "medium" | "hard"
}
```

**Response**

```json
{
 status: boolean
 message?: string
}
```

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

### Custom

**Request**

```http
url: expample.com/?createcustomroom
params: {
	nameRoom: string
	rows: number,
	columns: number,
	bombs: number
}

```

**Response**

```json
{
 status: boolean
 message?: string
}
```

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

#Socket request

#Template

## Title

**Request**

```http
code
```

**Response**

```json
{
 code
}
```

**Example response**

```json
{
 code
}
```