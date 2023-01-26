export type cellType = {
	row: number;
	column: number;
	countOfBombs: null | number;
	isFlagged: boolean;
	isOpened: boolean;
};

export type board = cellType[][];

export type gameState = "not_started" | "running" | "lose" | "win";

export interface bodyForGameState {
	board?: board;
	gameState: gameState;
	roomName: string;
	score: number;
	seconds: number;
}

export type messageType = "tick" | "game_state";

export interface baseResponse {
	messageType: messageType;
	body: bodyForGameState | any;
}
