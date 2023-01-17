import { useEffect, useRef } from "react";
import socket from "../../socket";
import { useSelector } from "react-redux";

import { OPEN_CELL, SET_FLAG, START_GAME } from "../../api/actionsSocket";

export default function Logic(Ui, props) {
	const store = useSelector((store) => store.stateGame);
	const { board, gameState } = store;

	const refBoard = useRef();
	useEffect(() => {
		const refCurrent = refBoard.current; //fix: notFoundContext
		const handleBaseValidation = (fn) => {
			return (event) => {
				const { target } = event;
				if (target.id !== "cell") {
					return;
				}
				const dataEl = target.dataset;
				const row = dataEl.row;
				const column = dataEl.column;
				fn(row, column, event);
			};
		};
		// CLICK
		const handleClick = (row, column, event) => {
			event.preventDefault();
			socket.send(JSON.stringify(OPEN_CELL(row, column)));
		};
		const handleClickWithValidation = handleBaseValidation(handleClick);
		refCurrent.addEventListener("click", handleClickWithValidation);

		// CONTEXTMENU
		const handleContextMenu = (row, column, event) => {
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

	const propsForUi = { gameState, ref: refBoard, board, ...props };
	return Ui(propsForUi);
}
