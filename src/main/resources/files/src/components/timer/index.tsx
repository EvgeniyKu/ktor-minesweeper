import { useState, useEffect } from "react";

import socket from "../../socket";
import type { bodyForGameState } from "../../api/typeSocket";

import type { baseResponse } from "../../api/typeSocket";

type typeGameState = Pick<bodyForGameState, "gameState">;
type typeProps = typeGameState;

export default function Timer({ gameState }: typeProps) {
	const [timer, setTimer] = useState(0);

	if (gameState !== "running" && timer !== 0) {
		setTimer(0);
	}
	useEffect(() => {
		const handleTick = (message: MessageEvent) => {
			const data: baseResponse = JSON.parse(message.data);
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
	return <span>{`${minutes}:${seconds}`}</span>;
}
