import { createSlice } from "@reduxjs/toolkit";
const initialState = {
	gameState: "",
	roomName: "anonymous",
	score: 0,
	seconds: 0,
};

export const store = createSlice({
	name: "stateGame",
	initialState,
	reducers: {
		setStateGame(state, action) {
			return action.payload;
		},
	},
});
export const { setStateGame } = store.actions;
export default store.reducer;
