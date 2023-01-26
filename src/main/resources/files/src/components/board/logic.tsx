import React, { useEffect, useRef, FC } from "react";
import { useSelector, useDispatch } from "react-redux";

import { setStateGame } from "../../store/stateGame";
import { OPEN_CELL, SET_FLAG, START_GAME } from "../../api/actionsSocket";
import socket from "../../socket";
import { log } from "../../utils";

import type { storeType } from "../../store";
import type {
	bodyForGameState,
	board,
	gameState,
	baseResponse,
} from "../../api/typeSocket";
import type { prop } from "./index";

export default function Logic({
	Ui,
	...props
}: {
	Ui: FC<propsForUiType>;
}): React.ReactElement {
	const store: bodyForGameState = useSelector(
		(store: storeType) => store.stateGame
	);
	const { board, gameState } = store;
	const dispatch = useDispatch();

	/* Handler mesage */
	useEffect(() => {
		const handlerMessage = (message: MessageEvent) => {
			const data: baseResponse = JSON.parse(message.data);
			switch (data["messageType"]) {
				case "game_state":
					const body = data["body"];
					dispatch(setStateGame(body));
					console.log("gameState", gameState);
					console.log("body", body);
					break;
			}
		};
		socket.addEventListener("message", handlerMessage);
		return () => socket.removeEventListener("message", handlerMessage);
	}, []);

	const refBoard = useRef() as React.MutableRefObject<HTMLDivElement>;
	useEffect(() => {
		const refCurrent = refBoard.current; //fix: notFoundContext
		if (refCurrent === undefined) {
			return;
		}
		const handleBaseValidation = (fn: callbackFunc) => {
			return (event: MouseEvent) => {
				const target = event.target as HTMLDivElement;
				if (!("id" in target)) {
					return;
				}
				if (target.id !== "cell") {
					return;
				}
				if (!("dataset" in target)) {
					log("Not found dataset");
					return;
				}
				const row = target.dataset.row;
				const column = target.dataset.column;
				if (row !== undefined && column !== undefined) {
					fn(+row, +column, event);
				}
			};
		};
		// CLICK
		const handleClick: callbackFunc = (row, column, event) => {
			event.preventDefault();
			socket.send(JSON.stringify(OPEN_CELL(row, column)));
		};
		const handleClickWithValidation = handleBaseValidation(handleClick);
		refCurrent.addEventListener("click", handleClickWithValidation);

		// CONTEXTMENU
		const handleContextMenu: callbackFunc = (row, column, event) => {
			event.preventDefault();
			socket.send(JSON.stringify(SET_FLAG(row, column)));
		};
		const handleContextMenuWithValidation =
			handleBaseValidation(handleContextMenu);
		refCurrent.addEventListener("contextmenu", handleContextMenuWithValidation);

		return () => {
			socket.send(JSON.stringify(START_GAME("easy"))); // при размонтирование говорим серверу "Игра закончилась"
			refCurrent.removeEventListener("click", handleClickWithValidation);
			refCurrent.removeEventListener(
				"contextmenu",
				handleContextMenuWithValidation
			);
		};
	}, [refBoard]);

	const propsForUi: propsForUiType = {
		gameState,
		ref: refBoard,
		board,
		...props,
	};
	return Ui(propsForUi) as React.ReactElement;
}

type callbackFunc = (row: number, column: number, event: any) => void;
export type propsForUiType = {
	gameState: gameState;
	board?: board;
	ref: React.MutableRefObject<HTMLDivElement>;
};
