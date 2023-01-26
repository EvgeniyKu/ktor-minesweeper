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
): startCustomGameActionType => ({
	action: "startCustomGame",
	body: { rows, columns, bombs },
});

export const SET_FLAG = (
	row: number,
	column: number
): defaultActionType<defaultPositionCellType> => ({
	action: "setFlag",
	body: {
		row,
		column,
	},
});

export const OPEN_CELL = (
	row: number,
	column: number
): defaultActionType<defaultPositionCellType> => ({
	action: "openCell",
	body: {
		row,
		column,
	},
});

export const MOUSE_POSITION = (
	x: number,
	y: number
): mousePositionActionType => ({
	action: "mousePosition",
	body: {
		x,
		y,
	},
});

type defaultActionType<T extends object> = {
	action: string;
	body: T;
};

type mousePositionBodyType = {
	x: number;
	y: number;
};
type defaultPositionCellType = {
	row: number;
	column: number;
};
type startCustomGameBodyType = {
	rows: number;
	columns: number;
	bombs: number;
};
type mousePositionActionType = defaultActionType<mousePositionBodyType>;
type startCustomGameActionType = defaultActionType<startCustomGameBodyType>;
