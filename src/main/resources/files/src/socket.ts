import { Socket } from "./controller/socket";
import { urlSocket } from "./api/config";

const socket = new Socket(urlSocket);
socket.onopen = function (data) {
	console.log(`[openWS] - Открыто соединение. ${data}`);
};
socket.onerror = function (data) {
	console.log(
		`[ErrorWS] - Ошибка на стороне WebSocket: ${JSON.stringify(data)}`
	);
	console.log(data);
};

socket.onmessage = function (message) {
	const data = JSON.parse(message.data);
	if (data.messageType !== "tick") {
		console.log(data);
	}
};

socket.onclose = function (data) {
	console.log(`[CloseWS] - Соединение закрыто: ${data}`);
};

export default socket;
