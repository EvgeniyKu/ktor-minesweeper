import stateGame from "./stateGame";
import { configureStore } from "@reduxjs/toolkit";

const store = configureStore({
	reducer: {
		stateGame,
	},
});
export default store;
