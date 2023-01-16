export const START_GAME = (difficulty: "easy" | "medium" | "hard") => ({
	action: "startGame",
	body: {
		difficulty: difficulty,
	},
});
export const START_CUSTOM_GAME = (
	rows: number,
	columns: number,
	bombs: number
) => ({
	action: "startCustomGame",
	body: { rows, columns, bombs },
});

export const SET_FLAG = (targetRow: number, targetColumn: number) => ({
	action: "setFlag",
	body: {
		row: targetRow,
		column: targetColumn,
	},
});

export const MOUSE_POSITION = (x: number, y: number) => ({
	action: "mousePosition",
	body: {
		x: x,
		y: y,
	},
});

export const OPEN_CELL = (targetRow: number, targetColumn: number) => ({
	action: "openCell",
	body: {
		row: targetRow,
		column: targetColumn,
	},
});
