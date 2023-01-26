import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";

import type { bodyForGameState } from "../api/typeSocket";

const initialState: bodyForGameState = {
	gameState: "not_started",
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
