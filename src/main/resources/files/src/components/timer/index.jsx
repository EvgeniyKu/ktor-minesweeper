import { useState, useEffect } from "react";
import socket from "../../socket";
export default function Timer({ gameState }) {
	const [timer, setTimer] = useState(0);

	if (gameState !== "running" && timer !== 0) {
		setTimer(0);
	}
	useEffect(() => {
		const handleTick = (message) => {
			const data = JSON.parse(message.data);
			if (data.messageType === "tick") {
				setTimer(data.body.seconds);
			}
		};
		socket.addEventListener("message", handleTick);
		return () => {
			socket.removeEventListener("message", handleTick);
		};
	}, []);
	const minutes = +(timer / 60).toFixed(0);
	const seconds = timer % 60;
	return `${minutes}:${seconds}`;
}
